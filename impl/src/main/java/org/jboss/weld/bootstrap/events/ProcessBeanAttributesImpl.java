/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Container lifecycle event that allows bean metadata ({@link BeanAttributes}) to be changed before the bean is registered.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the type of bean
 */
public class ProcessBeanAttributesImpl<T> extends AbstractDefinitionContainerEvent implements ProcessBeanAttributes<T> {

    protected static <T> ProcessBeanAttributesImpl<T> fire(BeanManagerImpl beanManager, BeanAttributes<T> attributes, Annotated annotated, Type type) {
        ProcessBeanAttributesImpl<T> event = new ProcessBeanAttributesImpl<T>(beanManager, attributes, annotated, type) {
        };
        event.fire();
        return event;
    }

    private ProcessBeanAttributesImpl(BeanManagerImpl beanManager, BeanAttributes<T> attributes, Annotated annotated, Type type) {
        super(beanManager, ProcessBeanAttributes.class, new Type[] { type });
        this.attributes = attributes;
        this.annotated = annotated;
    }

    private final Annotated annotated;
    private BeanAttributes<T> attributes;
    private boolean veto;
    private boolean dirty;

    @Override
    public Annotated getAnnotated() {
        return annotated;
    }

    @Override
    public BeanAttributes<T> getBeanAttributes() {
        return attributes;
    }

    @Override
    public void setBeanAttributes(BeanAttributes<T> beanAttributes) {
        attributes = beanAttributes;
        dirty = true;
    }

    @Override
    public void addDefinitionError(Throwable t) {
        getErrors().add(t);
    }

    @Override
    public void veto() {
        veto = true;
    }

    public boolean isVeto() {
        return veto;
    }

    public boolean isDirty() {
        return dirty;
    }
}
