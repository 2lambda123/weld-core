/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.event;

import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractReceiverBean;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.events.AbstractContainerEvent;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.introspector.WeldMember;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.WeldParameter;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.ObserverException;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.weld.logging.messages.EventMessage.INVALID_DISPOSES_PARAMETER;
import static org.jboss.weld.logging.messages.EventMessage.INVALID_INITIALIZER;
import static org.jboss.weld.logging.messages.EventMessage.INVALID_PRODUCER;
import static org.jboss.weld.logging.messages.EventMessage.INVALID_SCOPED_CONDITIONAL_OBSERVER;
import static org.jboss.weld.logging.messages.EventMessage.MULTIPLE_EVENT_PARAMETERS;
import static org.jboss.weld.logging.messages.ValidatorMessage.NON_FIELD_INJECTION_POINT_CANNOT_USE_NAMED;

/**
 * <p>
 * Reference implementation for the ObserverMethod interface, which represents
 * an observer method. Each observer method has an event type which is the class
 * of the event object being observed, and event binding types that are
 * annotations applied to the event parameter to narrow the event notifications
 * delivered.
 * </p>
 * 
 * @author David Allen
 * @author Ales Justin
 */
public class ObserverMethodImpl<T, X> extends AbstractReceiverBean<X, T, Method> implements ObserverMethod<T>
{
   
   public static final String ID_PREFIX = ObserverMethodImpl.class.getPackage().getName();
   
   public static final String ID_SEPARATOR = "-";
   
   private final Set<Annotation> bindings;
   private final Type eventType;
   protected BeanManagerImpl beanManager;
   private final Reception reception;
   protected final RIBean<X> declaringBean;
   protected final MethodInjectionPoint<T, ? super X> observerMethod;
   protected TransactionPhase transactionPhase;

   /**
    * Creates an Observer which describes and encapsulates an observer method
    * (8.5).
    *
    * @param observer The observer
    * @param declaringBean The observer bean
    * @param manager The Bean manager
    * @param services the services
    */
   protected ObserverMethodImpl(final WeldMethod<T, ? super X> observer, final RIBean<X> declaringBean, final BeanManagerImpl manager, ServiceRegistry services)
   {
      super(new StringBuilder().append(ID_PREFIX).append(ID_SEPARATOR).append(ObserverMethod.class.getSimpleName()).append(ID_SEPARATOR).append(declaringBean.getBeanClass().getName()).append(".").append(observer.getSignature()).toString(),
            declaringBean, manager, services);
      this.beanManager = manager;
      this.declaringBean = declaringBean;
      this.observerMethod = MethodInjectionPoint.of(declaringBean, observer);
      this.eventType = observerMethod.getAnnotatedParameters(Observes.class).get(0).getBaseType();
      this.bindings = new HashSet<Annotation>(observerMethod.getAnnotatedParameters(Observes.class).get(0).getMetaAnnotations(Qualifier.class));
      Observes observesAnnotation = observerMethod.getAnnotatedParameters(Observes.class).get(0).getAnnotation(Observes.class);
      this.reception = observesAnnotation.notifyObserver();
      transactionPhase = TransactionPhase.IN_PROGRESS;

      initQualifiers();
      initTypes();
      initStereotypes();
      for (ParameterInjectionPoint<?,?> ip : Beans.getParameterInjectionPoints(this, observerMethod))
      {
         if (ip.isAnnotationPresent(Observes.class) == false)
         {
            addInjectionPoint(ip);
         }
      }
   }

   @Override
   public WeldMember<T, ?, Method> getWeldAnnotated()
   {
      return observerMethod;
   }

   @Override
   protected void initQualifiers()
   {
      qualifiers = new HashSet<Annotation>();
      qualifiers.addAll(observerMethod.getWeldParameters().get(0).getQualifiers());
      initDefaultQualifiers();
   }

   @Override
   protected void initTypes()
   {
      Set<Type> types = new HashSet<Type>();
      types.addAll(observerMethod.getAnnotatedParameters(Observes.class).get(0).getTypeClosure());
      types.add(Object.class);
      super.types = types;
   }

   @Override
   protected void checkType()
   {
      // No-op
   }

   @Override
   protected void initScope()
   {
      initScopeFromStereotype();

      if (this.scope == null)
         this.scope = Dependent.class;
   }

   @Override
   protected String getDefaultName()
   {
      return observerMethod.getPropertyName();
   }

   @Override
   public AbstractBean<?, ?> getSpecializedBean()
   {
      // Doesn't support specialization
      return null;
   }

   @Override
   public boolean isProxyable()
   {
      return true;
   }

   @Override
   public boolean isPassivationCapableBean()
   {
      return false;  // Not relevant
   }

   @Override
   public boolean isPassivationCapableDependency()
   {
      return false;  // Not relevant
   }

   public T create(CreationalContext<T> tCreationalContext)
   {
      return null;  // Not relevant
   }

   public void destroy(T instance, CreationalContext<T> tCreationalContext)
   {
      // Nothing to do
   }

