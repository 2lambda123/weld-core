/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.util.Observers.validateObserverMethod;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.DefinitionException;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.CustomDecoratorWrapper;
import org.jboss.weld.bean.attributes.ExternalBeanAttributesFactory;
import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.ContextLogger;
import org.jboss.weld.logging.InterceptorLogger;
import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Bindings;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.Preconditions;

public class AfterBeanDiscoveryImpl extends AbstractBeanDiscoveryEvent implements AfterBeanDiscovery {

    private static final String TYPE_ARGUMENT_NAME = "type";

    public static void fire(BeanManagerImpl beanManager, Deployment deployment, BeanDeploymentArchiveMapping bdaMapping, Collection<ContextHolder<? extends Context>> contexts) {
        final AfterBeanDiscoveryImpl event = new AfterBeanDiscoveryImpl(beanManager, deployment, bdaMapping, contexts);
        event.fire();
        event.finish();
    }

    private AfterBeanDiscoveryImpl(BeanManagerImpl beanManager, Deployment deployment, BeanDeploymentArchiveMapping bdaMapping, Collection<ContextHolder<? extends Context>> contexts) {
        super(beanManager, AfterBeanDiscovery.class, bdaMapping, deployment, contexts);
        this.slimAnnotatedTypeStore = beanManager.getServices().get(SlimAnnotatedTypeStore.class);
        this.containerLifecycleEvents = beanManager.getServices().get(ContainerLifecycleEvents.class);
    }

    private final SlimAnnotatedTypeStore slimAnnotatedTypeStore;
    private final ContainerLifecycleEvents containerLifecycleEvents;
    private final List<Bean<?>> additionalBeans = new LinkedList<Bean<?>>();
    private final List<ObserverMethod<?>> additionalObservers = new LinkedList<ObserverMethod<?>>();

    @Override
    public void addBean(Bean<?> bean) {
        checkWithinObserverNotification();
        Preconditions.checkArgumentNotNull(bean, "bean");
        ExternalBeanAttributesFactory.validateBeanAttributes(bean, getBeanManager());
        validateBean(bean);
        additionalBeans.add(bean);
    }

    protected <T> void processBean(Bean<T> bean) {
        BeanManagerImpl beanManager = getOrCreateBeanDeployment(bean.getBeanClass()).getBeanManager();
        if (bean instanceof Interceptor<?>) {
            beanManager.addInterceptor((Interceptor<?>) bean);
        } else if (bean instanceof Decorator<?>) {
            beanManager.addDecorator(CustomDecoratorWrapper.of((Decorator<?>) bean, beanManager));
        } else {
            beanManager.addBean(bean);
        }
        containerLifecycleEvents.fireProcessBean(beanManager, bean);
    }

    private void validateBean(Bean<?> bean) {
        if (bean.getBeanClass() == null) {
            throw BeanLogger.LOG.beanMethodReturnsNull("getBeanClass", bean);
        }
        if (bean.getInjectionPoints() == null) {
            throw BeanLogger.LOG.beanMethodReturnsNull("getInjectionPoints", bean);
        }
        if (bean instanceof PassivationCapable) {
            PassivationCapable passivationCapable = (PassivationCapable) bean;
            if (passivationCapable.getId() == null) {
                throw BeanLogger.LOG.passivationCapableBeanHasNullId(bean);
            }
        }
        if (bean instanceof Interceptor<?>) {
            validateInterceptor((Interceptor<?>) bean);
        } else if (bean instanceof Decorator<?>) {
            validateDecorator((Decorator<?>) bean);
        }
    }

    private void validateInterceptor(Interceptor<?> interceptor) {
        Set<Annotation> bindings = interceptor.getInterceptorBindings();
        if (bindings == null) {
            throw InterceptorLogger.LOG.nullInterceptorBindings(interceptor);
        }
        for (Annotation annotation : bindings) {
            if (!getBeanManager().isInterceptorBinding(annotation.annotationType())) {
                throw MetadataLogger.LOG.notAnInterceptorBinding(annotation, interceptor);
            }
        }
    }

    private void validateDecorator(Decorator<?> decorator) {
        Set<Annotation> qualifiers = decorator.getDelegateQualifiers();
        if (decorator.getDelegateType() == null) {
            throw BeanLogger.LOG.decoratorMethodReturnsNull("getDelegateType", decorator);
        }
        Bindings.validateQualifiers(qualifiers, getBeanManager(), decorator, "Decorator.getDelegateQualifiers");
        if (decorator.getDecoratedTypes() == null) {
            throw BeanLogger.LOG.decoratorMethodReturnsNull("getDecoratedTypes", decorator);
        }

    }

    @Override
    public void addContext(Context context) {
        checkWithinObserverNotification();
        Preconditions.checkArgumentNotNull(context, "context");
        Class<? extends Annotation> scope = context.getScope();
        if (scope == null) {
            throw ContextLogger.LOG.contextHasNullScope(context);
        }
        // TODO: re-enable this once Mojarra is fixed - https://java.net/jira/browse/JAVASERVERFACES-3240
        //  if (!getBeanManager().isScope(scope)) {
        //      throw MetadataLogger.LOG.contextGetScopeIsNotAScope(scope, context);
        //  }
        getBeanManager().addContext(context);
    }

    @Override
    public void addObserverMethod(ObserverMethod<?> observerMethod) {
        checkWithinObserverNotification();
        Preconditions.checkArgumentNotNull(observerMethod, "observerMethod");
        validateObserverMethod(observerMethod, getBeanManager(), null);
        additionalObservers.add(observerMethod);
    }

    @Override
    public <T> AnnotatedType<T> getAnnotatedType(Class<T> type, String id) {
        checkWithinObserverNotification();
        Preconditions.checkArgumentNotNull(type, TYPE_ARGUMENT_NAME);
        return slimAnnotatedTypeStore.get(type, id);
    }

    @Override
    public <T> Iterable<AnnotatedType<T>> getAnnotatedTypes(Class<T> type) {
        checkWithinObserverNotification();
        Preconditions.checkArgumentNotNull(type, TYPE_ARGUMENT_NAME);
        return cast(slimAnnotatedTypeStore.get(type));
    }

    /**
     * Bean and observer registration is delayed until after all {@link AfterBeanDiscovery} observers are notified.
     */
    private void finish() {
        try {
            for (Bean<?> bean : additionalBeans) {
                processBean(bean);
            }
            for (ObserverMethod<?> observer : additionalObservers) {
                BeanManagerImpl manager = getOrCreateBeanDeployment(observer.getBeanClass()).getBeanManager();
                if (Observers.isObserverMethodEnabled(observer, manager)) {
                    ObserverMethod<?> processedObserver = containerLifecycleEvents.fireProcessObserverMethod(manager, observer);
                    if (processedObserver != null) {
                        manager.addObserver(processedObserver);
                    }
                }
            }
        } catch (Exception e) {
            throw new DefinitionException(e);
        }
    }
}
