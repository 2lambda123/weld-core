/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.deployment.discovery;

import java.util.Collection;
import java.util.List;

/**
 * Scans the application for bean archives.
 *
 * The implementation may be optimized for bean archives containing beans.xml file with bean-discovey-mode of none. E.g. it does not have to scan classes in
 * such an archive.
 *
 * @author Martin Kouba
 */
public interface BeanArchiveScanner {

    /**
     * Performs scan. The {@link DiscoveryStrategy} may provide some default handlers. However, it's up to the scanner whether it will use the provided handlers
     * or not.
     *
     * @param beanArchiveHandlers The ordered list of provided bean archive handlers
     * @return the collection of {@link BeanArchiveBuilder}s which will be used for discovery
     */
    Collection<BeanArchiveBuilder> scan(List<BeanArchiveHandler> beanArchiveHandlers);

}
