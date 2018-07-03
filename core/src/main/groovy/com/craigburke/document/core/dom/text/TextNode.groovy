package com.craigburke.document.core.dom.text

import com.craigburke.document.core.dom.BaseNode
import com.craigburke.document.core.dom.attribute.BackgroundAssignable
import com.craigburke.document.core.dom.attribute.Bookmarkable
import com.craigburke.document.core.dom.attribute.Stylable
import com.craigburke.document.core.dom.block.BlockNode
import groovy.transform.CompileStatic

/**
 * Text node
 * @author Craig Burke
 */
@CompileStatic
abstract class TextNode<P extends BlockNode> extends BaseNode<P>
  implements Stylable, Bookmarkable, BackgroundAssignable {

    String value

    @Override
    List<String> getTemplateKeys(String nodeKey) {
        List<String> keys = super.getTemplateKeys(nodeKey)
        if (style)
            keys += "${nodeKey}.${style}".toString()
        keys
    }

    @Override
    void setNodeFont(List<Map> nodeProperties) {
        font = cloneParentFont()
        nodeProperties.each {
            font << (it.font as Map)
        }
    }

    @Override
    void setNodeProperties(List<Map> nodePropertiesMap) {
        super.setNodeProperties(nodePropertiesMap)
        setNodeFont(nodePropertiesMap)
        setNodeBackground(nodePropertiesMap)
    }

}
