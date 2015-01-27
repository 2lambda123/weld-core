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
package org.jboss.weld.probe;

/**
 * Various string constants.
 *
 * @author Martin Kouba
 */
final class Strings {

    static final String WELD_VERSION = "weldVersion";
    static final String ID = "id";
    static final String BDA_ID = "bdaId";
    static final String BDA = "bda";
    static final String BEAN_DISCOVERY_MODE = "beanDiscoveryMode";
    static final String BDAS = "bdas";
    static final String CONFIGURATION = "configuration";
    static final String SCOPE = "scope";
    static final String TYPES = "types";
    static final String TYPE = "type";
    static final String QUALIFIER = "qualifier";
    static final String QUALIFIERS = "qualifiers";
    static final String NAME = "name";
    static final String STEREOTYPES = "stereotypes";
    static final String KIND = "kind";
    static final String IS_ALTERNATIVE = "isAlternative";
    static final String EJB_NAME = "ejbName";
    static final String SESSION_BEAN_TYPE = "sessionBeanType";
    static final String DEPENDENCIES = "dependencies";
    static final String DEPENDENTS = "dependents";
    static final String DECLARING_BEAN = "declaringBean";
    static final String DISPOSAL_METHOD = "disposalMethod";
    static final String PRODUCER_METHOD = "producerMethod";
    static final String PRODUCER_FIELD = "producerField";
    static final String PRODUCER_INFO = "producerInfo";
    static final String BEAN_CLASS = "beanClass";
    static final String BEAN_TYPE = "beanType";
    static final String OBSERVED_TYPE = "observedType";
    static final String RECEPTION = "reception";
    static final String TX_PHASE = "txPhase";
    static final String PRIORITY = "priority";
    static final String PRIORITY_RANGE = "priorityRange";
    static final String ANNOTATED_METHOD = "annotatedMethod";
    static final String VALUE = "value";
    static final String PROPERTIES = "properties";
    static final String INSTANCES = "instances";
    static final String AS_STRING = "asString";
    static final String REQUIRED_TYPE = "requiredType";
    static final String METHOD_NAME = "methodName";
    static final String START = "start";
    static final String TIME = "time";
    static final String DECLARED_OBSERVERS = "declaredObservers";
    static final String DECLARED_PRODUCERS = "declaredProducers";
    static final String REMOVED_INVOCATIONS = "removedInvocations";
    static final String CHILDREN = "children";
    static final String INTERCEPTED_BEAN = "interceptedBean";
    static final String DECLARING_CLASS = "declaringClass";
    static final String ENABLEMENT = "enablement";
    static final String INTERCEPTORS = "interceptors";
    static final String DECORATORS = "decorators";
    static final String ALTERNATIVES = "alternatives";
    static final String ACCESSIBLE_BDAS = "accessibleBdas";
    static final String BEANS = "beans";

    static final String PAGE = "page";
    static final String TOTAL = "total";
    static final String LAST_PAGE = "lastPage";
    static final String DATA = "data";
    static final String FILTERS = "filters";

    // Internet media types
    static final String APPLICATION_JSON = "application/json";
    static final String APPLICATION_JAVASCRIPT = "application/javascript";
    // otf, ttf fonts
    static final String APPLICATION_FONT_SFNT = "application/font-sfnt";
    static final String APPLICATION_FONT_WOFF = "application/font-woff";
    // eot
    static final String APPLICATION_FONT_MS = "application/vnd.ms-fontobject";
    static final String TEXT_JAVASCRIPT = "text/javascript";
    static final String TEXT_CSS = "text/css";
    static final String TEXT_HTML = "text/html";
    static final String TEXT_PLAIN = "text/plain";
    static final String IMG_PNG = "image/png";
    static final String IMG_SVG = "image/svg+xml";

    static final String ENCODING_UTF8 = "UTF-8";

    static final String SUFFIX_HTML = "html";
    static final String SUFFIX_CSS = "css";
    static final String SUFFIX_JS = "js";
    static final String SUFFIX_PNG = "png";
    static final String SUFFIX_TTF = "ttf";
    static final String SUFFIX_OTF = "otf";
    static final String SUFFIX_EOT = "eot";
    static final String SUFFIX_SVG = "svg";
    static final String SUFFIX_WOFF = "woff";

    static final String SLASH = "/";

    static final String PARAM_TRANSIENT_DEPENDENCIES = "transientDependencies";
    static final String PARAM_TRANSIENT_DEPENDENTS = "transientDependents";

    static final String FILE_CLIENT_HTML = "probe.html";
    static final String PATH_META_INF_CLIENT = "/META-INF/client/";

    static final String RESOURCE_PARAM_START = "{";
    static final String RESOURCE_PARAM_END = "}";

    static final String ADDITIONAL_BDA_SUFFIX = ".additionalClasses";
    static final String WEB_INF_CLASSES = "WEB-INF/classes";

    private Strings() {
    }

}
