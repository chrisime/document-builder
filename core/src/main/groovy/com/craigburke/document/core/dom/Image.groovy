package com.craigburke.document.core.dom

import com.craigburke.document.core.dom.attribute.ImageType
import com.craigburke.document.core.dom.block.Paragraph
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import java.security.MessageDigest

/**
 * Image node
 * @author Craig Burke
 */
@CompileStatic
class Image extends BaseNode<Paragraph> {

    String hashName
    ImageType type = ImageType.JPG
    BigDecimal width
    BigDecimal height
    String url
    byte[] data

    void setType(String value) {
        type = Enum.valueOf(ImageType, value.toUpperCase())
    }

    byte[] getData() {
        if (!this.@data && url)
            this.data = new URL(url).bytes

        this.@data
    }

    def withInputStream(@ClosureParams(value = SimpleType, options = 'java.io.ByteArrayInputStream') Closure work) {
        work.call(new ByteArrayInputStream(getData()))
    }

    String getHash() {
        Formatter hexHash = new Formatter()
        MessageDigest.getInstance('SHA-1').digest(getData()).each { byte b -> hexHash.format('%02x', b) }
        hexHash.toString()
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static Image create(Map attributes) {
        new Image(attributes)
    }

}
