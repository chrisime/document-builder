package com.craigburke.document.core.dom

import com.craigburke.document.core.dom.block.Paragraph
import groovy.transform.Canonical
import groovy.transform.CompileStatic

/**
 * Line break node
 * @author Craig Burke
 */
@CompileStatic
@Canonical
class LineBreak extends BaseNode<Paragraph> {
    Integer height = 0
}
