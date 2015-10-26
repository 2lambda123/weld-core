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
package org.jboss.weld.tests.experimental.event.async.metadata;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.EventMetadata;
import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link EventMetadata#isAsync()}.
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class AsyncEventMetadataTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(AsyncEventMetadataTest.class)).addPackage(AsyncEventMetadataTest.class.getPackage());
    }

    @Inject
    private Event<Message> event;

    @Test
    public void testSync() {
        Message message = new Message();
        event.fire(message);
        Assert.assertFalse(message.isAsync());
    }

    @Test
    public void testAsync() throws InterruptedException {
        BlockingQueue<Message> synchronizer = new LinkedBlockingQueue<>();
        event.fireAsync(new Message()).thenAccept(synchronizer::add);
        Assert.assertTrue(synchronizer.poll(2, TimeUnit.SECONDS).isAsync());
    }
}
