package org.arquillian.spacelift.gradle.configuration;

class IllegalConfigurationException extends IllegalStateException {

    IllegalConfigurationException() {
    }

    IllegalConfigurationException(String s) {
        super(s);
    }

    IllegalConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    IllegalConfigurationException(Throwable cause) {
        super(cause);
    }

    static IllegalConfigurationException incompatibleOverride(ConfigurationItem<?> mainItem, ConfigurationItem<?> profileItem) {
        new IllegalConfigurationException("Profile configuration tried to override `${mainItem.name}`, but " +
                "type `${mainItem.type.resolve()}` is not assignable from `${profileItem.type.resolve()}`");
    }

}
