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
package org.jboss.weld.event;

import static com.google.common.collect.ImmutableSet.copyOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.util.Observers;

import com.google.common.collect.ImmutableSet;

public class ResolvedObservers<T> {

    private static final ResolvedObservers<Object> EMPTY = new ResolvedObservers<Object>(Collections.<ObserverMethod<? super Object>>emptySet(), Collections.<ObserverMethod<? super Object>>emptySet(), false) {
        public boolean isEmpty() {
            return true;
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> ResolvedObservers<T> of(Set<ObserverMethod<? super T>> observers) {
        if (observers.isEmpty()) {
            return (ResolvedObservers<T>) EMPTY;
        }
        boolean metadataRequired = false;
        List<ObserverMethod<? super T>> immediateObservers = new ArrayList<ObserverMethod<? super T>>();
        List<ObserverMethod<? super T>> transactionObservers = new ArrayList<ObserverMethod<? super T>>();
        for (ObserverMethod<? super T> observer : observers) {
            if (TransactionPhase.IN_PROGRESS == observer.getTransactionPhase()) {
                immediateObservers.add(observer);
            } else {
                transactionObservers.add(observer);
            }
            if (!metadataRequired && Observers.isEventMetadataRequired(observer)) {
                metadataRequired = true;
            }
        }
        return new ResolvedObservers<T>(copyOf(immediateObservers), copyOf(transactionObservers), metadataRequired);
    }

    private final Set<ObserverMethod<? super T>> immediateObservers;
    private final Set<ObserverMethod<? super T>> transactionObservers;
    private final boolean metadataRequired;

    private ResolvedObservers(Set<ObserverMethod<? super T>> immediateObservers, Set<ObserverMethod<? super T>> transactionObservers, boolean metadataRequired) {
        this.immediateObservers = immediateObservers;
        this.transactionObservers = transactionObservers;
        this.metadataRequired = metadataRequired;
    }

    Set<ObserverMethod<? super T>> getImmediateObservers() {
        return immediateObservers;
    }

    Set<ObserverMethod<? super T>> getTransactionObservers() {
        return transactionObservers;
    }

    boolean isMetadataRequired() {
        return metadataRequired;
    }

    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns all observer methods.
     */
    public Set<ObserverMethod<? super T>> getAllObservers() {
        return ImmutableSet.<ObserverMethod<? super T>>builder().addAll(immediateObservers).addAll(transactionObservers).build();
    }
}
