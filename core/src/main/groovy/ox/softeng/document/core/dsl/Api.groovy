package ox.softeng.document.core.dsl

import groovy.transform.CompileStatic

/**
 * @since 07/06/2018
 */
@CompileStatic
interface Api {

    void callClosure(Closure closure, def delegate)

    void callClosure(Closure closure, def delegate, int resolveStrategy)

}
