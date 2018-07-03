package com.craigburke.document.core.dom.attribute

import com.craigburke.document.core.dom.BaseNode
import com.craigburke.document.core.dom.block.Document

/**
 * @since 07/06/2018
 */
interface ParentAware<T extends BaseNode> {

    T getParent()

    Document getDocument()

    List<String> getTemplateKeys(String nodeKey)

}
