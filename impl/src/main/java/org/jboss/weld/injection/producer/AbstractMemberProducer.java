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
package org.jboss.weld.injection.producer;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CIRCULAR_CALL;
import static org.jboss.weld.logging.messages.BeanMessage.DECLARING_BEAN_MISSING;
import static org.jboss.weld.logging.messages.BeanMessage.PRODUCER_METHOD_CANNOT_HAVE_A_WILDCARD_RETURN_TYPE;
import static org.jboss.weld.logging.messages.BeanMessage.PRODUCER_METHOD_WITH_TYPE_VARIABLE_RETURN_TYPE_MUST_BE_DEPENDENT;
import static org.jboss.weld.logging.messages.BeanMessage.RETURN_TYPE_MUST_BE_CONCRETE;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMember;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.slf4j.cal10n.LocLogger;

/**
 * Common functionality for {@link Producer}s backing producer fields and producer methods.
 *
 * @author Jozef Hartinger
 */
public abstract class AbstractMemberProducer<X, T> extends AbstractProducer<T> {

    private static final LocLogger log = loggerFactory().getLogger(BEAN);

    private final DisposalMethod<?, ?> disposalMethod;

    public AbstractMemberProducer(EnhancedAnnotatedMember<T, ? super X, ? extends Member> enhancedMember, DisposalMethod<?, ?> disposalMethod) {
        this.disposalMethod = disposalMethod;
        checkDeclaringBean();
        checkProducerReturnType(enhancedMember);
    }

    protected void checkDeclaringBean() {
        if (getDeclaringBean() == null && !getAnnotated().isStatic()) {
            throw new org.jboss.weld.exceptions.IllegalArgumentException(DECLARING_BEAN_MISSING, getAnnotated());
        }
    }

    /**
     * Validates the producer method
     */
    protected void checkProducerReturnType(EnhancedAnnotatedMember<T, ? super X, ? extends Member> enhancedMember) {
        if ((enhancedMember.getBaseType() instanceof TypeVariable<?>) || (enhancedMember.getBaseType() instanceof WildcardType)) {
            throw new DefinitionException(RETURN_TYPE_MUST_BE_CONCRETE, enhancedMember.getBaseType());
        } else if (enhancedMember.isParameterizedType()) {
            boolean dependent = getBean() != null && Dependent.class.equals(getBean().getScope());
            for (Type type : enhancedMember.getActualTypeArguments()) {
                if (!dependent && type instanceof TypeVariable<?>) {
                    throw new DefinitionException(PRODUCER_METHOD_WITH_TYPE_VARIABLE_RETURN_TYPE_MUST_BE_DEPENDENT, enhancedMember);
                } else if (type instanceof WildcardType) {
                    throw new DefinitionException(PRODUCER_METHOD_CANNOT_HAVE_A_WILDCARD_RETURN_TYPE, enhancedMember);
                }
            }
        }
    }

    /**
     * Gets the receiver of the product. The two creational contexts need to be separated because the receiver only serves the product
     * creation (it is not a dependent instance of the created instance).
     *
     * @param productCreationalContext the creational context of the produced instance
     * @param receiverCreationalCOntext the creational context of the receiver
     * @return The receiver
     */
    protected Object getReceiver(CreationalContext<?> productCreationalContext, CreationalContext<?> receiverCreationalContext) {
        // This is a bit dangerous, as it means that producer methods can end up
        // executing on partially constructed instances. Also, it's not required
        // by the spec...
        if (getAnnotated().isStatic()) {
            return null;
        } else {
            if (productCreationalContext instanceof WeldCreationalContext<?>) {
                WeldCreationalContext<?> creationalContextImpl = (WeldCreationalContext<?>) productCreationalContext;
                final Object incompleteInstance = creationalContextImpl.getIncompleteInstance(getDeclaringBean());
                if (incompleteInstance != null) {
                    log.warn(CIRCULAR_CALL, getAnnotated(), getDeclaringBean());
                    return incompleteInstance;
                }
            }
            return getBeanManager().getReference(getDeclaringBean(), null, receiverCreationalContext, true);
        }
    }

    public void dispose(T instance) {
        if (disposalMethod != null) {
            CreationalContext<T> ctx = getBeanManager().createCreationalContext(null);
            try {
                Object receiver = getReceiver(ctx, ctx);
                disposalMethod.invokeDisposeMethod(receiver, instance, ctx);
            } finally {
                ctx.release();
            }
        }
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        CreationalContext<X> receiverCreationalContext = getBeanManager().createCreationalContext(getDeclaringBean());
        Object receiver = getReceiver(ctx, receiverCreationalContext);

        try {
            return produce(receiver, ctx);
        } finally {
            receiverCreationalContext.release();
        }
    }

    public DisposalMethod<?, ?> getDisposalMethod() {
        return disposalMethod;
    }

    protected boolean isTypeSerializable(Object object) {
        return object instanceof Serializable;
    }

    public abstract BeanManagerImpl getBeanManager();

    public abstract Bean<X> getDeclaringBean();

    public abstract Bean<T> getBean();

    public abstract AnnotatedMember<? super X> getAnnotated();

    protected abstract T produce(Object receiver, CreationalContext<T> ctx);

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Producer for ");
        if (getDeclaringBean() == null) {
            result.append(getAnnotated());
        } else {
            if (getBean() == null) {
                result.append(getAnnotated());
        } else {
                result.append(getBean());
            }
            result.append(" declared on " + getDeclaringBean());
        }
        return result.toString();
    }
}
