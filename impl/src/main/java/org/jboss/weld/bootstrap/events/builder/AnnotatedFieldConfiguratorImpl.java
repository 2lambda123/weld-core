/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.bootstrap.events.builder;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.builder.AnnotatedFieldConfigurator;

/**
 * Configurator for {@link AnnotatedField}.
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public class AnnotatedFieldConfiguratorImpl<T> extends AnnotatedConfigurator<T, AnnotatedField<T>, AnnotatedFieldConfiguratorImpl<T>>
        implements AnnotatedFieldConfigurator<T> {

    /**
     *
     * @param annotatedParam
     * @return
     */
    static <X> AnnotatedFieldConfiguratorImpl<X> from(AnnotatedField<X> annotatedField) {
        return new AnnotatedFieldConfiguratorImpl<>(annotatedField);
    }

    /**
     *
     * @param annotatedParam
     */
    private AnnotatedFieldConfiguratorImpl(AnnotatedField<T> annotatedField) {
        super(annotatedField);
    }

    @Override
    protected AnnotatedFieldConfiguratorImpl<T> self() {
        return this;
    }

}
