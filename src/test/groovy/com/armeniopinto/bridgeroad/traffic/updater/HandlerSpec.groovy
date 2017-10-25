/**
 * HandlerSpec.groovy
 * 
 * Copyright (C) 2017 by Arménio Pinto.
 * Please read LICENSE for the license details.
 */
package com.armeniopinto.bridgeroad.traffic.updater

import spock.lang.*

/**
 * Tests {@link Handler}.
 * 
 * @author armenio.pinto
 */
class HandlerSpec extends Specification {

	def "convert an array with a single traffic conditions to a string of severities"() {
		given: "an array with a single traffic condition"
		Traffic[] traffics = [Traffic.Low]

		when: "we convert it to a string of severities"
		def severities = Handler.toString(traffics)

		then: "the string must contain the traffic severity"
		severities == Integer.toString(traffics[0].getSeverity())
	}

	def "convert an array with multiple traffic conditions to a string of severities"() {
		given: "an array with multiple traffic condition"
		Traffic[] traffics = [
			Traffic.Low,
			Traffic.Heavy,
			Traffic.None
		]

		when: "we convert it to a string of severities"
		def severities = Handler.toString(traffics)

		then: "the string must contain the traffic severity"
		severities == Traffic.Low.getSeverity() + ", " + Traffic.Heavy.getSeverity() + ", " + Traffic.None.getSeverity()
	}
}