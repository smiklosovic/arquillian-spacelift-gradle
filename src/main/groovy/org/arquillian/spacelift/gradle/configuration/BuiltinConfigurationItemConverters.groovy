package org.arquillian.spacelift.gradle.configuration

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive

/**
 * Created by tkriz on 25/03/15.
 */
class BuiltinConfigurationItemConverters {

    private static final Map<Class<?>, ConfigurationItemConverter<?>> BUILTIN_CONVERTERS =
            new HashMap<Class<?>, ConfigurationItemConverter<?>>() {
                {

                    put(Boolean, new SimpleConfigurationItemConverter<Boolean>() {
                        @Override
                        Boolean fromString(String value) throws ConversionException {
                            Boolean.valueOf(value)
                        }
                    })

                    put(Byte, new SimpleConfigurationItemConverter<Byte>() {
                        @Override
                        Byte fromString(String value) throws ConversionException {
                            try {
                                Byte.valueOf(value)
                            } catch (Exception ex) {
                                throw new ConversionException("Unable to convert $value to Byte.", ex)
                            }
                        }
                    })

                    put(Short, new SimpleConfigurationItemConverter<Short>() {
                        @Override
                        Short fromString(String value) throws ConversionException {
                            try {
                                Short.valueOf(value)
                            } catch (Exception ex) {
                                throw new ConversionException("Unable to convert $value to Short.", ex)
                            }
                        }
                    })

                    put(Integer, new SimpleConfigurationItemConverter<Integer>() {
                        @Override
                        Integer fromString(String value) throws ConversionException {
                            try {
                                Integer.valueOf(value)
                            } catch (Exception ex) {
                                throw new ConversionException("Unable to convert $value to Integer.", ex)
                            }
                        }
                    })

                    put(Long, new SimpleConfigurationItemConverter<Long>() {
                        @Override
                        Long fromString(String value) throws ConversionException {
                            try {
                                Long.valueOf(value)
                            } catch (Exception ex) {
                                throw new ConversionException("Unable to convert $value to Long.", ex)
                            }
                        }
                    })

                    put(CharSequence, new SimpleConfigurationItemConverter<CharSequence>() {
                        @Override
                        CharSequence fromString(String value) throws ConversionException {
                            value
                        }
                    })

                    put(String, new StringConfigurationItemConverter())
                    put(Class, new ClassConfigurationItemConverter())
                    put(File, new FileConfigurationItemConverter())
                }
            }

    static <T> ConfigurationItemConverter<T> getConverter(Class<T> type) {
        if (type.isArray()) {
            ConfigurationItemConverter<?> itemConverter = getConverter(type.getComponentType())
            return new ArrayConfigurationItemConverter<?>(itemConverter) as ConfigurationItemConverter<T>
        }

        ConfigurationItemConverter<T> converter = BUILTIN_CONVERTERS.get(type) as ConfigurationItemConverter<T>

        if (!converter) {
            throw new RuntimeException("No suitable converter found for type: $type.")
        }

        converter
    }

    static class ArrayConfigurationItemConverter<CONVERTED_TYPE> implements ConfigurationItemConverter<CONVERTED_TYPE[]> {

        private final ConfigurationItemConverter<CONVERTED_TYPE> valueConverter

        ArrayConfigurationItemConverter(ConfigurationItemConverter<CONVERTED_TYPE> valueConverter) {
            this.valueConverter = valueConverter
        }

        @Override
        CONVERTED_TYPE[] fromString(String value) throws ConversionException {
            def array = new JsonParser().parse(value).getAsJsonArray()
            def output = []
            array.each {
                output.add(valueConverter.fromString(it.getAsString()))
            }

            output.toArray() as CONVERTED_TYPE[]
        }

        @Override
        String toString(CONVERTED_TYPE[] value) {
            def jsonArray = new JsonArray()

            value.each { CONVERTED_TYPE element ->
                jsonArray.add(new JsonPrimitive(valueConverter.toString(element)))
            }

            jsonArray.toString()
        }
    }

    static abstract class SimpleConfigurationItemConverter<CONVERTED_TYPE>
            implements ConfigurationItemConverter<CONVERTED_TYPE> {

        @Override
        String toString(CONVERTED_TYPE value) {
            value.toString()
        }
    }

    static class StringConfigurationItemConverter implements ConfigurationItemConverter<String> {

        @Override
        String fromString(String value) throws ConversionException {
            value
        }

        @Override
        String toString(String value) {
            value
        }
    }

    static class ClassConfigurationItemConverter implements ConfigurationItemConverter<Class<?>> {

        @Override
        Class<?> fromString(String value) throws ConversionException {
            try {
                Class.forName(value)
            } catch (Exception ex) {
                throw new ConversionException(String.format("Unable to convert $value to Class"), ex)
            }
        }

        @Override
        String toString(Class<?> value) {
            value.getName()
        }
    }

    static class FileConfigurationItemConverter implements ConfigurationItemConverter<File> {

        @Override
        File fromString(String value) throws ConversionException {
            try {
                new File(value)
            } catch (Exception ex) {
                throw new ConversionException(String.format("Unable to convert $value to File."), ex)
            }
        }

        @Override
        String toString(File value) {
            value.path
        }
    }
}
