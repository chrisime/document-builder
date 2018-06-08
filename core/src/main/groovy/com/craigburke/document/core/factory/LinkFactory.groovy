package com.craigburke.document.core.factory

import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.text.Link

/**
 * Factory for link nodes
 * @author Craig Burke
 */
@Deprecated
class LinkFactory extends AbstractFactory {

    boolean isLeaf() { true }

    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) { false }

    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        Paragraph paragraph

        if (builder.parentName == 'paragraph') {
            paragraph = builder.current
        } else {
            paragraph = builder.getColumnParagraph(builder.current)
        }

        String text = attributes.value ?: value
        String url = attributes.url ?: value

        List elements = paragraph.add(text, true)

        elements.findAll { it instanceof Link }.each { Link link ->
            link.url = url
            link.style = attributes.style
            builder.setNodeProperties(link, attributes, 'link')
        }

        elements
    }

}
