/**
 * Handler.java
 * 
 * Copyright (C) 2017, 2018 by Arménio Pinto.
 * Please read LICENSE for the license details.
 */
package com.armeniopinto.bridgeroad.traffic.updater;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import static com.armeniopinto.bridgeroad.traffic.updater.TrafficDetector.DetectedTraffic;

/**
 * Detects the traffic conditions on Bridge Road and stores them in DynamoDB.
 * 
 * @author armenio.pinto
 */
public class Handler implements RequestHandler<Map<String, Object>, Void> {

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mmX";

	@Override
	public Void handleRequest(final Map<String, Object> event, final Context context) {
		final BufferedImage image;
		try {
			image = downloadMap(context.getLogger());
		} catch (final IOException ioe) {
			throw new RuntimeException("Failed to download the map.", ioe);
		}
		saveToDDB(TrafficDetector.detectOutbound(image));

		return null;
	}

	private static BufferedImage downloadMap(final LambdaLogger logger) throws IOException {
		try {
			final String mapURL = System.getenv("BASE_MAP_URL")
					+ buildConnection(logger).get().select("#lu_map").get(0).attr("src");
			logger.log(String.format("Downloading traffic map from '%s'", mapURL));
			final BufferedImage image = ImageIO.read(new URL(mapURL));

			final File file = File.createTempFile(getNowKey() + "_", ".png");
			logger.log(String.format("Saving traffic map to '%s'.", file.getAbsolutePath()));
			ImageIO.write(image, "png", file);
			saveToS3(file, "bridge-road-traffic-updater");

			return image;

		} catch (final IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private static Connection buildConnection(final LambdaLogger logger) {
		final String searchURL = System.getenv("SEARCH_URL");
		Connection connection = Jsoup.connect(searchURL);
		logger.log(String.format("Downloading traffic page from '%s'.", searchURL));
		final String userAgent = System.getenv("USER_AGENT");
		if (userAgent != null) {
			connection = connection.userAgent(userAgent);
			logger.log(String.format("Using user agent '%s'.", userAgent));
		}

		final String proxyAddress = System.getenv("PROXY_ADDRESS");
		final String proxyPort = System.getenv("PROXY_PORT");
		if (proxyAddress != null && proxyPort != null) {
			setupProxy();
			final int port = Integer.parseInt(proxyPort);
			logger.log(String.format("Using proxy '%s:%d'.", proxyAddress, port));
			connection = connection.proxy(proxyAddress, port);
		}

		return connection;
	}

	private static void setupProxy() {
		final String proxyUser = System.getenv("PROXY_USER");
		final String proxyPassword = System.getenv("PROXY_PASSWORD");
		Authenticator.setDefault(new Authenticator() {
			@Override
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
			}
		});
		System.setProperty("https.proxyUser", proxyUser);
		System.setProperty("https.proxyPassword", proxyPassword);
	}

	private static void saveToS3(final File file, final String bucket) {
		AmazonS3 client = null;
		try {
			client = AmazonS3ClientBuilder.defaultClient();
			client.putObject(bucket, file.getName(), file);
		} finally {
			if (client != null) {
				client.shutdown();
			}
		}
	}

	private static void saveToDDB(final DetectedTraffic outbound) {
		AmazonDynamoDB ddb = null;
		try {
			ddb = AmazonDynamoDBClientBuilder.defaultClient();
			final HashMap<String, AttributeValue> item = new HashMap<>();
			final String key = getNowKey();
			item.put("date", new AttributeValue().withS(key.substring(0, 10)));
			item.put("time", new AttributeValue().withS(key.substring(11, 16)));
			item.put("outbound_severity", new AttributeValue().withN(toString(outbound.traffic)));
			item.put("outbound_samples", new AttributeValue().withS(toString(outbound.samples)));
			ddb.putItem("bridge_road_traffic", item);
		} finally {
			if (ddb != null) {
				ddb.shutdown();
			}
		}
	}

	private static String toString(final Traffic... traffics) {
		final String[] severities = new String[traffics.length];
		for (int i = 0; i < traffics.length; i++) {
			severities[i] = Integer.toString(traffics[i].getSeverity());
		}

		return String.join(", ", severities);
	}

	private static final String getNowKey() {
		return DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneOffset.UTC)
				.format(Instant.now());
	}

}
