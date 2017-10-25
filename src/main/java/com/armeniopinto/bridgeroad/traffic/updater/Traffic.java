/**
 * Traffic.java
 * 
 * Copyright (C) 2017 by Arménio Pinto.
 * Please read LICENSE for the license details.
 */
package com.armeniopinto.bridgeroad.traffic.updater;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The possible traffic conditions.
 * 
 * @see https://maps.gstatic.com/tactile/layers/traffic-legend-20150511.png
 * @author armenio.pinto
 */
enum Traffic {

	Unknown(-1, new int[] { 0, 0, 0 }),

	None(0, new int[] { 249, 249, 249 }, new int[] { 255, 255, 255 }),

	Low(1, new int[] { 132, 202, 80 }),

	Medium(2, new int[] { 240, 125, 2 }),

	Heavy(3, new int[] { 230, 0, 0 }),

	VeryHeavy(4, new int[] { 158, 19, 19 });

	private final int severity;

	private final int[] rgbas;

	private static final HashMap<Integer, Traffic> CACHE = new HashMap<>();
	static {
		cache(None.rgbas, None);
		cache(Low.rgbas, Low);
		cache(Medium.rgbas, Medium);
		cache(Heavy.rgbas, Heavy);
		cache(VeryHeavy.rgbas, VeryHeavy);
	}

	private static void cache(final int[] rgbas, final Traffic traffic) {
		Arrays.stream(rgbas).forEach(rgba -> {
			CACHE.put(rgba, traffic);
		});
	}

	private static final HashMap<Integer, Traffic> SEVERITIES = new HashMap<>();
	static {
		SEVERITIES.put(None.getSeverity(), None);
		SEVERITIES.put(Low.getSeverity(), Low);
		SEVERITIES.put(Medium.getSeverity(), Medium);
		SEVERITIES.put(Heavy.getSeverity(), Heavy);
		SEVERITIES.put(VeryHeavy.getSeverity(), VeryHeavy);
	}

	private Traffic(final int severity, final int[]... rgbs) {
		this.severity = severity;
		rgbas = new int[rgbs.length];
		for (int i = 0; i < rgbs.length; i++) {
			rgbas[i] = new Color(rgbs[i][0], rgbs[i][1], rgbs[i][2], 255).getRGB();
		}
	}

	public int getSeverity() {
		return severity;
	}

	public static Traffic fromSeverity(final int severity) {
		return SEVERITIES.containsKey(severity) ? SEVERITIES.get(severity) : Unknown;
	}

	public static Traffic fromRGBA(final int rgba) {
		return CACHE.containsKey(rgba) ? CACHE.get(rgba) : Unknown;
	}

}
