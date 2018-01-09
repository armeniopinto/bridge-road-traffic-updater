/**
 * TrafficSpec.groovy
 * 
 * Copyright (C) 2017 by Arménio Pinto.
 * Please read LICENSE for the license details.
 */
package com.armeniopinto.bridgeroad.traffic.updater

import spock.lang.*

import java.awt.Color

import javax.imageio.ImageIO

/**
 * Tests {@link traffic}.
 * 
 * @author armenio.pinto
 */
class TrafficSpec extends Specification {

	def final LEGEND_FILE = "traffic-legend-20180109-custom.png"

	def "traffic legend RGBA matches traffic conditions"() {
		given: "the traffic legend"
		def image = ImageIO.read(new File(getClass().getClassLoader().getResource(LEGEND_FILE).getFile()))

		and: "the RGBA values for the different traffic conditions"
		def noneRGB = new Color(255, 255, 255, 255).getRGB()
		def lowRGB = image.getRGB(8, 2)
		def mediumRGB = image.getRGB(29, 3)
		def heavyRGB = image.getRGB(49, 3)
		def veryHeavyRGB = image.getRGB(69, 3)

		when: "we try to identify the traffic conditions"
		def none = Traffic.fromRGBA(noneRGB)
		def low = Traffic.fromRGBA(lowRGB)
		def medium = Traffic.fromRGBA(mediumRGB)
		def heavy = Traffic.fromRGBA(heavyRGB)
		def veryHeavy = Traffic.fromRGBA(veryHeavyRGB)

		then: "the traffic conditions must match"
		none == Traffic.None
		low == Traffic.Low
		medium == Traffic.Medium
		heavy == Traffic.Heavy
		veryHeavy == Traffic.VeryHeavy
	}

	def "invalid traffic legend RGBA values return unknown traffic condition"() {
		when: "we try to identify traffic conditions from invalid RGB values"
		def traffic1 = Traffic.fromRGBA(0)
		def traffic2 = Traffic.fromRGBA(Integer.MAX_VALUE)
		def traffic3 = Traffic.fromRGBA(Integer.MIN_VALUE)

		then: "the traffic conditions must be unknonw"
		traffic1 == Traffic.Unknown
		traffic2 == Traffic.Unknown
		traffic3 == Traffic.Unknown
	}

	def "traffic severity matches traffic conditions"() {
		when: "we try to identify the traffic conditions from their severities"
		def none = Traffic.fromSeverity(0)
		def low = Traffic.fromSeverity(1)
		def medium = Traffic.fromSeverity(2)
		def heavy = Traffic.fromSeverity(3)
		def veryHeavy = Traffic.fromSeverity(4)

		then: "the traffic conditions must match"
		none == Traffic.None
		low == Traffic.Low
		medium == Traffic.Medium
		heavy == Traffic.Heavy
		veryHeavy == Traffic.VeryHeavy
	}

	def "invalid traffic severity values return unknown traffic condition"() {
		when: "we try to identify traffic conditions from invalid severities"
		def traffic1 = Traffic.fromSeverity(-1)
		def traffic2 = Traffic.fromSeverity(5)

		then: "the traffic conditions must be unknonw"
		traffic1 == Traffic.Unknown
		traffic2 == Traffic.Unknown
	}
}