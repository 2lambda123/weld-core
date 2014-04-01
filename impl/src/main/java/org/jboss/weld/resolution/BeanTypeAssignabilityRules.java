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
package org.jboss.weld.resolution;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import javax.enterprise.inject.spi.Bean;

/**
 * Assignability rules for {@link Bean} resolution.
 *
 * @author Pete Muir
 * @author Ales Justin
 * @author Marko Luksa
 * @author Jozef Hartinger
 */
public class BeanTypeAssignabilityRules extends EventTypeAssignabilityRules {

    protected BeanTypeAssignabilityRules() {
    }

    private static final BeanTypeAssignabilityRules INSTANCE = new BeanTypeAssignabilityRules();

    public static BeanTypeAssignabilityRules instance() {
        return INSTANCE;
    }

    @Override
    protected boolean areActualTypeArgumentsMatching(ActualTypeHolder requiredType, Type[] otherActualTypeArguments) {
        if (requiredType.getActualTypeArguments().length == 0) {
            /*
             * A parameterized bean type is considered assignable to a raw required type if the raw types are identical and
             * all type parameters of the bean type are either unbounded type variables or java.lang.Object.
             */
            return isArrayOfUnboundedTypeVariablesOrObjects(otherActualTypeArguments);
        } else if (otherActualTypeArguments.length == 0) {
            /*
             * A raw bean type is considered assignable to a parameterized required type if the raw types are identical and all
             * type parameters of the required type are either unbounded type variables or java.lang.Object.
             */
            return isArrayOfUnboundedTypeVariablesOrObjects(requiredType.getActualTypeArguments());
        } else {
            return super.areActualTypeArgumentsMatching(requiredType, otherActualTypeArguments);
        }
    }

    protected boolean isArrayOfUnboundedTypeVariablesOrObjects(Type[] types) {
        for (Type type : types) {
            if (Object.class.equals(type)) {
                continue;
            }
            if (type instanceof TypeVariable<?>) {
                Type[] bounds = ((TypeVariable<?>) type).getBounds();
                if (bounds == null || bounds.length == 0 || (bounds.length == 1 && Object.class.equals(bounds[0]))) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    protected boolean matches(WildcardType requiredType, Type beanType) {
        if (beanType instanceof TypeVariable<?>) {
            /*
             * the required type parameter is a wildcard, the bean type parameter is a type variable and the upper bound of
             * the type variable is assignable to or assignable from the upper bound, if any, of the wildcard and assignable
             * from the lower bound, if any, of the wildcard, or
             */
            TypeVariable<?> beanTypeVariable = (TypeVariable<?>) beanType;
            if (areTypesInsideBounds(beanTypeVariable.getBounds(), requiredType.getLowerBounds(), requiredType.getUpperBounds())) {
                return true;
            }
            if (areTypesInsideBounds(requiredType.getUpperBounds(), EMPTY_TYPES, beanTypeVariable.getBounds())) {
                return true;
            }
            return false;
        } else {
            /*
             * the required type parameter is a wildcard, the bean type parameter is an actual type and the actual type is
             * assignable to the upper bound, if any, of the wildcard and assignable from the lower bound, if any, of the
             * wildcard, or
             */
            return isTypeInsideBounds(beanType, requiredType.getLowerBounds(), requiredType.getUpperBounds());
        }
    }

    @Override
    protected boolean matches(TypeVariable<?> requiredType, Type beanType) {
        /*
         * the required type parameter and the bean type parameter are both type variables and the upper bound of the required
         * type parameter is assignable to the upper bound, if any, of the bean type parameter.
         */
        if (beanType instanceof TypeVariable<?>) {
            TypeVariable<?> requiredTypeVariable = requiredType;
            TypeVariable<?> beanTypeVariable = (TypeVariable<?>) beanType;
            return areTypesInsideBounds(requiredTypeVariable.getBounds(), EMPTY_TYPES, beanTypeVariable.getBounds());
        }
        return false;
    }

    @Override
    protected boolean isAssignableFrom(ActualTypeHolder requiredType, Type otherType) {
        otherType = wrapWithinTypeHolder(otherType);
        if (otherType instanceof ActualTypeHolder) {
            return isAssignableFrom(requiredType, (ActualTypeHolder) otherType);
        }

        // TODO: this doesn't look OK!
        if (otherType instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) otherType;
            if (isTypeInsideBounds(requiredType, EMPTY_TYPES, typeVariable.getBounds())) {
                return true;
            }
        }
        return false;
    }

    /**
     * The spec says:
     * "Array types are considered to match only if their element types are identical."
     * This does not apply to event - observer resolution.
     */
    @Override
    protected boolean arraysMatch(ActualTypeHolder a1, ActualTypeHolder a2) {
        return a1.getComponentType().equals(a2.getComponentType());
    }


}
