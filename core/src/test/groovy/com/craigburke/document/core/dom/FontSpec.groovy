package com.craigburke.document.core.dom

import com.craigburke.document.core.dom.attribute.Font

import spock.lang.Specification

/**
 * Font tests
 * @author Craig Burke
 */
class FontSpec extends Specification {

    def "override properties with left shit"() {
        Font font = new Font(family:'Initial', size:10)

        when:
        font << [family:'New']

        then:
        font.family == 'New'
        font.size == 10

        when:
        font << [size:12]

        then:
        font.family == 'New'
        font.size == 12

    }

}
