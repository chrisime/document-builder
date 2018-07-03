package com.craigburke.document.builder.element

import com.craigburke.document.core.dom.text.TextNode
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.apache.pdfbox.pdmodel.font.PDFont

/**
 * Rendering element for the Text node
 * @author Craig Burke
 */
@CompileStatic
@Canonical
class TextElement implements ParagraphElement<TextNode> {
    PDFont   pdfFont
    TextNode node
    String   text
    int      width
}
