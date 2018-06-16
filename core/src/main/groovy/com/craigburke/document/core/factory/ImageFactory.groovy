package com.craigburke.document.core.factory

import com.craigburke.document.core.Image
import com.craigburke.document.core.ImageType
import com.craigburke.document.core.TextBlock

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Factory for image nodes
 * @author Craig Burke
 */
class ImageFactory extends AbstractFactory {

    boolean isLeaf() {
        true
    }

    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, node, Map attributes) {
        false
    }

    def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) {
        Image image = new Image(attributes)

        if (!image.width || !image.height) {
            BufferedImage bufferedImage = image.withInputStream { ImageIO.read(it) }
            if (bufferedImage == null) {
                throw new IllegalStateException("could not read image $attributes")
            }
            if (image.width) {
                image.height = (image.width * (bufferedImage.height / bufferedImage.width)).toInteger()
            } else if (image.height) {
                image.width = (image.height * (bufferedImage.width / bufferedImage.height)).toInteger()
            } else {
                image.width = bufferedImage.width
                image.height = bufferedImage.height
            }
        }

        if (!image.name || builder.imageFileNames.contains(image.name)) {
            image.name = generateImageName(image)
        }
        builder.imageFileNames << image.name

        TextBlock paragraph
        if (builder.parentName == 'paragraph') {
            paragraph = builder.current
        } else {
            paragraph = builder.getColumnParagraph(builder.current)
        }
        image.parent = paragraph
        paragraph.children << image

        image
    }

    static String generateImageName(Image image) {
        "${image.hash}.${image.type == ImageType.JPG ? 'jpg' : 'png'}"
    }

}
