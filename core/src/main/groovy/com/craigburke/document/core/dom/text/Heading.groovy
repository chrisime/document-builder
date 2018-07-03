package com.craigburke.document.core.dom.text

import com.craigburke.document.core.dom.attribute.Alignable
import com.craigburke.document.core.dom.attribute.Margin
import com.craigburke.document.core.dom.attribute.MarginAssignable
import com.craigburke.document.core.dom.attribute.TextBlockType
import com.craigburke.document.core.dom.block.Document
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

/**
 * Created by craig on 3/25/15.
 */
@CompileStatic
class Heading extends TextNode<Document> implements MarginAssignable, TextBlockType, Alignable {

    private static final List<BigDecimal> FONT_SIZE_MULTIPLIERS = [2.0, 1.5, 1.17, 1.12, 0.83, 0.75]

    static Margin DEFAULT_MARGIN = new Margin(top: 12, bottom: 12, left: 0, right: 0)

    BigDecimal lineSpacing
    BigDecimal lineSpacingMultiplier = 1.15
    int        level                 = 1

    @Override
    List<String> getTemplateKeys(String nodeKey) {
        List<String> keys = super.getTemplateKeys(nodeKey)
        keys += "heading${level}".toString()
        if (style)
            keys += "heading${level}.${style}".toString()
        keys
    }

    @Override
    void setNodeFont(List<Map> nodeProperties) {
        font = cloneParentFont()
        font.size = null
        nodeProperties.each {
            font << (it.font as Map)
        }
        if (!font.size) {
            font.size = document.font.size * FONT_SIZE_MULTIPLIERS[level - 1]
        }
    }

    @Override
    Margin getDefaultMargin() {
        DEFAULT_MARGIN
    }

    @Override
    void setNodeProperties(List<Map> nodePropertiesMap) {
        super.setNodeProperties(nodePropertiesMap)
        margin = defaultMargin.clone()
        nodePropertiesMap.each {
            if (it.margin)
                margin << (it.margin as Map)
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static Heading create(Map attributes) {
        new Heading(attributes)
    }

}
