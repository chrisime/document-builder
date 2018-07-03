package com.craigburke.document.core.dom.attribute

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * Two dimensional width/height.
 */
@Immutable
@CompileStatic
class Dimension {
    BigDecimal width
    BigDecimal height
}
