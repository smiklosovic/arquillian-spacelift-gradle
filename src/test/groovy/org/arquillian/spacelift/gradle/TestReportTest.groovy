package org.arquillian.spacelift.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class TestReportTest {

    @Test
    public void executeTestReport() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'org.arquillian.spacelift'

        project.repositories {
            mavenCentral()
        }

        project.configurations {
            junitreport
        }

        project.dependencies {
            junitreport 'org.apache.ant:ant-junit:1.9.4'
        }

        project.spacelift {
            tests {
            }
            tools {
            }
            profiles {
            }
            installations {
            }
        }

        // execute testreport task
        project.getTasks()['testreport'].execute()
    }
}
