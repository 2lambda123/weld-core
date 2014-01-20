package org.jboss.weld.environment.servlet.test.el;

import static org.jboss.weld.environment.servlet.test.util.TomcatDeployments.CONTEXT_XML;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JsfTest extends JsfTestBase {
    @Deployment(testable = false)
    public static WebArchive deployment() {
        return JsfTestBase.deployment().add(CONTEXT_XML, "META-INF/context.xml");
    }
}
