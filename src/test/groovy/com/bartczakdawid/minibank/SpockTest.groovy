package com.bartczakdawid.minibank

import spock.lang.Specification

class SpockTest extends Specification {

    def "Spock Test"() {
        expect:
        1 + 1 == 2
    }
}