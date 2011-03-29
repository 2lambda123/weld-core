package org.jboss.weld.environment.servlet.test.injection;

import static org.jboss.weld.environment.servlet.test.util.JettyDeployments.JETTY_ENV;
import static org.jboss.weld.environment.servlet.test.util.JettyDeployments.JETTY_WEB;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ListenerInjectionTest extends ListenerInjectionTestBase
{
   
   @Deployment(testable = false)
   public static WebArchive deployment()
   {
      return ListenerInjectionTestBase.deployment().addAsWebInfResource(JETTY_ENV, "jetty-env.xml").addAsWebInfResource(JETTY_WEB, "jetty-web.xml");
   }

}
