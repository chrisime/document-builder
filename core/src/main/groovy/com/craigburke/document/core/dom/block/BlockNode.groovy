package com.craigburke.document.core.dom.block

import com.craigburke.document.core.dom.BaseNode
import com.craigburke.document.core.dom.attribute.Alignable
import com.craigburke.document.core.dom.attribute.Border
import com.craigburke.document.core.dom.attribute.MarginAssignable
import com.craigburke.document.core.dom.attribute.Stylable
import groovy.transform.CompileStatic

/**
 * The base node for all block nodes
 * @author Craig Burke
 */
@CompileStatic
abstract class BlockNode<P extends BlockNode, C extends BaseNode> extends BaseNode<P>
  implements Stylable, Alignable, MarginAssignable {

    Border border = new Border()

    List<C> children = []

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

    BlockNode addToChildren(C child) {
        child.parent = this
        this
    }

    BlockNode addToChildren(Integer indexToAddAt, C child) {
        children.add(indexToAddAt, child)
        this
    }

    C child(Integer index) {
        children[index]
    }

    @Override
    void setNodeProperties(List<Map> nodePropertiesMap) {
        super.setNodeProperties(nodePropertiesMap)
        setNodeFont(nodePropertiesMap)
        margin = defaultMargin.clone()
        nodePropertiesMap.each {
            if (it.margin)
                margin << (it.margin as Map)
            if (it.border)
                border << (it.border as Map)
        }
    }

}
