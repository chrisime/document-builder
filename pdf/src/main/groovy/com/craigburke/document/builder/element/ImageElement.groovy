package com.craigburke.document.builder.element

import com.craigburke.document.core.dom.Image
import groovy.transform.Canonical
import groovy.transform.CompileStatic

/**
 * Rendering element for the Image node
 * @author Craig Burke
 */
@CompileStatic
@Canonical
class ImageElement implements ParagraphElement<Image> {
    Image node
}
