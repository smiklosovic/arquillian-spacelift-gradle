package org.arquillian.spacelift.gradle

import groovy.transform.CompileStatic
import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.task.TaskRegistry
import org.arquillian.spacelift.task.net.DownloadTool
import org.slf4j.Logger

import static org.junit.Assert.assertTrue

@CompileStatic
class MyOwnInstallationDefinition extends BaseContainerizableObject<MyOwnInstallationDefinition> implements Installation {

    MyOwnInstallationDefinition(String name, Object parent) {
        super(name, parent)
    }

    MyOwnInstallationDefinition(String name, MyOwnInstallationDefinition other) {
        super(name, other)
    }

    @Override
    MyOwnInstallationDefinition clone(String name) {
        new MyOwnInstallationDefinition(name, this)
    }

    @Override
    String getProduct() {
        "test"
    }

    @Override
    String getVersion() {
        "1"
    }

    @Override
    File getHome() {
        (File) parent['workspace']
    }

    @Override
    boolean isInstalled() {
        false
    }

    @Override
    void registerTools(TaskRegistry registry) {
        // do nothing
    }

    @Override
    void install(Logger logger) {
        File location = new File(getHome(), "${product}-${version}")
        Spacelift.task(DownloadTool).from("https://github.com/arquillian/arquillian-selenium-bom/archive/master.zip").timeout(60000).to(location).execute().await()
        assertTrue location.exists()
    }
}
