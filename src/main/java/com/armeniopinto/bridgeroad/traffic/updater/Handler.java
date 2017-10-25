/**
 * Handler.java
 * 
 * Copyright (C) 2017 by Arménio Pinto.
 * Please read LICENSE for the license details.
 */
package com.armeniopinto.bridgeroad.traffic.updater;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
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

	private static final String SEARCH_URL = "https://www.google.co.uk/search?q=traffic+exeter+bridge+road";

	private static final String BASE_MAP_URL = "https://www.google.co.uk/";

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mmX";

	@Override
	public Void handleRequest(final Map<String, Object> event, final Context context) {
		final BufferedImage image;
		try {
			image = downloadMap();
		} catch (final IOException ioe) {
			throw new RuntimeException("Failed to download the map.", ioe);
		}
		saveToDDB(TrafficDetector.detectOutbound(image));

		return null;
	}

	private static BufferedImage downloadMap() throws IOException {
		try {
			final Document doc = Jsoup.connect(SEARCH_URL).get();
			final String url = doc.select("#lu_map").get(0).attr("src");
			final BufferedImage image = ImageIO.read(new URL(BASE_MAP_URL + url));

			final File file = File.createTempFile(getNowKey() + "_", ".png");
			ImageIO.write(image, "png", file);
			saveToS3(file, "bridge-road-traffic-updater");

			return image;

		} catch (final IOException ioe) {
			throw new RuntimeException(ioe);
		}
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
			item.put("instant", new AttributeValue().withS(getNowKey()));
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
