package org.arquillian.spacelift.gradle

import org.arquillian.spacelift.task.TaskRegistry
import org.slf4j.Logger

/**
 * This class defines anything installable
 */
interface Installation extends ContainerizableObject<Installation> {

    /**
     *
     * @return product identified of the installation
     */
    String getProduct()

    /**
     *
     * @return version of the product
     */
    String getVersion()

    /**
     *
     * @return directory where installation is installed
     */
    File getHome()

    /**
     * Checks whether the installation was already installed
     *
     * @return {@code true} if installation is already installed, {@code false} otherwise
     */
    boolean isInstalled()

    /**
     * Installs installation into Spacelift workspace
     *
     * @param logger logger which this installation will use
     */
    void install(Logger logger)

    /**
     * Registers provided tools into registry
     * @param registry
     */
    void registerTools(TaskRegistry registry)

    // FIXME installation should also have uninstall(logger) or something like that
}