package org.jboss.weld.tests.proxy.superclass.named;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Yann Diorcet
 * @see https://issues.jboss.org/browse/WELD-1827
 */
@RunWith(Arquillian.class)
public class Weld1827Test {

    @Inject
    private Bar bar;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(Weld1827Test.class.getPackage());
    }

    @Test
    public void testDeployment() {
        Assert.assertEquals("Bar", bar.getName());
    }

}
