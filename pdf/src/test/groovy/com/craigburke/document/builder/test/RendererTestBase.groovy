package com.craigburke.document.builder.test

import com.craigburke.document.core.dom.BaseNode
import com.craigburke.document.core.dom.LineBreak
import com.craigburke.document.core.dom.attribute.Font
import com.craigburke.document.core.dom.attribute.Margin
import com.craigburke.document.core.dom.block.Document
import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.text.Text

import com.craigburke.document.builder.BaseSpec
import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.builder.render.TextBlockRenderer

/**
 * Base test for all Render Elements
 * @author Craig Burke
 */
class RendererTestBase extends BaseSpec {

    final float defaultLineHeight = 19f

    PdfDocument document

    def setup() {
        makeDocument()
    }

    def cleanup() {
        document?.saveAndClosePdf(null)
    }

    void makeDocument() {
        document = new PdfDocument(margin: Document.DEFAULT_MARGIN, font: new Font())
    }

    Paragraph makeParagraph(Paragraph paragraph, BaseNode parent = document) {
        Paragraph newParagraph = paragraph.clone()
        newParagraph.parent = parent
        parent.children << newParagraph
        newParagraph
    }

    Paragraph makeParagraph(int lineCount, BaseNode parent = document) {
        Paragraph paragraph = new Paragraph(margin: Margin.NONE, font: new Font())
        lineCount.times {
            paragraph.children << new Text(value: "Line${it}", font: new Font())
            if (it != lineCount - 1) {
                paragraph.children << new LineBreak()
            }
        }
        paragraph.parent = parent
        parent.children << paragraph
        paragraph
    }

    TextBlockRenderer makeParagraphElement(Paragraph paragraph) {
        float width = paragraph.parent.width
        new TextBlockRenderer(paragraph, document, 0f, width)
    }

}
