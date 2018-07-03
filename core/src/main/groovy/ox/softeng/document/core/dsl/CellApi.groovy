package ox.softeng.document.core.dsl

import com.craigburke.document.core.dom.attribute.Font
import com.craigburke.document.core.dom.attribute.Margin
import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.block.table.Cell
import groovy.transform.TypeChecked

/**
 * @since 07/06/2018
 */
trait CellApi implements ParagraphApi, TableApi<Cell> {

    abstract Cell getCell()

    @Override
    Paragraph getParagraph() {
        if (cell.children && cell.children.first() instanceof Paragraph) {
            return cell.children.first() as Paragraph
        }
        Paragraph paragraph = new Paragraph(font: cell.font.clone() as Font, align: cell.align)
        cell.addToChildren(paragraph)
        paragraph.setNodeProperties(margin: Margin.NONE.toMap())
        paragraph
    }

    @Override
    Cell getCurrentNode() {
        cell
    }

}
