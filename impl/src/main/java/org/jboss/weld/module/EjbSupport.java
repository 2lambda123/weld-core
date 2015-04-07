/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.SetMultimap;

/**
 * This service provides EJB support. It is implemented by the weld-ejb module.
 *
 * @author Jozef Hartinger
 *
 */
public interface EjbSupport extends Service {

    /**
     * Creates a {@link BeanAttributes} object for a session bean from the given annotated type and ejb descriptor
     * @param type annotated type that defines the session bean
     * @param descriptor session bean descriptor
     * @param manager the bean manager
     * @return BeanAttributes representation of a given session bean
     */
    <T> BeanAttributes<T> createSessionBeanAttributes(EnhancedAnnotatedType<T> type, InternalEjbDescriptor<?> descriptor, BeanManagerImpl manager);

    /**
     * Creates an {@link InjectionTarget} implementation for a given session bean.
     * @param type annotated type that defines the session bean
     * @param descriptor session bean descriptor
     * @param manager the bean manager
     * @return InjectionTarget implementation for a given session bean
     */
    <T> BasicInjectionTarget<T> createSessionBeanInjectionTarget(EnhancedAnnotatedType<T> type, SessionBean<T> bean, BeanManagerImpl manager);

    /**
     * Creates an {@link InjectionTarget} implementation for a message-driven bean
     * @param type annotated type that defines the message-driven bean
     * @param descriptor message-driven bean descriptor
     * @param manager the bean manager
     * @return InjectionTarget implementation for a given message-driven bean
     */
    <T> BasicInjectionTarget<T> createMessageDrivenInjectionTarget(EnhancedAnnotatedType<T> type, InternalEjbDescriptor<T> descriptor, BeanManagerImpl manager);

    /**
     * Creates session beans and registers them within the given environment.
     * @param environment
     * @param classes
     * @param manager
     */
    void createSessionBeans(BeanDeployerEnvironment environment, SetMultimap<Class<?>, SlimAnnotatedType<?>> classes, BeanManagerImpl manager);

    /**
     * Creates {@link New} session beans and registers them within the given environment.
     * @param environment
     * @param classes
     * @param manager
     */
    void createNewSessionBeans(BeanDeployerEnvironment environment, BeanManagerImpl manager);

    /**
     * Returns the class object for the {@link javax.ejb.Timeout} annotation
     * @return the class object for the Timeout annotation or null if the annotation is not present
     */
    Class<? extends Annotation> getTimeoutAnnotation();

    EjbSupport NOOP_IMPLEMENTATION = new EjbSupport() {

        @Override
        public void cleanup() {
        }

        private <T> T fail() {
            throw new IllegalStateException("Cannot process session bean. weld-ejb module not registered.");
        }

        @Override
        public <T> BasicInjectionTarget<T> createSessionBeanInjectionTarget(EnhancedAnnotatedType<T> type, SessionBean<T> bean, BeanManagerImpl manager) {
            return fail();
        }

        @Override
        public <T> BeanAttributes<T> createSessionBeanAttributes(EnhancedAnnotatedType<T> type, InternalEjbDescriptor<?> descriptor, BeanManagerImpl manager) {
            return fail();
        }

        @Override
        public <T> BasicInjectionTarget<T> createMessageDrivenInjectionTarget(EnhancedAnnotatedType<T> type, InternalEjbDescriptor<T> descriptor,
                BeanManagerImpl manager) {
            return fail();
        }

        @Override
        public void createSessionBeans(BeanDeployerEnvironment environment, SetMultimap<Class<?>, SlimAnnotatedType<?>> classes, BeanManagerImpl manager) {
        }

        @Override
        public void createNewSessionBeans(BeanDeployerEnvironment environment, BeanManagerImpl manager) {
        }

        @Override
        public Class<? extends Annotation> getTimeoutAnnotation() {
            return null;
        }
    };
}
