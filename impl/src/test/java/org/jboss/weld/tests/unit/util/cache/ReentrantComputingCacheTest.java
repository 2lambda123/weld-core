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
package org.jboss.weld.tests.unit.util.cache;

import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Testcase for {@link ComputingCacheBuilder#buildReentrant(java.util.function.Function)}
 *
 * @author Jozef Hartinger
 *
 */
public class ReentrantComputingCacheTest {

    private ComputingCache<Class<?>, Integer> cache;

    @Before
    public void init() {
        cache = ComputingCacheBuilder.newBuilder().buildReentrant(
                (x) -> x.getInterfaces().length + ((x.getSuperclass() == null) ? 0 : countInterfaces(x.getSuperclass())));
    }

    public int countInterfaces(Class<?> clazz) {
        return cache.getValue(clazz);
    }

    @Test
    public void testReentrantComputingCache() {
        Assert.assertEquals(2, countInterfaces(Integer.class));
    }
}
