/*
 * Copyright 2017 original authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.particleframework.core.serialize;

import org.particleframework.core.convert.ConversionService;
import org.particleframework.core.reflect.ClassUtils;
import org.particleframework.core.serialize.exceptions.SerializationException;

import java.io.*;
import java.util.Optional;

/**
 * A {@link ObjectSerializer} that uses JDK serialization
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class JdkSerializer implements ObjectSerializer {

    private final ConversionService<?> conversionService;

    public JdkSerializer(ConversionService<?> conversionService) {
        this.conversionService = conversionService;
    }

    public JdkSerializer() {
        this(ConversionService.SHARED);
    }

    @Override
    public void serialize(Object object, OutputStream outputStream) throws SerializationException {
        try {
            try(ObjectOutputStream objectOut = createObjectOutput(outputStream)) {
                objectOut.writeObject(object);
                objectOut.flush();
            }
        } catch (IOException e) {
            throw new SerializationException("I/O error occurred during serialization: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> Optional<T> deserialize(InputStream inputStream, Class<T> requiredType) throws SerializationException {
        try {
            try(ObjectInputStream objectIn = createObjectInput(inputStream, requiredType)) {
                try {
                    Object readObject = objectIn.readObject();

                    return conversionService.convert(readObject, requiredType);
                }
                catch(ClassCastException cce ) {
                    throw new SerializationException("Invalid type deserialized from stream: " + cce.getMessage(), cce);
                }
                catch (ClassNotFoundException e) {
                    throw new SerializationException("Type not found deserializing from stream: " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new SerializationException("I/O error occurred during deserialization: " + e.getMessage(), e);
        }
    }

    protected ObjectOutputStream createObjectOutput(OutputStream outputStream) throws IOException {
        return new ObjectOutputStream(outputStream);
    }

    protected ObjectInputStream createObjectInput(InputStream inputStream, Class<?> requiredType) throws IOException {
        return new ObjectInputStream(inputStream) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                Optional<Class> aClass = ClassUtils.forName(desc.getName(), requiredType.getClassLoader());
                if(aClass.isPresent()) {
                    return aClass.get();
                }
                else {
                    return super.resolveClass(desc);
                }
            }
        };
    }
}