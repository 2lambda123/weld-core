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
package org.jboss.weld.event;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.bean.builtin.AbstractFacade;
import org.jboss.weld.bean.builtin.FacadeInjectionPoint;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.EventLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.Types;
import org.jboss.weld.util.reflection.EventObjectTypeResolverBuilder;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.TypeResolver;

/**
 * Implementation of the Event interface
 *
 * @param <T> The type of event being wrapped
 * @author David Allen
 * @see javax.enterprise.event.Event
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_NO_SUITABLE_CONSTRUCTOR", justification = "Uses SerializationProxy")
public class EventImpl<T> extends AbstractFacade<T, Event<T>> implements Event<T>, Serializable {

    private static final String SUBTYPE_ARGUMENT_NAME = "subtype";
    private static final long serialVersionUID = 656782657242515455L;

    public static <E> EventImpl<E> of(InjectionPoint injectionPoint, BeanManagerImpl beanManager) {
        return new EventImpl<E>(injectionPoint, beanManager);
    }

    private final transient HierarchyDiscovery injectionPointTypeHierarchy;
    private transient volatile CachedObservers cachedObservers;

    private EventImpl(InjectionPoint injectionPoint, BeanManagerImpl beanManager) {
        super(injectionPoint, null, beanManager);
        this.injectionPointTypeHierarchy = new HierarchyDiscovery(getType());
    }

    /**
     * Gets a string representation
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return Formats.formatAnnotations(getQualifiers()) + " Event<" + Formats.formatType(getType()) + ">";
    }

    @Override
    public void fire(T event) {
        Preconditions.checkArgumentNotNull(event, "event");
        CachedObservers observers = getObservers(event);

        // we can do lenient here as the event type is checked within #getObservers()
        getBeanManager().getGlobalLenientObserverNotifier().notify(observers.observers, event, observers.metadata);
    }

    private CachedObservers getObservers(T event) {
        CachedObservers cachedObservers = this.cachedObservers;
        if (cachedObservers != null && cachedObservers.rawType.equals(event.getClass())) {
            return cachedObservers;
        }
        final Type eventType = getEventType(event);
        // this performs type check
        final ResolvedObservers<T> observers = getBeanManager().getGlobalStrictObserverNotifier().resolveObservers(eventType, getQualifiers());
        final EventMetadata metadata = new EventMetadataImpl(eventType, getInjectionPoint(), getQualifiers());
        cachedObservers = new CachedObservers(event.getClass(), observers, metadata);
        return this.cachedObservers = cachedObservers;
    }

    @Override
    public Event<T> select(Annotation... qualifiers) {
        return selectEvent(this.getType(), qualifiers);
    }

    @Override
    public <U extends T> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
        Preconditions.checkArgumentNotNull(subtype, SUBTYPE_ARGUMENT_NAME);
        return selectEvent(subtype, qualifiers);
    }

    @Override
    public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        Preconditions.checkArgumentNotNull(subtype, SUBTYPE_ARGUMENT_NAME);
        return selectEvent(subtype.getType(), qualifiers);
    }

    private <U extends T> Event<U> selectEvent(Type subtype, Annotation[] newQualifiers) {
        getBeanManager().getGlobalStrictObserverNotifier().checkEventObjectType(subtype);
        return new EventImpl<U>(new FacadeInjectionPoint(getBeanManager(), getInjectionPoint(), subtype, getQualifiers(), newQualifiers),
                getBeanManager());
    }

    protected Type getEventType(T event) {
        Type resolvedType = event.getClass();
        if (Types.containsUnresolvedTypeVariableOrWildcard(resolvedType)) {
            /*
             * If the container is unable to resolve the parameterized type of the event object, it uses the specified type to infer the parameterized type of the event types.
             */
            resolvedType = injectionPointTypeHierarchy.resolveType(resolvedType);
        }
        if (Types.containsUnresolvedTypeVariableOrWildcard(resolvedType)) {
            /*
             * Examining the hierarchy of the specified type did not help. This may still be one of the cases when combining the
             * event type and the specified type reveals the actual values for type variables. Let's try that.
             */
            Type canonicalEventType = Types.getCanonicalType(event.getClass());
            TypeResolver objectTypeResolver = new EventObjectTypeResolverBuilder(injectionPointTypeHierarchy.getResolver()
                    .getResolvedTypeVariables(), new HierarchyDiscovery(canonicalEventType).getResolver()
                    .getResolvedTypeVariables()).build();
            resolvedType = objectTypeResolver.resolveType(canonicalEventType);
        }
        return resolvedType;
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<T>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw EventLogger.LOG.proxyRequired();
    }

    private static class SerializationProxy<T> extends AbstractFacadeSerializationProxy<T, Event<T>> {

        private static final long serialVersionUID = 9181171328831559650L;

        public SerializationProxy(EventImpl<T> event) {
            super(event);
        }

        private Object readResolve() throws ObjectStreamException {
            return EventImpl.of(getInjectionPoint(), getBeanManager());
        }

    }

    private class CachedObservers {
        private final Class<?> rawType;
        private final ResolvedObservers<T> observers;
        private final EventMetadata metadata;

        private CachedObservers(Class<?> rawType, ResolvedObservers<T> observers, EventMetadata metadata) {
            this.rawType = rawType;
            this.observers = observers;
            this.metadata = metadata;
        }
    }
}
