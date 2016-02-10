package org.arquillian.spacelift.gradle

import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.gradle.utils.KillJavas
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class KillJavasTest {

    @Test
    void killJavas() {
        Project project = ProjectBuilder.builder().build()

        project.apply plugin: 'org.arquillian.spacelift'

        project.spacelift {
            tools {
            }
            profiles {
            }
            installations {
            }
            tests {
            }
        }

        // kill servers
        Spacelift.task(KillJavas).execute().await()
    }
}
