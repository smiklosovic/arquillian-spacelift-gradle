package org.arquillian.spacelift.gradle

import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.gradle.arquillian.ArquillianXmlUpdater
import org.arquillian.spacelift.process.CommandBuilder
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat

class ClosureEvaluationTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    void "closure local bindings"() {

        def project = defineProject(
                installations: {
                    foo {
                        product "test"
                        version "1"
                        fileName "index.html"
                        autoExtract false
                        remoteUrl "http://www.google.com"
                        postActions {
                            assertThat project, is(notNullValue())
                            assertThat project.spacelift, is(notNullValue())
                            assertThat project.spacelift.cacheDir, is(notNullValue())
                            assertThat fsPath, is(new File(project.spacelift.cacheDir, "test/1/index.html"))
                            assertThat Spacelift.task('java'), is(notNullValue())
                            assertThat Spacelift.task(ArquillianXmlUpdater), is(notNullValue())
                        }
                    }
                },
                tools: {
                    java {
                        command {
                            assertThat project, is(notNullValue())
                            assertThat project.spacelift, is(notNullValue())
                            return new CommandBuilder("java")
                        }
                    }
                    java2 {
                        command {
                            assertThat project, is(notNullValue())
                            assertThat project.rootDir, is(notNullValue())
                            return new CommandBuilder("${project.rootDir}/java")
                        }
                    }
                },
                tests: {
                    bar {
                        execute {
                            assertThat project, is(notNullValue())
                            assertThat project.spacelift, is(notNullValue())
                            assertThat project.spacelift.tools, is(notNullValue())
                            assertThat project.spacelift.installations, is(notNullValue())
                            project.spacelift.tools.each { tool ->
                                org.junit.Assert.assertThat tool.name, containsString("java")
                            }
                        }
                    }
                })

        project.spacelift.tests.each { test ->
            test.executeTest(project.logger)
        }

        def java2Tool = Spacelift.task("java2")
        assertThat java2Tool, is(notNullValue())
    }

    @Test
    void "ant closure in test"() {

        def project = defineProject(tests: {
            bar {
                execute {
                    assertThat project, is(notNullValue())
                    project.ant.mkdir(dir: "${project.spacelift.workspace}/ant-closure-test")
                    project.ant.copy(todir: "${project.spacelift.workspace}/ant-closure-test", verbose: true) {
                        fileset(file: "${System.getProperty('user.dir')}/build.gradle")
                    }

                    File copiedFile = new File(project.spacelift.workspace, "ant-closure-test/build.gradle")
                    assertThat copiedFile.exists(), is(true)
                }
            }
        })

        project.spacelift.tests.each { test ->
            test.executeTest(project.logger)
        }
    }

    @Test
    void "closure extract mapper bindings"() {
        defineProject(installations: {
            foo {
                product "test"
                version "1"
                fileName "arquillian-selenium-bom-master.zip"
                remoteUrl "https://github.com/arquillian/arquillian-selenium-bom/archive/master.zip"
                home { "arquillian-selenium-${propagatedProperty}" }
                extractMapper {
                    remap("arquillian-selenium-bom-master/*").with("arquillian-selenium-${propagatedProperty}/*")
                }
                postActions {
                    assertThat project, is(notNullValue())
                    assertThat project.spacelift, is(notNullValue())
                    assertThat project.spacelift.cacheDir, is(notNullValue())
                    assertThat "${home}".toString(), containsString("${project.spacelift.workspace}/arquillian-selenium-${propagatedProperty}")
                }
            }
        })
    }

    @Test
    void "indirect installation reference"() {
        defineProject(installations: {
            fooReferenced {
                product "test"
                version "1"
            }
            fooIndirect {
                product "test"
                version "1"
                postActions {
                    assertThat project.spacelift.installations['fooReferenced'], is(notNullValue())
                }
            }
        })
    }

    @Test
    void "property syntax installation reference"() {
        defineProject(
                profiles: {
                    fooInvalid {}
                },
                installations: {
                    fooReferenced {
                        product "test"
                        version "1"
                    }
                    fooIndirect {
                        product "test"
                        version "1"
                        postActions {
                            // FIXME, this is now working but question is whether this call is safe
                            assertThat project.spacelift.installations.fooReferenced, is(notNullValue())
                            println project.spacelift.installations.fooReferenced
                        }
                    }
                })
    }

    @Test
    void "direct installation reference"() {
        defineProject(
                installations: {
                    fooReferenced {
                        product "test"
                        version "1"
                    }
                    fooIndirect {
                        product "test"
                        version "1"
                        postActions {
                            assertThat fooReferenced.version, is(notNullValue())
                        }
                    }
                })
    }

    @Test
    void "ambiguous installation reference"() {
        defineProject(
                profiles: {
                    fooReferenced {}
                },
                installations: {
                    fooReferenced {
                        product "test"
                        version "1"
                    }
                    fooIndirect {
                        product "test"
                        version "1"
                        postActions {
                            assertThat fooReferenced.version, is(notNullValue())
                            assertEquals(DefaultInstallation.class, fooReferenced.class)
                        }
                    }
                })
    }

    private Project defineProject(Map closures) {

        def profilesClosure = closures.get("profiles", {})
        def installationsClosure = closures.get("installations", {})
        def toolsClosure = closures.get("tools", {})
        def testsClosure = closures.get("tests", {})

        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'org.arquillian.spacelift'

        project.spacelift {
            configuration {
                propagatedProperty {
                    defaultValue 10
                }
            }
            profiles profilesClosure
            installations installationsClosure
            tools toolsClosure
            tests testsClosure
        }

        project.spacelift.installations.each { installation ->
            SpaceliftPlugin.installInstallation(installation, project.logger)
        }

        project
    }
}
