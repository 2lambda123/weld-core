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
package org.jboss.weld.annotated.enhanced.jlr;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Reflections;

public class MethodSignatureImpl implements MethodSignature {

    private static final long serialVersionUID = 870948075030895317L;

    private final String methodName;
    private final String[] parameterTypes;

    public MethodSignatureImpl(AnnotatedMethod<?> method) {
        this.methodName = method.getJavaMember().getName();
        this.parameterTypes = new String[method.getParameters().size()];
        for (int i = 0; i < method.getParameters().size(); i++) {
            parameterTypes[i] = Reflections.getRawType(method.getParameters().get(i).getBaseType()).getName();
        }
    }

    public MethodSignatureImpl(Method method) {
        this.methodName = method.getName();
        this.parameterTypes = new String[method.getParameterTypes().length];
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            parameterTypes[i] = method.getParameterTypes()[i].getName();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + methodName.hashCode();
        result = prime * result + Arrays.hashCode(parameterTypes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MethodSignatureImpl)) {
            return false;
        }
        MethodSignatureImpl other = (MethodSignatureImpl) obj;
        if (methodName == null) {
            if (other.methodName != null) {
                return false;
            }
        } else if (!methodName.equals(other.methodName)) {
            return false;
        }
        if (!Arrays.equals(parameterTypes, other.parameterTypes)) {
            return false;
        }
        return true;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getParameterTypes() {
        return Arrays2.copyOf(parameterTypes, parameterTypes.length);
    }

    @Override
    public String toString() {
        return new StringBuffer().append("method ").append(getMethodName()).append(Arrays.toString(parameterTypes).replace('[', '(').replace(']', ')')).toString();
    }

}
