/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection;

import static org.jboss.weld.injection.Exceptions.rethrowException;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.TransientReference;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.Arrays2;

/**
 * {@link MethodInjectionPoint} that delegates to a static method.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 * @param <X>
 */
class StaticMethodInjectionPoint<T, X> extends AbstractCallableInjectionPoint<T, X, Method> implements MethodInjectionPoint<T, X> {

    // TODO transient reference mask instead of looking up annotations
    private final int specialInjectionPointIndex;
    private final AnnotatedMethod<X> annotatedMethod;
    final Method accessibleMethod;

    StaticMethodInjectionPoint(EnhancedAnnotatedMethod<T, X> enhancedMethod, Bean<?> declaringBean, Class<?> declaringComponentClass,
            Class<? extends Annotation> specialParameterMarker, InjectionPointFactory factory, BeanManagerImpl manager) {
        super(enhancedMethod, declaringBean, declaringComponentClass, specialParameterMarker != null, factory, manager);
        this.accessibleMethod = SecurityActions.getAccessibleCopyOfMethod(enhancedMethod.getJavaMember());
        this.annotatedMethod = enhancedMethod.slim();
        this.specialInjectionPointIndex = initSpecialInjectionPointIndex(enhancedMethod, specialParameterMarker);
    }

    private static <X> int initSpecialInjectionPointIndex(EnhancedAnnotatedMethod<?, X> enhancedMethod, Class<? extends Annotation> specialParameterMarker) {
        if (specialParameterMarker == null) {
            return -1;
        }
        List<EnhancedAnnotatedParameter<?, X>> parameters = enhancedMethod.getEnhancedParameters(specialParameterMarker);
        if (parameters.isEmpty()) {
            throw new org.jboss.weld.exceptions.IllegalArgumentException("Not a disposer nor observer method: " + enhancedMethod);
        }
        return parameters.get(0).getPosition();
    }

    public T invoke(Object receiver, Object specialValue, BeanManagerImpl manager, CreationalContext<?> ctx,
            Class<? extends RuntimeException> exceptionTypeToThrow) {
        CreationalContext<?> invocationContext = null;
        if (!getInjectionPoints().isEmpty()) {
            invocationContext = manager.createCreationalContext(null);
        }
        try {
            return cast(getMethod(receiver).invoke(receiver, getParameterValues(specialValue, manager, ctx, invocationContext)));
        } catch (IllegalArgumentException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (SecurityException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (IllegalAccessException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (InvocationTargetException e) {
            rethrowException(e, exceptionTypeToThrow);
        } catch (NoSuchMethodException e) {
            rethrowException(e, exceptionTypeToThrow);
        } finally {
            if (invocationContext != null) {
                invocationContext.release();
            }
        }
        return null;
    }

    /**
     * Helper method for getting the current parameter values from a list of annotated parameters.
     *
     * @param parameters The list of annotated parameter to look up
     * @param manager The Bean manager
     * @return The object array of looked up values
     */
    protected Object[] getParameterValues(Object specialVal, BeanManagerImpl manager, CreationalContext<?> ctx, CreationalContext<?> invocationContext) {
        if (getInjectionPoints().isEmpty()) {
            if (specialInjectionPointIndex == -1) {
                return Arrays2.EMPTY_ARRAY;
            } else {
                return new Object[] { specialVal };
            }
        }
        Object[] parameterValues = new Object[getParameterInjectionPoints().size()];
        Iterator<ParameterInjectionPoint<?, X>> iterator = getParameterInjectionPoints().iterator();
        for (int i = 0; i < parameterValues.length; i++) {
            ParameterInjectionPoint<?, ?> param = iterator.next();
            if (i == specialInjectionPointIndex) {
                parameterValues[i] = specialVal;
            } else if (param.getAnnotated().isAnnotationPresent(TransientReference.class)) {
                parameterValues[i] = param.getValueToInject(manager, invocationContext);
            } else {
                parameterValues[i] = param.getValueToInject(manager, ctx);
            }
        }
        return parameterValues;
    }

    protected Method getMethod(Object receiver) throws NoSuchMethodException {
        return accessibleMethod;
    }

    @Override
    public AnnotatedMethod<X> getAnnotated() {
        return annotatedMethod;
    }
}
