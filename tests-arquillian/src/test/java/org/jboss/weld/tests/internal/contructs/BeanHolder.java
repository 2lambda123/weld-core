/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.internal.contructs;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@ApplicationScoped
public class BeanHolder {

    @Inject
    private ClientProxyBean clientProxyBean;

    @Inject
    private DecoratedDependentBean decoratedBean;

    @Inject
    private InterceptedDependentBean interceptedBean;

    @Inject
    private DecoratedProxiedBean decoratedProxiedBean;

    @Inject
    private InterceptedProxiedBean interceptedProxiedBean;

    public ClientProxyBean getClientProxyBean() {
        return clientProxyBean;
    }

    public DecoratedDependentBean getDecoratedDependentBean() {
        return decoratedBean;
    }

    public InterceptedDependentBean getInterceptedDependentBean() {
        return interceptedBean;
    }

    public InterceptedProxiedBean getInterceptedProxiedBean() {
        return interceptedProxiedBean;
    }

    public DecoratedProxiedBean getDecoratedProxiedBean() {
        return decoratedProxiedBean;
    }
}
