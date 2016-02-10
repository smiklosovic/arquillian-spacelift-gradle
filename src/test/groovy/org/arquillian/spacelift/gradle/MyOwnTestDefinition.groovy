package org.arquillian.spacelift.gradle

import groovy.transform.CompileStatic
import org.slf4j.Logger

import static org.junit.Assert.assertNotNull

@CompileStatic
class MyOwnTestDefinition extends BaseContainerizableObject<MyOwnTestDefinition> implements Test {

    DeferredValue<Object> myDSL = DeferredValue.of(Object.class)

    MyOwnTestDefinition(String name, Object parent) {
        super(name, parent)
    }

    MyOwnTestDefinition(String name, MyOwnTestDefinition other) {
        super(name, other)
        this.myDSL = other.@myDSL.copy()
    }

    @Override
    MyOwnTestDefinition clone(String name) {
        new MyOwnTestDefinition(name, this)
    }

    @Override
    void executeTest(Logger logger) {
        Object value = myDSL.resolve()
        assertNotNull value
    }
}