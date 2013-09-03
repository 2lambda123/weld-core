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

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.jboss.weld.annotated.AnnotatedTypeValidator;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.spi.AnnotationDiscovery;

/**
 * Container lifecycle event for each Java class or interface discovered by
 * the container.
 *
 * @author pmuir
 * @author David Allen
 */
@SuppressWarnings("rawtypes")
public class ProcessAnnotatedTypeImpl<X> extends AbstractDefinitionContainerEvent implements ProcessAnnotatedType<X> {

    private final SlimAnnotatedType<X> originalAnnotatedType;
    private AnnotatedType<X> annotatedType;
    private boolean veto;
    private final Resolvable resolvable;

    public ProcessAnnotatedTypeImpl(BeanManagerImpl beanManager, SlimAnnotatedType<X> annotatedType, AnnotationDiscovery discovery) {
        this(beanManager, annotatedType, ProcessAnnotatedType.class, discovery);
    }

    protected ProcessAnnotatedTypeImpl(BeanManagerImpl beanManager, SlimAnnotatedType<X> annotatedType, Class<? extends ProcessAnnotatedType> rawType, AnnotationDiscovery discovery) {
        super(beanManager, rawType, new Type[] { annotatedType.getJavaClass() });
        this.annotatedType = annotatedType;
        this.originalAnnotatedType = annotatedType;
        this.resolvable = createResolvable(annotatedType, discovery);
    }

    public AnnotatedType<X> getAnnotatedType() {
        return annotatedType;
    }

    /**
     * Call this method after all observer methods of this event have been invoked to get the final value of this {@link AnnotatedType}.
     * @return
     */
    public SlimAnnotatedType<X> getResultingAnnotatedType() {
        if (isDirty()) {
            return ClassTransformer.instance(getBeanManager()).getUnbackedAnnotatedType(originalAnnotatedType, annotatedType);
        } else {
            return originalAnnotatedType;
        }
    }

    public void setAnnotatedType(AnnotatedType<X> type) {
        if (type == null) {
            throw BootstrapLogger.LOG.annotationTypeNull(this);
        }
        if (!this.originalAnnotatedType.getJavaClass().equals(type.getJavaClass())) {
            throw BootstrapLogger.LOG.annotatedTypeJavaClassMismatch(this.annotatedType.getJavaClass(), type.getJavaClass());
        }
        AnnotatedTypeValidator.validateAnnotatedType(type);
        this.annotatedType = type;
    }

    protected Resolvable createResolvable(SlimAnnotatedType<X> annotatedType, AnnotationDiscovery discovery) {
        return ProcessAnnotatedTypeEventResolvable.forProcessAnnotatedType(annotatedType, discovery);
    }

    @Override
    public void fire() {
        try {
            getBeanManager().getGlobalLenientObserverNotifier().fireEvent(this, resolvable);
        } catch (Exception e) {
            getErrors().add(e);
        }
        if (!getErrors().isEmpty()) {
            throw new DefinitionException(getErrors());
        }
    }

    public void veto() {
        this.veto = true;
    }

    public boolean isVeto() {
        return veto;
    }

    public boolean isDirty() {
        return originalAnnotatedType != annotatedType;
    }

    @Override
    public String toString() {
        return annotatedType.toString();
    }

}
