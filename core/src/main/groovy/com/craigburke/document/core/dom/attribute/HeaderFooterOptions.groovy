package com.craigburke.document.core.dom.attribute

import groovy.transform.Canonical
import groovy.transform.CompileStatic

import java.time.OffsetDateTime

/**
 * Options for generated page headers and footers
 * @author Craig Burke
 */
@CompileStatic
@Canonical
class HeaderFooterOptions {
    OffsetDateTime dateGenerated
    String         pageCount
    String         pageNumber
}
