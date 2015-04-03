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
package org.jboss.weld.ejb;

import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.injection.producer.AbstractInstantiator;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.injection.producer.DefaultInstantiator;
import org.jboss.weld.injection.producer.SubclassedComponentInstantiator;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.EjbSupport;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.SetMultimap;
import org.jboss.weld.util.reflection.Reflections;

class EjbSupportImpl implements EjbSupport {

    @Override
    public void cleanup() {
    }

    @Override
    public <T> BasicInjectionTarget<T> createSessionBeanInjectionTarget(EnhancedAnnotatedType<T> type, SessionBean<T> bean, BeanManagerImpl beanManager) {
        return SessionBeanInjectionTarget.of(type, bean, beanManager);
    }

    @Override
    public <T> BasicInjectionTarget<T> createMessageDrivenInjectionTarget(EnhancedAnnotatedType<T> type, InternalEjbDescriptor<T> descriptor, BeanManagerImpl manager) {
        EnhancedAnnotatedType<T> implementationClass = SessionBeans.getEjbImplementationClass(descriptor, manager, type);

        AbstractInstantiator<T> instantiator = null;
        if (type.equals(implementationClass)) {
            instantiator = new DefaultInstantiator<T>(type, null, manager);
        } else {
            // Session bean subclassed by the EJB container
            instantiator = SubclassedComponentInstantiator.forSubclassedEjb(type, implementationClass, null, manager);
        }
        return BasicInjectionTarget.createDefault(type, null, manager, instantiator);
    }

    @Override
    public <T> BeanAttributes<T> createSessionBeanAttributes(EnhancedAnnotatedType<T> annotated, InternalEjbDescriptor<?> descriptor, BeanManagerImpl manager) {
        return SessionBeans.createBeanAttributes(annotated, descriptor, manager);
    }

    @Override
    public void createSessionBeans(BeanDeployerEnvironment environment, SetMultimap<Class<?>, SlimAnnotatedType<?>> types, BeanManagerImpl manager) {
        final ClassTransformer transformer = manager.getServices().get(ClassTransformer.class);

        for (InternalEjbDescriptor<?> ejbDescriptor : environment.getEjbDescriptors()) {
            if (environment.isVetoed(ejbDescriptor.getBeanClass()) || Beans.isVetoed(ejbDescriptor.getBeanClass())) {
                continue;
            }
            if (ejbDescriptor.isSingleton() || ejbDescriptor.isStateful() || ejbDescriptor.isStateless()) {
                Set<SlimAnnotatedType<?>> classes = types.get(ejbDescriptor.getBeanClass());
                if (!classes.isEmpty()) {
                    for (SlimAnnotatedType<?> annotatedType : classes) {
                        createSessionBean(ejbDescriptor, annotatedType, environment, manager, transformer);
                    }
                } else {
                    createSessionBean(ejbDescriptor, environment, manager, transformer);
                }
            }
        }
    }

    private <T> SessionBean<T> createSessionBean(InternalEjbDescriptor<?> descriptor, SlimAnnotatedType<T> slimType, BeanDeployerEnvironment environment, BeanManagerImpl manager, ClassTransformer transformer) {
        // TODO Don't create enterprise bean if it has no local interfaces!
        final EnhancedAnnotatedType<T> type = transformer.getEnhancedAnnotatedType(slimType);
        final BeanAttributes<T> attributes = createSessionBeanAttributes(type, descriptor, manager);
        final SessionBean<T> bean = SessionBeanImpl.of(attributes, Reflections.<InternalEjbDescriptor<T>>cast(descriptor), manager, type);
        environment.addSessionBean(bean);
        return bean;
    }

    protected <T> SessionBean<T> createSessionBean(InternalEjbDescriptor<T> descriptor, BeanDeployerEnvironment environment, BeanManagerImpl manager, ClassTransformer transformer) {
        final SlimAnnotatedType<T> type = transformer.getBackedAnnotatedType(descriptor.getBeanClass(), manager.getId());
        manager.getServices().get(SlimAnnotatedTypeStore.class).put(type);
        return createSessionBean(descriptor, type, environment, manager, transformer);
    }

    @Override
    public void createNewSessionBeans(BeanDeployerEnvironment environment, BeanManagerImpl manager) {
        final SlimAnnotatedTypeStore store = manager.getServices().get(SlimAnnotatedTypeStore.class);
        for (Entry<InternalEjbDescriptor<?>, EnhancedAnnotatedType<?>> entry : environment.getNewSessionBeanDescriptorsFromInjectionPoint().entrySet()) {
            InternalEjbDescriptor<?> descriptor = entry.getKey();
            environment.addSessionBean(createNewSessionBean(entry.getValue(), descriptor, manager, store));
        }
    }

    private <T> SessionBean<T> createNewSessionBean(EnhancedAnnotatedType<?> type, InternalEjbDescriptor<T> ejbDescriptor, BeanManagerImpl beanManager, SlimAnnotatedTypeStore store) {
        store.put(type.slim());
        final BeanAttributes<T> attributes = Reflections.cast(SessionBeans.createBeanAttributesForNew(type, ejbDescriptor, beanManager, type.getJavaClass()));
        return NewSessionBean.of(attributes, ejbDescriptor, beanManager);
    }
}
