package org.arquillian.spacelift.gradle.git

import groovy.transform.CompileStatic
import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.gradle.*
import org.arquillian.spacelift.task.DefaultGradleTask
import org.arquillian.spacelift.task.TaskRegistry
import org.slf4j.Logger

/**
 * Installs software from a git repository
 *
 * @author kpiwko
 */
@CompileStatic
class GitBasedInstallation extends BaseContainerizableObject<GitBasedInstallation> implements Installation {

    DeferredValue<String> product = DeferredValue.of(String).from("unused")

    DeferredValue<String> version = DeferredValue.of(String).from("unused")

    DeferredValue<String> repository = DeferredValue.of(String)

    DeferredValue<String> commit = DeferredValue.of(String).from("master")

    DeferredValue<Boolean> skipFetch = DeferredValue.of(Boolean).from(Boolean.FALSE)

    // represents directory where installation is extracted to
    DeferredValue<File> home = DeferredValue.of(File)

    DeferredValue<Boolean> isInstalled = DeferredValue.of(Boolean).from({

        File home = getHome()

        if (!home.exists()) {
            return false
        }

        if (skipFetch.resolve()) {
            return true
        }

        // get commit sha
        String commitSha = commit.resolve();

        // if we checked out a commit, this should work
        String repositorySha = Spacelift.task(home, GitRevParseTask).rev("HEAD").execute().await()
        if (repositorySha != null && repositorySha == commitSha) {
            return true
        }

        // if we checkout out master or a reference, make sure that we fetch latest first
        Spacelift.task(home, GitFetchTask).execute().await()

        def originRepositorySha = Spacelift.task(home, GitRevParseTask).rev("origin/${commitSha}").execute().await()
        // ensure that content is the same

        if (repositorySha != null && originRepositorySha != null && repositorySha == originRepositorySha) {
            return true
        }

        // not installed
        false
    })

    // actions to be invoked after installation is done
    DeferredValue<Void> postActions = DeferredValue.of(Void)

    // tools provided by this installation
    InheritanceAwareContainer<GradleTask, DefaultGradleTask> tools

    GitBasedInstallation(String name, Object parent) {
        super(name, parent)

        this.tools = new InheritanceAwareContainer(this, GradleTask, DefaultGradleTask)
    }

    GitBasedInstallation(String name, GitBasedInstallation other) {
        super(name, other)

        this.product = other.@product.copy()
        this.version = other.@product.copy()
        this.repository = other.@repository.copy()
        this.commit = other.@commit.copy()
        this.isInstalled = other.@isInstalled.copy()
        this.postActions = other.@postActions.copy()
        this.tools = (InheritanceAwareContainer<GradleTask, DefaultGradleTask>) other.@tools.clone()

    }

    @Override
    GitBasedInstallation clone(String name) {
        new GitBasedInstallation(name, this)
    }

    @Override
    String getVersion() {
        version.resolve()
    }

    @Override
    String getProduct() {
        product.resolve()
    }

    @Override
    File getHome() {
        home.resolve()
    }

    @Override
    public boolean isInstalled() {
        isInstalled.resolve()
    }

    @Override
    void install(Logger logger) {

        File home = getHome()
        String commitId = commit.resolve()

        // if home exist already, assume that we want just to update
        if (home.exists()) {
            logger.info(":install:${name} Identified existing git installation at ${home}, will fetch latest content")
            Spacelift.task(home, GitFetchTask).execute().await()

            // checkout commit
            logger.info(":install:${name} Force checking out ${commitId} at ${home}")
            Spacelift.task(home, GitCheckoutTask).checkout(commitId).force().execute().await()
        }
        // otherwise, clone the repository
        else {
            String repositoryPath = repository.resolve()
            logger.info(":install:${name} Cloning git repository from ${repositoryPath} to ${home}")
            home = Spacelift.task(repositoryPath, GitCloneTask).destination(home).execute().await()

            // checkout commit
            logger.info(":install:${name} Checking out ${commitId} at ${home}")
            Spacelift.task(home, GitCheckoutTask).checkout(commitId).execute().await()
        }

        // execute post actions
        postActions.resolve()

    }

    @Override
    void registerTools(TaskRegistry registry) {
        ((Iterable<GradleTask>) tools).each { GradleTask task ->
            Spacelift.registry().register(task.factory())
        }
    }
}
