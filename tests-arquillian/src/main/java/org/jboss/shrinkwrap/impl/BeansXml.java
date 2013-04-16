package org.jboss.shrinkwrap.impl;

import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BeansXml implements Asset {

    public static final BeansXml SUPPRESSOR = new BeansXml(Collections.<Class<?>> emptyList(), Collections.<Class<?>> emptyList(), Collections.<Class<?>> emptyList(), Collections.<Class<?>> emptyList()) {
        @Override
        public BeanDiscoveryMode getBeanDiscoveryMode() {
            return BeanDiscoveryMode.NONE;
        }
    };

    public static enum BeanDiscoveryMode {

        NONE("none"), ANNOTATED("annotated"), ALL("all");

        private final String value;

        private BeanDiscoveryMode(String value) {
            this.value = value;
        }
    }

    private static final String CLOSING_TAG_PREFIX = "</";
    private static final String OPENING_TAG_PREFIX = "<";
    private static final String TAG_SUFFIX_NEW_LINE = ">\n";
    private static final String TAG_SUFFIX = ">";
    private static final String ALTERNATIVES_ELEMENT_NAME = "alternatives";
    private static final String CLASS = "class";

    private final List<Class<?>> alternatives;
    private final List<Class<?>> interceptors;
    private final List<Class<?>> decorators;
    private final List<Class<?>> stereotypes;
    private BeanDiscoveryMode mode = BeanDiscoveryMode.ALL;

    public BeansXml() {
        this(new ArrayList<Class<?>>(), new ArrayList<Class<?>>(), new ArrayList<Class<?>>(), new ArrayList<Class<?>>());
    }

    private BeansXml(List<Class<?>> alternatives, List<Class<?>> interceptors, List<Class<?>> decorators, List<Class<?>> stereotypes) {
        this.alternatives = alternatives;
        this.interceptors = interceptors;
        this.decorators = decorators;
        this.stereotypes = stereotypes;
    }

    public BeansXml alternatives(Class<?>... alternatives) {
        this.alternatives.addAll(Arrays.asList(alternatives));
        return this;
    }

    public BeansXml interceptors(Class<?>... interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
        return this;
    }

    public BeansXml decorators(Class<?>... decorators) {
        this.decorators.addAll(Arrays.asList(decorators));
        return this;
    }

    public BeansXml stereotype(Class<?>... stereotypes) {
        this.stereotypes.addAll(Arrays.asList(stereotypes));
        return this;
    }

    public BeanDiscoveryMode getBeanDiscoveryMode() {
        return mode;
    }

    public void setBeanDiscoveryMode(BeanDiscoveryMode mode) {
        this.mode = mode;
    }

    public InputStream openStream() {
        StringBuilder xml = new StringBuilder();
        xml.append("<beans version=\"1.1\" bean-discovery-mode=\"");
        xml.append(getBeanDiscoveryMode().value);
        xml.append("\">\n");
        appendAlternatives(alternatives, stereotypes, xml);
        appendSection("interceptors", CLASS, interceptors, xml);
        appendSection("decorators", CLASS, decorators, xml);
        xml.append("</beans>");

        return new ByteArrayInputStream(xml.toString().getBytes());
    }

    private void appendAlternatives(List<Class<?>> alternatives, List<Class<?>> stereotypes, StringBuilder xml) {
        if (alternatives.size() > 0 || stereotypes.size() > 0) {
            xml.append(OPENING_TAG_PREFIX).append(ALTERNATIVES_ELEMENT_NAME).append(TAG_SUFFIX_NEW_LINE);
            appendClasses(CLASS, alternatives, xml);
            appendClasses("stereotype", stereotypes, xml);
            xml.append(CLOSING_TAG_PREFIX).append(ALTERNATIVES_ELEMENT_NAME).append(TAG_SUFFIX_NEW_LINE);
        }
    }

    private void appendSection(String name, String subName, List<Class<?>> classes, StringBuilder xml) {
        if (classes.size() > 0) {
            xml.append(OPENING_TAG_PREFIX).append(name).append(TAG_SUFFIX_NEW_LINE);
            appendClasses(subName, classes, xml);
            xml.append(CLOSING_TAG_PREFIX).append(name).append(TAG_SUFFIX_NEW_LINE);
        }
    }

    private void appendClasses(String name, List<Class<?>> classes, StringBuilder xml) {
        for (Class<?> clazz : classes) {
            xml.append(OPENING_TAG_PREFIX).append(name).append(TAG_SUFFIX).append(clazz.getName()).append(CLOSING_TAG_PREFIX).append(name).append(TAG_SUFFIX_NEW_LINE);
        }
    }
}
