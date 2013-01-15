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
package org.jboss.weld.bootstrap;

import static org.jboss.weld.logging.messages.ValidatorMessage.AMBIGUOUS_EL_NAME;
import static org.jboss.weld.logging.messages.ValidatorMessage.BEAN_NAME_IS_PREFIX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.executor.IterativeWorkerTaskFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.HashSetSupplier;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * Processes validation of beans, decorators and interceptors in parallel.
 *
 * @author Jozef Hartinger
 *
 */
public class ConcurrentValidator extends Validator {

    private final ExecutorServices executor;

    public ConcurrentValidator(ExecutorServices executor) {
        this.executor = executor;
    }

    @Override
    public void validateBeans(Collection<? extends Bean<?>> beans, final BeanManagerImpl manager) {
        final List<RuntimeException> problems = new CopyOnWriteArrayList<RuntimeException>();
        final Set<RIBean<?>> specializedBeans = Sets.newSetFromMap(new ConcurrentHashMap<RIBean<?>, Boolean>());

        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<Bean<?>>(beans) {
            protected void doWork(Bean<?> bean) {
                validateBean(bean, specializedBeans, manager, problems);
            }
        });

        if (!problems.isEmpty()) {
            if (problems.size() == 1) {
                throw problems.get(0);
            } else {
                throw new DeploymentException(problems);
            }
        }
    }

    @Override
    public void validateInterceptors(Collection<? extends Interceptor<?>> interceptors, final BeanManagerImpl manager) {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<Interceptor<?>>(interceptors) {
            protected void doWork(Interceptor<?> interceptor) {
                validateInterceptor(interceptor, manager);
            }
        });
    }

    @Override
    public void validateDecorators(Collection<? extends Decorator<?>> decorators, final BeanManagerImpl manager) {
        final Set<RIBean<?>> specializedBeans = Sets.newSetFromMap(new ConcurrentHashMap<RIBean<?>, Boolean>());

        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<Decorator<?>>(decorators) {
            protected void doWork(Decorator<?> decorator) {
                validateDecorator(decorator, specializedBeans, manager);
            }
        });
    }

    @Override
    protected void validateObserverMethods(Iterable<ObserverInitializationContext<?, ?>> observers, final BeanManagerImpl beanManager) {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<ObserverInitializationContext<?, ?>>(observers) {
            protected void doWork(ObserverInitializationContext<?, ?> observerMethod) {
                for (InjectionPoint ip : observerMethod.getObserver().getInjectionPoints()) {
                    validateInjectionPoint(ip, beanManager);
                }
            }
        });
    }

    @Override
    public void validateBeanNames(final BeanManagerImpl beanManager) {
        final SetMultimap<String, Bean<?>> namedAccessibleBeans = Multimaps.newSetMultimap(new HashMap<String, Collection<Bean<?>>>(), HashSetSupplier.<Bean<?>> instance());

        for (Bean<?> bean : beanManager.getAccessibleBeans()) {
            if (bean.getName() != null) {
                namedAccessibleBeans.put(bean.getName(), bean);
            }
        }

        final List<String> accessibleNamespaces = new ArrayList<String>();
        for (String namespace : beanManager.getAccessibleNamespaces()) {
            accessibleNamespaces.add(namespace);
        }

        final SpecializationAndEnablementRegistry registry = beanManager.getServices().get(SpecializationAndEnablementRegistry.class);
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<String>(namedAccessibleBeans.keySet()) {
            protected void doWork(String name) {
                Set<Bean<?>> resolvedBeans = beanManager.getBeanResolver().resolve(Beans.removeDisabledAndSpecializedBeans(namedAccessibleBeans.get(name), beanManager, registry));
                if (resolvedBeans.size() > 1) {
                    throw new DeploymentException(AMBIGUOUS_EL_NAME, name, resolvedBeans);
                }
                if (accessibleNamespaces.contains(name)) {
                    throw new DeploymentException(BEAN_NAME_IS_PREFIX, name);
                }
            }
        });
    }

    @Override
    public void validateProducers(Collection<Producer<?>> producers, final BeanManagerImpl beanManager) {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<Producer<?>>(producers) {
            protected void doWork(Producer<?> producer) {
                validateProducer(producer, beanManager);
            }
        });
    }
}
