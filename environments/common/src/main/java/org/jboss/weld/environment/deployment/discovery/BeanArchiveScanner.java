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

import java.net.URL;
import java.util.Map;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.util.Preconditions;

/**
 * Scans the application for bean archives.
 *
 * The implementation may be optimized for bean archives containing beans.xml file with bean-discovey-mode of none. E.g. it does not have to scan classes in
 * such an archive.
 *
 * @author Martin Kouba
 */
public interface BeanArchiveScanner {

    public static class ScanResult {

        /**
         * @see BeanArchiveHandler#handle(String)
         */
        private final String beanArchiveRef;

        private final BeansXml beansXml;

        private String beanArchiveId;

        public ScanResult(BeansXml beansXml, String beanArchiveRef, String beanArchiveId) {
            this.beansXml = beansXml;
            this.beanArchiveRef = beanArchiveRef;
            this.beanArchiveId = beanArchiveId;
        }

        public ScanResult(BeansXml beansXml, String beanArchiveRef) {
            this(beansXml, beanArchiveRef, null);
        }

        public String getBeanArchiveRef() {
            return beanArchiveRef;
        }

        public BeansXml getBeansXml() {
            return beansXml;
        }

        /**
         * If {@link #beanArchiveRef} is not set, use {@link #beanArchiveRef}.
         *
         * @return the bean archive id to be used as {@link BeanDeploymentArchive#getId()}
         */
        public String getBeanArchiveId() {
            return beanArchiveId != null ? beanArchiveId : beanArchiveRef;
        }

        /**
         * The id should be consistent between multiple occurrences of the deployment. It's no-op if
         * the reference does not contain the specified separator.
         *
         * @param separator
         * @return self
         */
        public ScanResult extractBeanArchiveId(String separator) {
            Preconditions.checkArgumentNotNull(separator, "separator");
            if (beanArchiveRef.contains(separator)) {
                this.beanArchiveId = beanArchiveRef.substring(beanArchiveRef.indexOf(separator), beanArchiveRef.length());
            }
            return this;
        }

    }

    /**
     * Scans for bean archives identified by beans.xml files. The map must not contain multiple results with the same {@link ScanResult#beanArchiveRef}.
     *
     * @return the map of {@link ScanResult} representations mapped by url of the descriptor
     */
    Map<URL, ScanResult> scan();
}
