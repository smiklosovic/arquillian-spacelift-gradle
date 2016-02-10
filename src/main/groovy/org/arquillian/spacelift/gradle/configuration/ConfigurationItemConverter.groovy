package org.arquillian.spacelift.gradle.configuration

/**
 * Converter has to be stateless.
 *
 * @param <CONVERTED_TYPE>
 */
interface ConfigurationItemConverter<CONVERTED_TYPE> {

    /**
     * Tries to convert the string value into the type.
     *
     * @param value String representation of the value.
     * @return converted string value to object of type CONVERTED_TYPE
     *
     * @throws ConversionException If the string is malformed
     */
    CONVERTED_TYPE fromString(String value) throws ConversionException

    /**
     * Converts the type instance into string. This method has to be consistent with {@link #fromString(java.lang.String)}
     * and calling:
     *
     * <pre>
     *     CONVERTED_TYPE instance1 = << instance of the type with some state >>;
     *     CONVERTED_TYPE instance2 = fromString(toString(instance1));
     * </pre>
     *
     * will result in instance2 having the same state as instance1.
     *
     * @param value
     * @return
     */
    String toString(CONVERTED_TYPE value)
}