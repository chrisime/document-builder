package ox.softeng.document.core.dsl

import com.craigburke.document.core.dom.block.BlockNode
import com.craigburke.document.core.dom.block.Document
import com.craigburke.document.core.dom.block.Table
import groovy.transform.CompileStatic

/**
 * @since 07/06/2018
 */
@CompileStatic
trait TableApi<K extends BlockNode> implements Api {

    abstract K getCurrentNode()

    TableApi table(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Table) Closure closure) {
        table([:], closure)
    }

    TableApi table(Map attributes = [:], @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Table) Closure closure = null) {
        Table table = Table.create(attributes)
        currentNode.addToChildren(table)

        table.setNodeProperties(attributes)

        callClosure closure, table

        if (currentNode instanceof Document) {
            table.normalizeColumnWidths()
        }

        this
    }

}
