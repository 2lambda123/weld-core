/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import org.jboss.weld.event.ExtensionObserverMethodImpl;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Thrown when {@link FastProcessAnnotatedTypeResolver} cannot be created due to unsupported observed type.
 *
 * @author Jozef Hartinger
 *
 */
class UnsupportedObserverMethodException extends Exception {

    private static final long serialVersionUID = -2164722035016351775L;

    @SuppressWarnings(value = "SE_BAD_FIELD", justification = "Depends on realization of observer")
    private final ExtensionObserverMethodImpl<?, ?> observer;

    public UnsupportedObserverMethodException(ExtensionObserverMethodImpl<?, ?> observer) {
        this.observer = observer;
    }

    public ExtensionObserverMethodImpl<?, ?> getObserver() {
        return observer;
    }
}
