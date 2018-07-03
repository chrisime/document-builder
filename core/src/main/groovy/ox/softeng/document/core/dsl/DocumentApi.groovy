package ox.softeng.document.core.dsl

import com.craigburke.document.core.dom.attribute.EmbeddedFont
import com.craigburke.document.core.dom.block.Document

/**
 * @since 31/05/2018
 */
trait DocumentApi implements SectionApi, TableApi<Document> {

    @Override
    Document getCurrentNode() {
        document
    }

    DocumentApi addFont(Map params, String location) {
        EmbeddedFont embeddedFont = EmbeddedFont.create(params)
        embeddedFont.file = new File(location)
        addFont(embeddedFont)
    }

    DocumentApi addFont(EmbeddedFont embeddedFont) {
        document.addToEmbeddedFonts embeddedFont
        this
    }

}
