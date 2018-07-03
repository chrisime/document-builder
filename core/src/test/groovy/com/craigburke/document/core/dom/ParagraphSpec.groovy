package com.craigburke.document.core.dom

import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.text.Text

import spock.lang.Shared
import spock.lang.Specification

/**
 * Paragraph tests
 * @author Craig Burke
 */
class ParagraphSpec extends Specification {

    @Shared
    Paragraph paragraph
    static final int DEFAULT_FONT_SIZE = 12

    def setup() {
        paragraph = new Paragraph()
        paragraph.children << new Text(font:[size:DEFAULT_FONT_SIZE])
    }

    def "text combines text values"() {
        paragraph.children = [
            new Text(value:'FOO'),
            new Text(value:'BAR'),
            new LineBreak(),
            new Text(value:'123')
        ]

        expect:
        paragraph.text == 'FOOBAR123'
    }

}
