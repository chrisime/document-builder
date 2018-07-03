package com.craigburke.document.builder

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

/**
 * @since 25/06/2018
 */
abstract class BaseSpec extends Specification {

    Logger getLogger() {
        LoggerFactory.getLogger(getClass())
    }

    def setup() {
        logger.warn("--- ${specificationContext.currentIteration.name} ---")
    }
}
