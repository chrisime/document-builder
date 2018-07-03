package ox.softeng.document.core.dsl

import com.craigburke.document.core.dom.Image
import com.craigburke.document.core.dom.LineBreak
import com.craigburke.document.core.dom.block.Paragraph
import groovy.transform.CompileStatic

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * @since 07/06/2018
 */
@CompileStatic
trait ParagraphApi implements Api {

    abstract Paragraph getParagraph()

    ParagraphApi text(Map attributes = [:], String text) {
        getParagraph().add(text, attributes)
        this
    }

    ParagraphApi link(Map attributes = [:], String value) {
        String url = attributes.url as String ?: value
        String text = attributes.value as String ?: value
        link(attributes, url, text)

    }

    ParagraphApi link(Map attributes = [:], String url, String displayText) {
        getParagraph().add(displayText, attributes, url)
        this
    }

    ParagraphApi image(Map attributes = [:], String url) {
        attributes.url = url
        image(attributes)
    }

    ParagraphApi image(Map attributes = [:], byte[] data) {
        attributes.data = data
        image(attributes)
    }

    ParagraphApi image(Map attributes) {
        Image image = Image.create(attributes)

        if (!image.width || !image.height) {
            BufferedImage bufferedImage = image.withInputStream { ImageIO.read(it) } as BufferedImage
            if (bufferedImage == null) {
                throw new IllegalStateException("could not read image $attributes")
            }
            if (image.width) {
                image.height = image.width * (bufferedImage.height / bufferedImage.width)
            } else if (image.height) {
                image.width = image.height * (bufferedImage.width / bufferedImage.height)
            } else {
                image.width = bufferedImage.width
                image.height = bufferedImage.height
            }
        }

        image.hashName = "${image.hash}.${image.type.value}"

        getParagraph().addToChildren(image)

        this
    }

    ParagraphApi lineBreak(Integer height = 0) {
        getParagraph().addToChildren(new LineBreak(height: height))
        this
    }

}
