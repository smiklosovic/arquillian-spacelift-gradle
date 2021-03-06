package org.arquillian.spacelift.gradle.git

import org.apache.commons.io.FileUtils
import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.execution.ExecutionException
import org.arquillian.spacelift.gradle.Installation
import org.arquillian.spacelift.gradle.SpaceliftPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.CoreMatchers.notNullValue
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

class GitBasedInstallationTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @After
    public void teardown() {
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir"), "absoluteRepositoryTest"))
    }

    @Test
    void "install from commit and master"() {
        Project testProject = ProjectBuilder.builder().build()

        testProject.apply plugin: 'org.arquillian.spacelift'

        testProject.spacelift {
            workspace { new File(System.getProperty("user.dir"), "workspace") }
            installations {

                def counter = 0

                theCommit(from: GitBasedInstallation) {
                    repository "https://github.com/smiklosovic/test.git"
                    commit '15b1d748935a58ea0f583a4d21531e854e6c382a'
                    home "smikloso-test"
                    postActions {
                        counter++
                    }
                }
                theMaster(from: GitBasedInstallation) {
                    repository "https://github.com/smiklosovic/test.git"
                    commit 'master'
                    home "smikloso-test"
                    postActions {
                        counter++
                    }
                }
                theCommitAgain(from: GitBasedInstallation) {
                    repository "https://github.com/smiklosovic/test.git"
                    commit '15b1d748935a58ea0f583a4d21531e854e6c382a'
                    home "smikloso-test"
                    postActions {
                        counter++
                    }
                }
                // we need to end up with different commit than we've started
                theMasterAgain(from: GitBasedInstallation) {
                    repository "https://github.com/smiklosovic/test.git"
                    commit 'master'
                    home "smikloso-test"
                    postActions {
                        counter++
                    }
                }
                absoluteRepositoryTest(from: GitBasedInstallation) {
                    repository "https://github.com/smiklosovic/test.git"
                    commit 'master'
                    home new File(System.getProperty("java.io.tmpdir"), "absoluteRepositoryTest").absolutePath
                    postActions {
                        assertThat counter, is(4)
                        File repositoryFile = new File(System.getProperty("java.io.tmpdir"), "absoluteRepositoryTest")
                        assertTrue repositoryFile.exists()
                    }
                }
            }
        }

        testProject.spacelift.installations.each { Installation installation ->
            assertThat installation.isInstalled(), is(false)
            SpaceliftPlugin.installInstallation(installation, testProject.logger)
            assertThat installation.isInstalled(), is(true)
        }
    }

    @Test
    void "register tools for non-existing installation"() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'org.arquillian.spacelift'

        project.spacelift {
            workspace { new File(System.getProperty("user.dir"), "workspace") }
            installations {

                def counter = 0

                installedInstallation(from: GitBasedInstallation) {
                    repository "https://github.com/smiklosovic/test.git"
                    commit 'master'
                    home "whatever"
                    isInstalled false
                    tools {
                        javaalias {
                            command "java"
                        }
                    }
                }
                nonInstalledInstallation(from: GitBasedInstallation) {
                    repository "https://github.com/smiklosovic/test.git"
                    commit 'master'
                    home "whatever2"
                    isInstalled true
                    tools {
                        javaalias2 {
                            command "java"
                        }
                    }
                }
            }
        }

        project.spacelift.installations.each { Installation installation ->
            SpaceliftPlugin.installInstallation(installation, project.logger)
        }

        assertThat Spacelift.task('javaalias'), is(notNullValue())
        assertThat Spacelift.task('javaalias2'), is(notNullValue())
    }

    @Test
    void "install from non-existing commit"() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'org.arquillian.spacelift'

        project.spacelift {
            workspace { new File(System.getProperty("user.dir"), "workspace") }
            installations {
                theCommit(from: GitBasedInstallation) {
                    repository "https://github.com/smiklosovic/test.git"
                    commit 'does-not-exist'
                    home "smikloso-test-should-fail"
                }
            }
        }

        exception.expect(ExecutionException)
        exception.expectMessage("checkout")

        // on purpose, we are not installing here as this installation will download zillion of data
        // from internet, just verify that previous manual definition installed the SDK and tools are
        // properly registered
        project.spacelift.installations.each { Installation installation ->
            assertThat installation.isInstalled(), is(false)
            SpaceliftPlugin.installInstallation(installation, project.logger)
            assertThat installation.isInstalled(), is(true)
        }
    }
}
