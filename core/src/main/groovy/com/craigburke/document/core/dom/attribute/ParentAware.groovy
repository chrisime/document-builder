package com.craigburke.document.core.dom.attribute

import com.craigburke.document.core.dom.BaseNode
import com.craigburke.document.core.dom.block.Document

/**
 * @since 07/06/2018
 */
interface ParentAware {

    BaseNode getParent()

    Document getDocument()
}