/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

import java.io.Serializable;

import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;

/**
 * General event payload for {@link Initialized} / {@link Destroyed} events. A more specific payload is necessary
 * for certain contexts (e.g. {@link javax.servlet.http.HttpServletRequest})
 *
 * @author Jozef Hartinger
 *
 */
public class ContextEvent implements Serializable {

    private static final long serialVersionUID = -1197351184144276424L;

    private final String message;

    public ContextEvent(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
