/**
 * Traffic.java
 * 
 * Copyright (C) 2017, 2018 by Arménio Pinto.
 * Please read LICENSE for the license details.
 */
package com.armeniopinto.bridgeroad.traffic.updater;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The possible traffic conditions.
 * 
 * In 2018 Google changed the severity colours on their maps but maintained the old legend.
 * 
 * @see https://maps.gstatic.com/tactile/layers/traffic-legend-20150511.png
 * @see traffic-legend-20180109-custom.png
 * @author armenio.pinto
 */
enum Traffic {

	Unknown(-1, new int[] { 0, 0, 0 }),

	None(0, new int[] { 249, 249, 249 }, new int[] { 255, 255, 255 }),

	Low(1, new int[] { 99, 214, 104 }),

	Medium(2, new int[] { 255, 151, 77 }),

	Heavy(3, new int[] { 242, 60, 50 }),

	VeryHeavy(4, new int[] { 129, 31, 31 });

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
