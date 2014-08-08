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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 *
 * @author Matej Briškár
 * @author Martin Kouba
 */
public abstract class AbstractDiscoveryStrategy implements DiscoveryStrategy {

    protected final ResourceLoader resourceLoader;

    protected final Bootstrap bootstrap;

    protected BeanArchiveScanner scanner;

    public AbstractDiscoveryStrategy(ResourceLoader resourceLoader, Bootstrap bootstrap) {
        this.resourceLoader = resourceLoader;
        this.bootstrap = bootstrap;
    }

    @Override
    public void setScanner(BeanArchiveScanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public Set<WeldBeanDeploymentArchive> performDiscovery() {

        if (scanner == null) {
            scanner = new DefaultBeanArchiveScanner(resourceLoader, bootstrap);
        }

        Collection<BeanArchiveBuilder> beanArchiveBuilders = scanner.scan(getBeanArchiveHandlers());
        beforeDiscovery(beanArchiveBuilders);
        Set<WeldBeanDeploymentArchive> archives = new HashSet<WeldBeanDeploymentArchive>();

        for (BeanArchiveBuilder builder : beanArchiveBuilders) {
            BeansXml beansXml = builder.getBeansXml();
            switch (beansXml.getBeanDiscoveryMode()) {
                case ALL:
                    addToArchives(archives, processAllDiscovery(builder));
                    break;
                case ANNOTATED:
                    addToArchives(archives, processAnnotatedDiscovery(builder));
                    break;
                case NONE:
                    addToArchives(archives, processNoneDiscovery(builder));
                    break;
                default:
                    CommonLogger.LOG.undefinedBeanDiscoveryValue(beansXml.getBeanDiscoveryMode());
            }
        }
        afterDiscovery(archives);
        return archives;
    }

    @Override
    public ClassFileServices getClassFileServices() {
        // By default no bytecode scanning facility available
        return null;
    }

    protected void assignVisibility(Set<WeldBeanDeploymentArchive> deploymentArchives) {
        // By default bean archives see each other
        for (WeldBeanDeploymentArchive archive : deploymentArchives) {
            archive.setAccessibleBeanDeploymentArchives(deploymentArchives);
        }
    }

    protected void addToArchives(Set<WeldBeanDeploymentArchive> deploymentArchives, WeldBeanDeploymentArchive bda) {
        if (bda != null) {
            deploymentArchives.add(bda);
        }
    }

    /**
     * Initialize the strategy before accessing found BeanArchiveBuilder builders. Best used for saving some information before the process method for each
     * builder is called.
     */
    protected void beforeDiscovery(Collection<BeanArchiveBuilder> builders) {
        // No-op
    }

    protected void afterDiscovery(Set<WeldBeanDeploymentArchive> archives) {
        assignVisibility(archives);
    }

    /**
     * Process the bean archive with bean-discovery-mode of none. The archive is ignored by default.
     */
    protected WeldBeanDeploymentArchive processNoneDiscovery(BeanArchiveBuilder builder) {
        return null;
    }

    /**
     * Process the bean archive with bean-discovery-mode of annotated.
     */
    protected WeldBeanDeploymentArchive processAnnotatedDiscovery(BeanArchiveBuilder builder) {
        throw new UnsupportedOperationException();
    }

    /**
     * Process the bean archive with bean-discovery-mode of all.
     */
    protected WeldBeanDeploymentArchive processAllDiscovery(BeanArchiveBuilder builder) {
        return builder.build();
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    protected abstract List<BeanArchiveHandler> getBeanArchiveHandlers();

}
