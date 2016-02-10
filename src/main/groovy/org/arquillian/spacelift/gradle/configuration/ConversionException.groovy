package org.arquillian.spacelift.gradle.configuration;

/**
 * Exception which gets thrown once the conversion is not possible.
 */
class ConversionException extends RuntimeException {

    public ConversionException() {
    }

    public ConversionException(String message) {
        super(message)
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause)
    }

    public ConversionException(Throwable cause) {
        super(cause)
    }
}
