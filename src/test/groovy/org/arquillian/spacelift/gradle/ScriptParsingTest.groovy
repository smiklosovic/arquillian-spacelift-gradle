package org.arquillian.spacelift.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

public class ScriptParsingTest {

    @Test
    public void executeTestReport() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'org.arquillian.spacelift'

        project.ext.set("eap6", "true")

        project.spacelift {
            configuration {
                jbossHome {
                    type File
                    defaultValue 'hello'
                    description 'where the JBoss AS is located'
                }
            }
            tests {
                openshiftIntegrationTests {
                    execute {
                        println "openshiftIntegrationTests"
                    }
                }
                localIntegrationTests {
                    execute {
                        println "localIntegrationTests, jbossHome: $jbossHome"
                    }
                }
            }
            tools {
            }
            profiles {
                openshift {
                    enabledInstallations 'a', 'b'
                    tests 'openshiftIntegrationTests'
                }
                local {
                    tests 'localIntegrationTests'
                }
                eap6(inherits: local) {
                    enabledInstallations 'c', 'd'
                }

            }
            installations {
                a {}
                b {}
                c {}
                d {}
            }
        }

        project.extensions.add('profile', 'eap6')
        project.getTasks()['init'].execute()
        project.getTasks()['test'].execute()
    }
}
