package com.craigburke.document.core.dom.text

import com.craigburke.document.core.dom.block.Paragraph
import groovy.transform.Canonical
import groovy.transform.CompileStatic

/**
 * The node that can be associated with a URL link
 * @author Craig Burke
 */
@CompileStatic
@Canonical
class Link extends TextNode<Paragraph> {
    String url
}
