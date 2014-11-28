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
package org.jboss.weld.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Named;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.QualifierModel;
import org.jboss.weld.security.SetAccessibleAction;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.collections.WeldCollections;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

/**
 * Optimized representation of a qualifier. JDK annotation proxies are slooow, this class provides significantly faster equals/hashCode methods, that also
 * correctly handle non binding attributes.
 *
 * Note that Weld is using this representation for interceptor bindings as well. See also
 * {@link org.jboss.weld.manager.BeanManagerImpl#resolveInterceptors(javax.enterprise.inject.spi.InterceptionType, java.util.Collection)}
 *
 * @author Stuart Douglas
 * @author Martin Kouba
 */
public class QualifierInstance {

    public static final QualifierInstance ANY = new QualifierInstance(Any.class);
    public static final QualifierInstance DEFAULT = new QualifierInstance(Default.class);

    private final Class<? extends Annotation> annotationClass;
    private final Map<String, Object> values;
    private final int hashCode;

    public static Set<QualifierInstance> of(Set<Annotation> qualifiers, MetaAnnotationStore store) {
        if (qualifiers.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<QualifierInstance> ret = new ArraySet<QualifierInstance>();
        for (Annotation a : qualifiers) {
            ret.add(QualifierInstance.of(a, store));
        }
        return WeldCollections.immutableSet(ret);
    }

    public static Set<QualifierInstance> of(Bean<?> bean, MetaAnnotationStore store) {
        if (bean instanceof RIBean<?>) {
            return ((RIBean<?>) bean).getQualifierInstances();
        }
        return of(bean.getQualifiers(), store);
    }

    /**
     * @param annotation
     * @param store
     * @return a new qualifier instance for the given annotation
     */
    public static QualifierInstance of(Annotation annotation, MetaAnnotationStore store) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (Any.class == annotationType) {
            return ANY;
        } else if (Default.class == annotationType) {
            return DEFAULT;
        } else if (Named.class == annotationType) {
            Named named = (Named) annotation;
            return new QualifierInstance(annotationType, ImmutableMap.<String, Object>of("value", named.value()));
        } else {
            return new QualifierInstance(annotationType, createValues(annotation, store));
        }
    }

    private QualifierInstance(final Class<? extends Annotation> annotationClass) {
        this(annotationClass, Collections.<String, Object>emptyMap());
    }

    private QualifierInstance(Class<? extends Annotation> annotationClass, Map<String, Object> values) {
        this.annotationClass = annotationClass;
        this.values = values;
        this.hashCode = Objects.hashCode(annotationClass, values);
    }

    private static Map<String, Object> createValues(final Annotation instance, final MetaAnnotationStore store) {

        final Class<? extends Annotation> annotationClass = instance.annotationType();
        final QualifierModel<? extends Annotation> model = store.getBindingTypeModel(annotationClass);

        if(model.getAnnotatedAnnotation().getMethods().size() == 0) {
            return Collections.emptyMap();
        }

        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        for (final AnnotatedMethod<?> method : model.getAnnotatedAnnotation().getMethods()) {
            if(!model.getNonBindingMembers().contains(method)) {
                try {
                    if (System.getSecurityManager() != null) {
                        AccessController.doPrivileged(SetAccessibleAction.of(method.getJavaMember()));
                    } else {
                        method.getJavaMember().setAccessible(true);
                    }
                    builder.put(method.getJavaMember().getName(), method.getJavaMember().invoke(instance));
                } catch (IllegalAccessException e) {
                    throw new WeldException(e);
                } catch (InvocationTargetException e) {
                    throw new WeldException(e);
                }
            }
        }
        return builder.build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final QualifierInstance that = (QualifierInstance) o;

        if (!annotationClass.equals(that.annotationClass)) {
            return false;
        }
        if (!values.equals(that.values)) {
            return false;
        }

        return true;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "QualifierInstance {" +
                "annotationClass=" + annotationClass +
                ", values=" + values +
                '}';
    }

}
