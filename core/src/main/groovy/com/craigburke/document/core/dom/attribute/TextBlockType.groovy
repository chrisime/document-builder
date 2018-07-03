package com.craigburke.document.core.dom.attribute

/**
 * @since 11/06/2018
 */
interface TextBlockType extends ParentAware {

    Margin getDefaultMargin()

    Margin getMargin()

    String getRef()

    Color getBackground()

    BigDecimal getLineSpacing()

    BigDecimal getLineSpacingMultiplier()

    Align getAlign()

    Font getFont()

    void setFont(Font font)

}
