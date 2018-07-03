package ox.softeng.document.core.dsl

import com.craigburke.document.core.dom.block.table.Cell
import com.craigburke.document.core.dom.block.table.Row
import groovy.transform.CompileStatic

/**
 * @since 07/06/2018
 */
@CompileStatic
trait RowApi implements Api {

    abstract Row getRow()

    RowApi cell(Map attributes = [:], String text) {
        handleCell(attributes, null, text)
    }

    RowApi cell(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Cell) Closure closure) {
        cell([:], closure)
    }

    RowApi cell(Map attributes = [:],
                @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Cell) Closure closure = null) {
        handleCell(attributes, closure, null)
    }

    private RowApi handleCell(Map attributes, Closure closure, String text) {
        Cell cell = Cell.create(attributes)
        row.addToChildren(cell)
        cell.setNodeProperties(attributes)
        if (closure) {
            callClosure closure, cell
        } else if (text) {
            cell.text(attributes, text)
        }

        this
    }

}
