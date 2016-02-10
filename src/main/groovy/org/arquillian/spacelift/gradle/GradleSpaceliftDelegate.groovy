package org.arquillian.spacelift.gradle

import org.arquillian.spacelift.process.ProcessInteraction
import org.arquillian.spacelift.process.ProcessInteractionBuilder
import org.gradle.api.Project

// FIXME this is temporary delegate object that should be removed
class GradleSpaceliftDelegate {

    static final ProcessInteraction ECHO_OUTPUT = new ProcessInteractionBuilder()
            .outputPrefix("")
            .when("(?s).*")
            .printToOut()
            .build()

    private static final class ProjectHolder {
        private static Project project;
    }

    public static void currentProject(Project project) {
        ProjectHolder.project = project;
    }

    Project project() {
        if (ProjectHolder.project == null) {
            throw new IllegalStateException("Current project was not set via plugin.")
        }

        ProjectHolder.project
    }
}
