package com.craigburke.document.core.dom

import com.craigburke.document.core.dom.attribute.ParentAware
import com.craigburke.document.core.dom.block.BlockNode
import com.craigburke.document.core.dom.block.Document
import groovy.transform.CompileStatic
import ox.softeng.document.core.dsl.Api

/**
 * The base node for all document nodes
 * @author Craig Burke
 */
@CompileStatic
abstract class BaseNode<P extends BaseNode> implements ParentAware<P>, Api {

    P      parent
    String name

    BaseNode() {
        name = Document.isAssignableFrom(getClass()) ? 'document' : getClass().simpleName.toLowerCase()
    }

    List<String> getTemplateKeys(String nodeKey) {
        [nodeKey]
    }

    @Override
    Document getDocument() {
        parent?.document
    }

    void setParent(P parent) {
        this.@parent = parent
        if (parent instanceof BlockNode)
            parent.children << this
    }

    void setNodeProperties(Map attributes) {
        String[] templateKeys = getTemplateKeys(name)
        List<Map> nodeProperties = []
        templateKeys.each { String key ->
            if (document.templateMap[key])
                nodeProperties << (document.templateMap[key] as Map)
        }

        if (attributes)
            nodeProperties << attributes

        setNodeProperties(nodeProperties)
    }

    void setNodeProperties(List<Map> nodePropertiesMap) {
    }

    @Override
    void callClosure(Closure closure, Object delegate, int resolveStrategy = Closure.DELEGATE_FIRST) {
        if (closure) {
            closure.resolveStrategy = resolveStrategy
            closure.delegate = delegate
            closure.call()
        }
    }

}
