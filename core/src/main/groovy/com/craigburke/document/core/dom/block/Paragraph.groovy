package com.craigburke.document.core.dom.block

import com.craigburke.document.core.dom.BaseNode
import com.craigburke.document.core.dom.Image
import com.craigburke.document.core.dom.LineBreak
import com.craigburke.document.core.dom.attribute.BackgroundAssignable
import com.craigburke.document.core.dom.attribute.Bookmarkable
import com.craigburke.document.core.dom.attribute.Margin
import com.craigburke.document.core.dom.attribute.TextBlockType
import com.craigburke.document.core.dom.text.Link
import com.craigburke.document.core.dom.text.Text
import com.craigburke.document.core.dom.text.TextNode
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import ox.softeng.document.core.dsl.ParagraphApi

/**
 * Block element that holds text and images
 * @author Craig Burke
 */
@AutoClone
@CompileStatic
class Paragraph extends BlockNode<Document, BaseNode>
  implements Bookmarkable, BackgroundAssignable, TextBlockType, ParagraphApi {

    static Margin DEFAULT_MARGIN = new Margin(top: 12, bottom: 12, left: 0, right: 0)

    BigDecimal lineSpacing
    BigDecimal lineSpacingMultiplier = 1.15G

    String getText() {
        List<Text> textChildren = children.findAll { it instanceof Text } as List<Text>
        textChildren*.value.join('')
    }

    List<BaseNode> add(String text, Map attributes, String url = null) {
        List<BaseNode> elements = []
        String[] textSections = text.split('\n')

        textSections.each { String section ->
            TextNode element = url ? new Link(value: section, parent: this, url: url) :
                               new Text(value: section, parent: this)
            element.style = attributes.style
            element.setNodeProperties(attributes)

            elements << element

            if (section != textSections.last()) {
                elements << new LineBreak(parent: this)
            }
        }

        if (text.endsWith('\n')) {
            elements << new LineBreak(parent: this)
        }

        elements
    }

    List<Image> getAllImages() {
        children?.findAll { it instanceof Image } as List<Image>
    }

    @Override
    Margin getDefaultMargin() {
        DEFAULT_MARGIN
    }

    @Override
    Paragraph getParagraph() {
        this
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static Paragraph create(Map attributes) {
        new Paragraph(attributes)
    }

}
