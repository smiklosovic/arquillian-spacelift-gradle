package org.arquillian.spacelift.gradle.gradle

import groovy.transform.CompileStatic
import org.apache.commons.lang3.SystemUtils
import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.execution.ExecutionService
import org.arquillian.spacelift.gradle.BaseContainerizableObject
import org.arquillian.spacelift.gradle.DeferredValue
import org.arquillian.spacelift.gradle.GradleSpaceliftDelegate
import org.arquillian.spacelift.gradle.Installation
import org.arquillian.spacelift.process.CommandBuilder
import org.arquillian.spacelift.process.ProcessResult
import org.arquillian.spacelift.task.Task
import org.arquillian.spacelift.task.TaskFactory
import org.arquillian.spacelift.task.TaskRegistry
import org.arquillian.spacelift.task.archive.UnzipTool
import org.arquillian.spacelift.task.net.DownloadTool
import org.arquillian.spacelift.task.os.CommandTool
import org.slf4j.Logger

@CompileStatic
class GradleInstallation extends BaseContainerizableObject<GradleInstallation> implements Installation {

    DeferredValue<String> product = DeferredValue.of(String).from("gradle")

    DeferredValue<String> version = DeferredValue.of(String).from("2.9")

    DeferredValue<Boolean> isInstalled = DeferredValue.of(Boolean).from({ getHome().exists() })

    DeferredValue<File> home = DeferredValue.of(File).from("gradle")

    DeferredValue<Void> postActions = DeferredValue.of(Void)

    DeferredValue<String> remoteUrl = DeferredValue.of(String).from({
        "https://services.gradle.org/distributions/gradle-${getVersion()}-bin.zip"
    })

    DeferredValue<String> fileName = DeferredValue.of(String).from({ "gradle-${getVersion()}-bin.zip" })

    DeferredValue<String> alias = DeferredValue.of(String).from("gradle")

    GradleInstallation(String name, Object parent) {
        super(name, parent)
    }

    GradleInstallation(String name, GradleInstallation other) {
        super(name, other)

        this.product = other.@product.copy()
        this.version = other.@version.copy()
        this.isInstalled = other.@isInstalled.copy()
        this.home = other.@home.copy()
        this.postActions = other.@postActions.copy()
        this.remoteUrl = other.@remoteUrl.copy()
        this.fileName = other.@fileName.copy()
        this.alias = other.@alias.copy()
    }

    @Override
    GradleInstallation clone(String name) {
        new GradleInstallation(name, this)
    }

    @Override
    String getProduct() {
        product.resolve()
    }

    @Override
    String getVersion() {
        version.resolve()
    }

    @Override
    boolean isInstalled() {
        isInstalled.resolve()
    }

    @Override
    File getHome() {
        new File(home.resolve(), "gradle-" + getVersion())
    }

    String getRemoteUrl() {
        remoteUrl.resolve()
    }

    String getFileName() {
        fileName.resolve()
    }

    String getAlias() {
        alias.resolve()
    }

    @Override
    void install(Logger logger) {

        File targetFile = getFsPath()

        if (targetFile.exists()) {
            logger.info(":install:${name} Grabbing ${getFileName()} from file system cache")
        } else if (getRemoteUrl() != null) {
            // ensure parent directory exists
            targetFile.getParentFile().mkdirs()

            // dowload bits if they do not exists
            logger.info(":install:${name} Grabbing from ${getRemoteUrl()}, storing at ${targetFile}")
            Spacelift.task(DownloadTool).from(getRemoteUrl()).timeout(120000).to(targetFile).execute().await()
        }

        logger.info(":install:${name} Extracting installation from ${getFileName()}")
        Spacelift.task(getFsPath(), UnzipTool).toDir(((File) getHome()).parentFile.canonicalFile).execute().await()

        new GradleSpaceliftDelegate().project().getAnt().invokeMethod("chmod", [dir: "${getHome()}/bin", perm: "a+x", includes: "*"])
    }

    @Override
    void registerTools(TaskRegistry registry) {

        registry.register(GradleTool, new TaskFactory() {
            GradleTool create() {
                Task task = new GradleTool(getHome())
                ((GradleTool) task).setExecutionService(Spacelift.service())
                task
            }

            Collection aliases() { ["gradle"] }
        })
    }

    private File getFsPath() {
        new File((File) parent['cacheDir'], "${getProduct()}/${getVersion()}/${getFileName()}")
    }

    class GradleTool extends CommandTool {

        private String home

        static final Map osMapping = [
                windows: { return SystemUtils.IS_OS_WINDOWS },
                mac    : { return SystemUtils.IS_OS_MAC_OSX },
                linux  : { return SystemUtils.IS_OS_LINUX },
        ]

        Map<String, List<String>> nativeCommand = [
                windows: [ "${getPlatformSpecificHome(home)}/bin/gradle" ],
                linux  : ["${home}/bin/gradle"],
                mac    : ["${home}/bin/gradle"],
        ]

        GradleTool(File home) {
            super()

            this.home = home.canonicalPath

            List command = resolveNativeCommand(nativeCommand)
            this.commandBuilder = new CommandBuilder(command as CharSequence[])
            this.interaction = GradleSpaceliftDelegate.ECHO_OUTPUT

            super.environment.putAll(getGradleEnvironmentProperties())
        }

        public Task<Object, ProcessResult> setExecutionService(ExecutionService executionService) {
            return super.setExecutionService(executionService)
        }

        private List<String> resolveNativeCommand(Map nativeCommand) {
            for (Map.Entry<String, Closure> platformEntry : osMapping.entrySet()) {
                if (platformEntry.getValue().call()) {
                    return (List<String>) nativeCommand[platformEntry.getKey()]
                }
            }
        }

        private Map<String, String> getGradleEnvironmentProperties() {
            Map<String, String> envProperties = new HashMap<String, String>()

            String gradleHome = getPlatformSpecificHome(home)

            envProperties.put("GRADLE_HOME", gradleHome)
            envProperties
        }

        @Override
        String toString() {
            "GradleTool (${commandBuilder})"
        }

        private def getPlatformSpecificHome(String home) {
            boolean isRunningOnCygwin = Boolean.parseBoolean(System.getProperty("runningOnCygwin"));

            if (!isRunningOnCygwin) {
                return home
            }

            String disk = extractWindowsDisk(home)
            String path = extractPath(home)

            return "/cygwin/" + disk + path
        }

        private def extractWindowsDisk(String home) {
            home.tokenize(":").get(0).toLowerCase()
        }

        private def extractPath(String home) {
            home.tokenize(":").get(1).replaceAll("\\\\", "/")
        }
    }
}
