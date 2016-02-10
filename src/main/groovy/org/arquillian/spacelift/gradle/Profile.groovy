package org.arquillian.spacelift.gradle

import groovy.transform.CompileStatic
import org.arquillian.spacelift.gradle.configuration.ConfigurationContainer

// this class represents a profile enumerating installations to be installed

@CompileStatic
class Profile extends BaseContainerizableObject<Profile> implements ContainerizableObject<Profile> {

    ConfigurationContainer configuration

    // list of enabled installations
    DeferredValue<List> enabledInstallations = DeferredValue.of(List.class).from([])

    // list of tests to execute
    DeferredValue<List> tests = DeferredValue.of(List.class).from([])

    // list of tests to exclude
    DeferredValue<List> excludedTests = DeferredValue.of(List.class).from([])

    Profile(String profileName, Object parent) {
        super(profileName, parent)

        configuration = new ConfigurationContainer(this)
    }

    /**
     * Cloning constructor. Preserves lazy nature of closures to be evaluated later on.
     * @param other Profile to be cloned
     */
    Profile(String profileName, Profile other) {
        super(profileName, other)

        // use direct access to skip call of getter
        this.configuration = other.@configuration.clone() as ConfigurationContainer
        this.enabledInstallations = other.@enabledInstallations.copy()
        this.tests = other.@tests.copy()
        this.excludedTests = other.@excludedTests.copy()
    }

    @Override
    Profile clone(String name) {
        new Profile(name, this)
    }

    List<String> getEnabledInstallations() {
        enabledInstallations.resolve()
    }

    List<String> getTests() {
        tests.resolve()
    }

    List<String> getExcludedTests() {
        excludedTests.resolve()
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder()
        sb.append("Profile: ").append(name).append("\n")

        sb.append("\tConfiguration: \n${configuration.toString()}")

        // installations
        sb.append("\tInstallations: ")
        getEnabledInstallations().each {
            sb.append(it).append(" ")
        }
        sb.append("\n")

        sb.append("\tTests: ")
        getTests().each {
            sb.append(it).append(" ")
        }

        sb.append("\tExcluded tests: ")
        getExcludedTests().each {
            sb.append(it).append(" ")
        }

        sb.toString()
    }
}
