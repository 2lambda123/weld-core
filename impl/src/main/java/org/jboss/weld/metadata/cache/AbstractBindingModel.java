/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.metadata.cache;

import static org.jboss.weld.util.collections.WeldCollections.immutableSetView;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.util.Nonbinding;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotation;

/**
 * Common functionality for qualifiers and interceptor bindings.
 *
 * @author Jozef Hartinger
 *
 */
public abstract class AbstractBindingModel<T extends Annotation> extends AnnotationModel<T> {

    // The non-binding types
    private Set<AnnotatedMethod<?>> nonBindingMembers;

    public AbstractBindingModel(EnhancedAnnotation<T> enhancedAnnotatedAnnotation) {
        super(enhancedAnnotatedAnnotation);
    }

    @Override
    protected void init(EnhancedAnnotation<T> annotatedAnnotation) {
        initNonBindingMembers(annotatedAnnotation);
        super.init(annotatedAnnotation);
    }

    protected void initNonBindingMembers(EnhancedAnnotation<T> annotatedAnnotation) {
        Set<EnhancedAnnotatedMethod<?, ?>> enhancedMethods = annotatedAnnotation.getMembers(Nonbinding.class);
        if (enhancedMethods.isEmpty()) {
            this.nonBindingMembers = Collections.emptySet();
        } else {
            Set<AnnotatedMethod<?>> nonBindingMembers = new HashSet<AnnotatedMethod<?>>();
            for (EnhancedAnnotatedMethod<?, ?> method : enhancedMethods) {
                nonBindingMembers.add(method.slim());
            }
            this.nonBindingMembers = immutableSetView(nonBindingMembers);
        }
    }

    /**
     * Gets the non-binding types
     *
     * @return A set of non-binding types, or an empty set if there are none present
     */
    public Set<AnnotatedMethod<?>> getNonBindingMembers() {
        return nonBindingMembers;
    }
}
