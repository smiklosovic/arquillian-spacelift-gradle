package org.arquillian.spacelift.gradle

import org.slf4j.Logger

/**
 * Represents a test closure.
 *
 * @author kpiwko
 */
interface Test extends ContainerizableObject<Test> {

    /**
     * Executes this tests using the logger to notify user about progress
     *
     * @param logger
     */
    void executeTest(Logger logger);
}