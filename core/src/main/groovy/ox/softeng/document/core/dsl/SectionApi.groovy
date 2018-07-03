package ox.softeng.document.core.dsl

import com.craigburke.document.core.dom.PageBreak
import com.craigburke.document.core.dom.attribute.Align
import com.craigburke.document.core.dom.block.Document
import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.text.Heading

/**
 * @since 31/05/2018
 */
trait SectionApi implements Api {

    abstract Document getDocument()

    SectionApi section(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SectionApi) Closure closure) {
        callClosure(closure, this)
    }

    SectionApi paragraph(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Paragraph) Closure closure) {
        paragraph([:], closure)
    }

    SectionApi paragraph(Map attributes = [:], String text) {
        handleParagraph(attributes, null, text)
    }

    SectionApi paragraph(Map attributes = [:], String text,
                         @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Paragraph) Closure closure) {
        handleParagraph(attributes, closure, text)
    }

    SectionApi paragraph(Map attributes = [:],
                         @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Paragraph) Closure closure = null) {
        handleParagraph(attributes, closure, null)
    }

    SectionApi pageBreak() {
        document.addToChildren(new PageBreak())
        this
    }

    SectionApi heading1(Map attributes = [:], String text) {
        handleHeader(attributes, 1, text)
    }

    SectionApi heading2(Map attributes = [:], String text) {
        handleHeader(attributes, 2, text)
    }

    SectionApi heading3(Map attributes = [:], String text) {
        handleHeader(attributes, 3, text)
    }

    SectionApi heading4(Map attributes = [:], String text) {
        handleHeader(attributes, 4, text)
    }

    SectionApi heading5(Map attributes = [:], String text) {
        handleHeader(attributes, 5, text)
    }

    SectionApi heading6(Map attributes = [:], String text) {
        handleHeader(attributes, 6, text)
    }

    private SectionApi handleHeader(Map attributes, Integer level, String text) {
        Heading heading = Heading.create(attributes)
        heading.level = level
        document.addToChildren(heading)
        heading.setNodeProperties(attributes)
        heading.value = text
        this
    }

    private SectionApi handleParagraph(Map attributes, Closure closure, String text) {
        Paragraph paragraph = Paragraph.create(attributes)
        document.addToChildren(paragraph)
        paragraph.setNodeProperties(attributes)

        if (paragraph.parent instanceof Document) {
            paragraph.align = paragraph.align ?: Align.LEFT
        }
        if (text) {
            paragraph.text(attributes, text)
        }
        if (closure) {
            callClosure closure, paragraph
        }
        this
    }

}
