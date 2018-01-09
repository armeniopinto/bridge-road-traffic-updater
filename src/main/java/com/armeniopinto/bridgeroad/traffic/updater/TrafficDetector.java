/**
 * TrafficDetector.java
 * 
 * Copyright (C) 2017, 2018 by Arménio Pinto.
 * Please read LICENSE for the license details.
 */
package com.armeniopinto.bridgeroad.traffic.updater;

import java.awt.image.BufferedImage;

/**
 * Detects the traffic conditions based on pre-defined control points.
 * 
 * @author armenio.pinto
 */
class TrafficDetector {

	/** The coordinates of the image control points used to detect the outbound traffic. */
	private static final int[][] OUTBOUND_DETECTION_POINTS = { { 458, 10 }, { 400, 85 },
			{ 374, 114 }, { 316, 160 }, { 274, 212 }, { 230, 288 } };

	/** The control points weights in the overall outbound traffic conditions. */
	private static final double[] OUTBOUND_DETECTION_WEIGHTS = { 0.1, 0.3, 0.1, 0.1, 0.1, 0.3 };

	public static DetectedTraffic detectOutbound(final BufferedImage image) {
		return detect(image, OUTBOUND_DETECTION_POINTS, OUTBOUND_DETECTION_WEIGHTS);
	}

	private static DetectedTraffic detect(final BufferedImage image, final int points[][],
			final double[] weights) {
		double total = Traffic.Unknown.getSeverity();
		final Traffic[] samples = new Traffic[points.length];
		for (int i = 0; i < points.length; i++) {
			final Traffic traffic = Traffic.fromRGBA(image.getRGB(points[i][0], points[i][1]));
			samples[i] = traffic;
			if (traffic != Traffic.Unknown) {
				if (total == Traffic.Unknown.getSeverity()) {
					total = 0;
				}
				total += traffic.getSeverity() * weights[i];
			}
		}

		return new DetectedTraffic(Traffic.fromSeverity((int) Math.round(total)), samples);
	}

	static class DetectedTraffic {
		public Traffic traffic;
		public Traffic[] samples;

		public DetectedTraffic(final Traffic traffic, final Traffic[] samples) {
			this.traffic = traffic;
			this.samples = samples;
		}
	}

}
