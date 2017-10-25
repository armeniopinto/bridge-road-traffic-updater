/**
 * HandlerSpec.groovy
 * 
 * Copyright (C) 2017 by Arménio Pinto.
 * Please read LICENSE for the license details.
 */
package com.armeniopinto.bridgeroad.traffic.updater

import spock.lang.*

import java.awt.image.BufferedImage

/**
 * Tests {@link TrafficDetector}.
 * 
 * @author armenio.pinto
 */
class TrafficDetetorSpec extends Specification {

	def "map with no traffic for all control points is detected as no traffic"() {
		given: "a map with no traffic for all control points"
		def image = Mock(BufferedImage) {
			6 * getRGB(_, _) >> Traffic.None.rgbas[0]
		}

		when: "we detect the outbound traffic"
		def detected = TrafficDetector.detectOutbound(image)

		then: "no traffic must be detected"
		detected.traffic == Traffic.None
	}

	def "map with heavy traffic for all control points is detected as very heavy traffic"() {
		given: "a map with very heavy traffic for all control points"
		def image = Mock(BufferedImage) {
			6 * getRGB(_, _) >> Traffic.VeryHeavy.rgbas[0]
		}

		when: "we detect the outbound traffic"
		def detected = TrafficDetector.detectOutbound(image)

		then: "very heavy traffic must be detected"
		detected.traffic == Traffic.VeryHeavy
	}
}