   @Override
   public Set<Class<? extends Annotation>> getStereotypes()
   {
      return Collections.emptySet();
   }

   /**
    * Performs validation of the observer method for compliance with the
    * specifications.
    */
   private void checkObserverMethod()
   {
      // Make sure exactly one and only one parameter is annotated with Observes
      List<?> eventObjects = this.observerMethod.getAnnotatedParameters(Observes.class);
      if (this.reception.equals(Reception.IF_EXISTS) && declaringBean.getScope().equals(Dependent.class))
      {
         throw new DefinitionException(INVALID_SCOPED_CONDITIONAL_OBSERVER, this);
      }
      if (eventObjects.size() > 1)
      {
         throw new DefinitionException(MULTIPLE_EVENT_PARAMETERS, this);
      }
      // Check for parameters annotated with @Disposes
      List<?> disposeParams = this.observerMethod.getAnnotatedParameters(Disposes.class);
      if (disposeParams.size() > 0)
      {
         throw new DefinitionException(INVALID_DISPOSES_PARAMETER, this);
      }
      // Check annotations on the method to make sure this is not a producer
      // method, initializer method, or destructor method.
      if (this.observerMethod.isAnnotationPresent(Produces.class))
      {
         throw new DefinitionException(INVALID_PRODUCER, this);
      }
      if (this.observerMethod.isAnnotationPresent(Inject.class))
      {
         throw new DefinitionException(INVALID_INITIALIZER, this);
      }
      for (WeldParameter<?, ?> parameter : getMethod().getWeldParameters())
      {
         if (parameter.isAnnotationPresent(Named.class) && parameter.getAnnotation(Named.class).value().equals(""))
         {
            throw new DefinitionException(NON_FIELD_INJECTION_POINT_CANNOT_USE_NAMED, getMethod());
         }
      }

   }

   public Class<X> getBeanClass()
   {
      return declaringBean.getType();
   }
   
   public Annotation[] getBindingsAsArray()
   {
      return bindings.toArray(new Annotation[bindings.size()]);
   }

   public Reception getReception()
   {
      return reception;
   }

   public Set<Annotation> getObservedQualifiers()
   {
      return bindings;
   }

   public Type getObservedType()
   {
      return eventType;
   }

   public TransactionPhase getTransactionPhase()
   {
      return transactionPhase;
   }

   /**
    * @return the observerMethod
    */
   public MethodInjectionPoint<T, ? super X> getMethod()
   {
      return observerMethod;
   }

   /**
    * Completes initialization of the observer and allows derived types to
    * override behavior.
    */
   public void initialize()
   {
      checkObserverMethod();
   }

   public void notify(final T event)
   {
      if (ignore(event))
      {
         return;
      }
      sendEvent(event);
   }

   /**
    * Invokes the observer method immediately passing the event.
    * 
    * @param event The event to notify observer with
    */
   protected void sendEvent(final T event)
   {
      if (observerMethod.isStatic())
      {
         sendEvent(event, null, beanManager.createCreationalContext(declaringBean));
      }
      else if (reception.equals(Reception.IF_EXISTS))
      {
         Object receiver = getReceiverIfExists();
         // The observer is conditional, and there is no existing bean
         if (receiver != null)
         {
            sendEvent(event, receiver, null);
         }
      }
      else
      {
         CreationalContext<?> creationalContext = beanManager.createCreationalContext(declaringBean);
         Object receiver = beanManager.getReference(declaringBean, creationalContext, false);
         sendEvent(event, receiver, creationalContext);
      }

   }

   private void sendEvent(T event, Object receiver, CreationalContext<?> creationalContext)
   {
      try
      {
         if (receiver == null)
         {
            observerMethod.invokeWithSpecialValue(receiver, Observes.class, event, beanManager, creationalContext, ObserverException.class);
         }
         else
         {
            // As we are working with the contextual instance, we may not have the
            // actual object, but a container proxy (e.g. EJB)
            observerMethod.invokeOnInstanceWithSpecialValue(receiver, Observes.class, event, beanManager, creationalContext, ObserverException.class);
         }
      }
      finally
      {
         if (creationalContext != null && Dependent.class.equals(declaringBean.getScope()))
         {
            creationalContext.release();
         }
      }
   }
   
   private Object getReceiverIfExists()
   {
      try
      {
         return beanManager.getReference(declaringBean, null, false);
      }
      catch (ContextNotActiveException e)
      {
         return null;
      }
   }
   
   protected boolean ignore(T event)
   {
      Class<?> eventType = event.getClass();
      // This is a container lifeycle event, ensure we are firing to an extension
      if (AbstractContainerEvent.class.isAssignableFrom(eventType) && !Extension.class.isAssignableFrom(getBeanClass()))
      {
         return true;
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public String toString()
   {
      return observerMethod.toString();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof ObserverMethodImpl<?, ?>)
      {
         ObserverMethodImpl<?, ?> that = (ObserverMethodImpl<?, ?>) obj;
         return this.getId().equals(that.getId());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return getId().hashCode();
   }

}
