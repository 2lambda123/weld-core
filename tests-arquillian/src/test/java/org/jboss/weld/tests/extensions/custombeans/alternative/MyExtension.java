/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.extensions.custombeans.alternative;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessSyntheticBean;

public class MyExtension implements Extension {

  public static int PB_TRIGGERED = 0;
  public static int PSB_TRIGGERED = 0;

  public void observeABD(@Observes AfterBeanDiscovery abd) {
    abd.addBean(new FooBean());
  }

  public void obsevePB(@Observes ProcessBean<Foo> psb) { PB_TRIGGERED++; }

  public void obsevePSB(@Observes ProcessSyntheticBean<Foo> psb) {
    PSB_TRIGGERED++;
  }
}
