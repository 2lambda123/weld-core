/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.logging;

import static org.jboss.weld.logging.WeldLogger.WELD_PROJECT_CODE;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.InconsistentSpecializationException;
import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.exceptions.UnserializableDependencyException;

/**
 * Log messages for validation related classes.
 *
 * Message IDs: 001400 - 001499
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface ValidatorLogger extends WeldLogger {

    ValidatorLogger LOG = Logger.getMessageLogger(ValidatorLogger.class, Category.VALIDATOR.getName());

    LogMessageCallback INJECTION_INTO_NON_DEPENDENT_BEAN_CALLBACK = new LogMessageCallback() {
        @Override
        public String invoke(Object... params) {
            return ValidatorLogger.LOG.injectionIntoNonDependentBean(params[0]);
        }
    };

    LogMessageCallback INJECTION_INTO_DISPOSER_METHOD_CALLBACK = new LogMessageCallback() {
        @Override
        public String invoke(Object... params) {
            return ValidatorLogger.LOG.injectionIntoDisposerMethod(params[0]);
        }
    };

    LogMessageCallback INJECTION_INTO_NON_BEAN_CALLBACK = new LogMessageCallback() {
        @Override
        public String invoke(Object... params) {
            return ValidatorLogger.LOG.injectionIntoNonBean(params[0]);
        }
    };

    LogMessageCallback INTERCEPTOR_SPECIFIED_TWICE_CALLBACK = new LogMessageCallback() {
        @Override
        public String invoke(Object... params) {
            return ValidatorLogger.LOG.interceptorSpecifiedTwice(params[0], params[1], params[2]);
        }
    };

    LogMessageCallback DECORATOR_SPECIFIED_TWICE_CALLBACK = new LogMessageCallback() {
        @Override
        public String invoke(Object... params) {
            return ValidatorLogger.LOG.decoratorSpecifiedTwice(params[0], params[1], params[2]);
        }
    };

    LogMessageCallback ALTERNATIVE_CLASS_SPECIFIED_MULTIPLE_TIMES_CALLBACK = new LogMessageCallback() {
        @Override
        public String invoke(Object... params) {
            return ValidatorLogger.LOG.alternativeClassSpecifiedMultipleTimes(params[0]);
        }
    };

    LogMessageCallback ALTERNATIVE_STEREOTYPE_SPECIFIED_MULTIPLE_TIMES_CALLBACK = new LogMessageCallback() {
        @Override
        public String invoke(Object... params) {
            return ValidatorLogger.LOG.alternativeStereotypeSpecifiedMultipleTimes(params[0], params[1], params[2]);
        }
    };

    @Message(id = 1401, value = "Two beans cannot specialize the same bean {0}", format = Format.MESSAGE_FORMAT)
    InconsistentSpecializationException beanSpecializedTooManyTimes(Object param1);

    @Message(id = 1402, value = "The bean {0} declares a passivating scope but has a non-serializable interceptor {1}", format = Format.MESSAGE_FORMAT)
    DeploymentException passivatingBeanWithNonserializableInterceptor(Object param1, Object param2);

    @Message(id = 1403, value = "The bean {0} declares a passivating scope but has a non-serializable decorator {1}", format = Format.MESSAGE_FORMAT)
    UnserializableDependencyException passivatingBeanWithNonserializableDecorator(Object param1, Object param2);

    @Message(id = 1404, value = "The injection point {0} is annotated with @New which cannot be combined with other qualifiers", format = Format.MESSAGE_FORMAT)
    DefinitionException newWithQualifiers(Object param1);

    @Message(id = 1405, value = "Cannot inject {0} in a class which isn't a bean", format = Format.MESSAGE_FORMAT)
    String injectionIntoNonBean(Object param1);

    @Message(id = 1406, value = "Cannot inject {0} in a non @Dependent scoped bean", format = Format.MESSAGE_FORMAT)
    String injectionIntoNonDependentBean(Object param1);

    @Message(id = 1407, value = "Cannot declare an injection point with a type variable: {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException injectionPointWithTypeVariable(Object param1);

    @Message(id = 1408, value = "Unsatisfied dependencies for type {2} with qualifiers {1}\n  at injection point {0}\n  at {3}\n{4}", format = Format.MESSAGE_FORMAT)
    DeploymentException injectionPointHasUnsatisfiedDependencies(Object param1, Object param2, Object param3, Object param4, Object param5);

    @Message(id = 1409, value = "Ambiguous dependencies for type {2} with qualifiers {1}\n  at injection point {0}\n  at {3}\n  Possible dependencies: {4}\n", format = Format.MESSAGE_FORMAT)
    DeploymentException injectionPointHasAmbiguousDependencies(Object param1, Object param2, Object param3, Object param4, Object param5);

    @Message(id = 1410, value = "The injection point {0} has non-proxyable dependencies", format = Format.MESSAGE_FORMAT)
    DeploymentException injectionPointHasNonProxyableDependencies(Object param1, @Cause Throwable cause);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 1411, value = "Injection point {0} has nullable dependencies", format = Format.MESSAGE_FORMAT)
    String injectionPointHasNullableDependencies(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 1412, value = "The bean {0} declares a passivating scope but the producer returned a non-serializable bean for injection:  {1}", format = Format.MESSAGE_FORMAT)
    String nonSerializableBeanInjectedIntoPassivatingBean(Object param1, Object param2);

    @Message(id = 1413, value = "The bean {0} declares a passivating scope but has a non-passivation-capable dependency {1}", format = Format.MESSAGE_FORMAT)
    UnserializableDependencyException injectionPointHasNonSerializableDependency(Object param1, Object param2);

    @Message(id = 1414, value = "Bean name is ambiguous. Name {0} resolves to beans: {1}", format = Format.MESSAGE_FORMAT)
    DeploymentException ambiguousElName(Object param1, Object param2);

    @Message(id = 1415, value = "Bean name is identical to a bean name prefix used elsewhere. Name {0}", format = Format.MESSAGE_FORMAT)
    DeploymentException beanNameIsPrefix(Object param1);

    @Message(id = 1416, value = "Enabled interceptor class {0} specified twice:\n  - {1},\n  - {2}", format = Format.MESSAGE_FORMAT)
    String interceptorSpecifiedTwice(Object param1, Object param2, Object param3);

    @Message(id = 1417, value = "Enabled interceptor class {0} does not match an interceptor bean: the class is not found, or not annotated with @Interceptor and still not registered through a portable extension, or not annotated with @Dependent inside an implicit bean archive", format = Format.MESSAGE_FORMAT)
    DeploymentException interceptorClassDoesNotMatchInterceptorBean(Object param1);

    @Message(id = 1418, value = "Enabled decorator class {0} specified twice:\n  - {1},\n  - {2}", format = Format.MESSAGE_FORMAT)
    String decoratorSpecifiedTwice(Object param1, Object param2, Object param3);

    @Message(id = 1419, value = "Enabled decorator class {0} is not the bean class of at least one decorator bean (detected decorator beans: {1})", format = Format.MESSAGE_FORMAT)
    DeploymentException decoratorClassNotBeanClassOfDecorator(Object param1, Object param2);

    @Message(id = 1420, value = "Enabled alternative {0} is not a stereotype", format = Format.MESSAGE_FORMAT)
    DeploymentException alternativeStereotypeNotStereotype(Object param1);

    @Message(id = 1421, value = "Cannot enable the same alternative stereotype {0} in beans.xml:\n  - {1},\n  - {2}", format = Format.MESSAGE_FORMAT)
    String alternativeStereotypeSpecifiedMultipleTimes(Object param1, Object param2, Object param3);

    @Message(id = 1422, value = "Enabled alternative {0} is not an alternative", format = Format.MESSAGE_FORMAT)
    DeploymentException alternativeBeanClassNotAnnotated(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 1423, value = "Cannot enable the same alternative bean class {0} in beans.xml:\n  - {1},\n  - {2}", format = Format.MESSAGE_FORMAT)
    String alternativeBeanClassSpecifiedMultipleTimes(Object param1, Object param2, Object param3);

    @Message(id = 1424, value = "The following disposal methods were declared but did not resolve to a producer method: {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException disposalMethodsWithoutProducer(Object param1);

    @Message(id = 1425, value = "An injection point of type {0} cannot have a wildcard type parameter:  {1}", format = Format.MESSAGE_FORMAT)
    DefinitionException injectionPointHasWildcard(Object param1, Object param2);

    @Message(id = 1426, value = "An injection point of type {0} must have a type parameter:  {1}", format = Format.MESSAGE_FORMAT)
    DefinitionException injectionPointMustHaveTypeParameter(Object param1, Object param2);

    @Message(id = 1427, value = "Only field injection points can use the @Named qualifier with no value. {0} is not a field injection point.", format = Format.MESSAGE_FORMAT)
    DefinitionException nonFieldInjectionPointCannotUseNamed(Object param1);

    @Message(id = 1428, value = "A decorator cannot have producer methods, but at least one was found on {0}.", format = Format.MESSAGE_FORMAT)
    DefinitionException decoratorsCannotHaveProducerMethods(Object param1);

    @Message(id = 1429, value = "A decorator cannot have producer fields, but at least one was found on {0}.", format = Format.MESSAGE_FORMAT)
    DefinitionException decoratorsCannotHaveProducerFields(Object param1);

    @Message(id = 1430, value = "A decorator cannot have disposer methods, but at least one was found on {0}.", format = Format.MESSAGE_FORMAT)
    DefinitionException decoratorsCannotHaveDisposerMethods(Object param1);

    @Message(id = 1431, value = "An interceptor cannot have producer methods, but at least one was found on {0}.", format = Format.MESSAGE_FORMAT)
    DefinitionException interceptorsCannotHaveProducerMethods(Object param1);

    @Message(id = 1432, value = "An interceptor cannot have producer fields, but at least one was found on {0}.", format = Format.MESSAGE_FORMAT)
    DefinitionException interceptorsCannotHaveProducerFields(Object param1);

    @Message(id = 1433, value = "An interceptor cannot have disposer methods, but at least one was found on {0}.", format = Format.MESSAGE_FORMAT)
    DefinitionException interceptorsCannotHaveDisposerMethods(Object param1);

    @Message(id = 1434, value = "Normal scoped bean {0} is not proxyable for an unknown reason - {1}.", format = Format.MESSAGE_FORMAT)
    UnproxyableResolutionException notProxyableUnknown(Object param1, Object param2);

    @Message(id = 1435, value = "Normal scoped bean {0} is not proxyable because it has no no-args constructor - {1}.", format = Format.MESSAGE_FORMAT)
    UnproxyableResolutionException notProxyableNoConstructor(Object param1, Object param2);

    @Message(id = 1436, value = "Normal scoped bean {0} is not proxyable because it has a private constructor {1} - {2}.", format = Format.MESSAGE_FORMAT)
    String notProxyablePrivateConstructor(Object param1, Object param2, Object param3);

    @Message(id = 1437, value = "Normal scoped bean {0} is not proxyable because the type is final or it contains a final method {1} - {2}.", format = Format.MESSAGE_FORMAT)
    UnproxyableResolutionException notProxyableFinalTypeOrMethod(Object param1, Object param2, Object param3);

    @Message(id = 1438, value = "Normal scoped bean {0} is not proxyable because it is a primitive - {1}.", format = Format.MESSAGE_FORMAT)
    UnproxyableResolutionException notProxyablePrimitive(Object param1, Object param2);

    @Message(id = 1439, value = "Normal scoped bean {0} is not proxyable because it is an array type - {1}.", format = Format.MESSAGE_FORMAT)
    UnproxyableResolutionException notProxyableArrayType(Object param1, Object param2);

    @LogMessage(level = Level.WARN)
    @Message(id = 1440, value = "Scope type {0} used on injection point {1}", format = Format.MESSAGE_FORMAT)
    void scopeAnnotationOnInjectionPoint(Object param1, Object param2);

    @Message(id = 1441, value = "Enabled alternative {0} is not a class", format = Format.MESSAGE_FORMAT)
    DeploymentException alternativeBeanClassNotClass(Object param1);

    @Message(id = 1442, value = "Enabled alternative {0} is not annotated @Alternative", format = Format.MESSAGE_FORMAT)
    DeploymentException alternativeStereotypeNotAnnotated(Object param1);

    @Message(id = 1443, value = "Pseudo scoped bean has circular dependencies. Dependency path: {0}", format = Format.MESSAGE_FORMAT)
    DeploymentException pseudoScopedBeanHasCircularReferences(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 1444, value = "Cannot inject {0} {1} into {2}", format = Format.MESSAGE_FORMAT)
    String cannotInjectBeanMetadata(Object param1, Object param2, Object param3);

    @Message(id = 1445, value = "An interceptor cannot have observer methods, but at least one was found on {0}.", format = Format.MESSAGE_FORMAT)
    DefinitionException interceptorsCannotHaveObserverMethods(Object param1);

    @Message(id = 1446, value = "A decorator cannot have observer methods, but at least one was found on {0}.", format = Format.MESSAGE_FORMAT)
    DefinitionException decoratorsCannotHaveObserverMethods(Object param1);

    @Message(id = 1447, value = "Method {0} defined on class {1} is not defined according to the specification. It is annotated with @{2} but it does not return {3}.", format = Format.MESSAGE_FORMAT)
    DefinitionException interceptorMethodDoesNotReturnObject(Object param1, Object param2, Object param3, Object param4);

    @Message(id = 1448, value = "Method {0} defined on class {1} is not defined according to the specification. It is annotated with @{2} but it does not have exactly one parameter.", format = Format.MESSAGE_FORMAT)
    DefinitionException interceptorMethodDoesNotHaveExactlyOneParameter(Object param1, Object param2, Object param3);

    @Message(id = 1449, value = "Method {0} defined on class {1} is not defined according to the specification. It is annotated with @{2} but its single parameter is not a {3}.", format = Format.MESSAGE_FORMAT)
    DefinitionException interceptorMethodDoesNotHaveCorrectTypeOfParameter(Object param1, Object param2, Object param3, Object param4);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 1450, value = "Interceptor method {0} does not declare that it throws Exception.", format = Format.MESSAGE_FORMAT)
    String interceptorMethodDoesNotThrowException(Object param1);

    @Message(id = 1451, value = "javax.transaction.UserTransaction cannot be injected into an enterprise bean with container-managed transactions {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException userTransactionInjectionIntoBeanWithContainerManagedTransactions(Object param1);

    @Message(id = 1452, value = "{0} is not a valid type for a Bean metadata injection point {1}", format = Format.MESSAGE_FORMAT)
    DefinitionException invalidBeanMetadataInjectionPointType(Object param1, Object param2);

    @Message(id = 1453, value = "{0} is not a valid type argument for a Bean metadata injection point {1}", format = Format.MESSAGE_FORMAT)
    DefinitionException invalidBeanMetadataInjectionPointTypeArgument(Object param1, Object param2);

    @Message(id = 1454, value = "{0} cannot be used at a Bean metadata injection point of a bean which is not {1}, {2}", format = Format.MESSAGE_FORMAT)
    DefinitionException invalidBeanMetadataInjectionPointQualifier(Object param1, Object param2, Object param3);

    @Message(id = 1455, value = "{0} does not declare any decorated types.", format = Format.MESSAGE_FORMAT)
    DefinitionException noDecoratedTypes(Object param1);

    @Message(id = 1456, value = "Argument {0} must not be null", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException argumentNull(Object param1);

    @Message(id = 1457, value = "Cannot enable the same alternative class {0} in beans.xml", format = Format.MESSAGE_FORMAT)
    String alternativeClassSpecifiedMultipleTimes(Object param1);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 1458, value = "Locally disabled {0} does not match any global enablement. {1}", format = Format.MESSAGE_FORMAT)
    String noGloballyEnabledClassMatchingLocalDisable(Object param1, Object param2);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 1459, value = "Class {0} is enabled but no priority is set for it. Either enable the class globally by providing the priority attribute or do not use the enabled flag explicitly. {1}", format = Format.MESSAGE_FORMAT)
    String enabledFlagUsedWithoutPrioritySet(Object param1, Object param2);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 1460, value = "Globally enabled alternative {0} is not present in the bean archive that enables it globally {1}", format = Format.MESSAGE_FORMAT)
    String globallyEnabledAlternativeNotInArchive(Object param1, Object param2);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 1461, value = "Globally enabled interceptor {0} is not present in the bean archive that enables it globally {1}", format = Format.MESSAGE_FORMAT)
    String globallyEnabledInterceptorNotInArchive(Object param1, Object param2);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 1462, value = "Globally enabled decorator {0} is not present in the bean archive that enables it globally {1}", format = Format.MESSAGE_FORMAT)
    String globallyEnabledDecoratorNotInArchive(Object param1, Object param2);

    @Message(id = 1463, value = "Bean declaring a passivating scope must be passivation capable.  Bean:  {0}", format = Format.MESSAGE_FORMAT)
    DeploymentException beanWithPassivatingScopeNotPassivationCapable(Object param1);

    @Message(id = 1465, value = "{0} for a built-in bean {1} must be passivation capable.", format = Format.MESSAGE_FORMAT)
    UnserializableDependencyException builtinBeanWithNonserializableDecorator(Object param1, Object param2);

    @Message(id = 1466, value = "Cannot inject {0} in a disposer method", format = Format.MESSAGE_FORMAT)
    String injectionIntoDisposerMethod(Object param1);

    @Message(id = 1467, value = "Method {0} defined on class {1} is not defined according to the specification. It is annotated with @{2} but it does not return {3} or {4}.", format = Format.MESSAGE_FORMAT)
    DefinitionException interceptorMethodDoesNotReturnObjectOrVoid(Object param1, Object param2, Object param3, Object param4, Object param5);

    @Message(id = 1468, value = "Method {0} defined on class {1} is not defined according to the specification. It is annotated with @{2} but it does not have a {3} return type.", format = Format.MESSAGE_FORMAT)
    DefinitionException interceptorMethodDoesNotHaveVoidReturnType(Object param1, Object param2, Object param3, Object param4);

    @Message(id = 1469, value = "Method {0} defined on class {1} is not defined according to the specification. It is annotated with @{2} but it does not have zero parameters.", format = Format.MESSAGE_FORMAT)
    DefinitionException interceptorMethodDoesNotHaveZeroParameters(Object param1, Object param2, Object param3);

    /**
     * @deprecated Not in use
     */
    @Deprecated
    @Message(id = 1470, value = "Method {0} defined on target class {1} is a @{2} callback method and should not be defined on the target class.", format = Format.MESSAGE_FORMAT)
    String aroundConstructInterceptorMethodNotAllowedOnTargetClass(Object param1, Object param2, Object param3);

    @LogMessage(level = Level.WARN)
    @Message(id = 1471, value = "Interceptor method {0} defined on class {1} is not defined according to the specification. It should not throw {2}, which is a checked exception.", format = Format.MESSAGE_FORMAT)
    void interceptorMethodShouldNotThrowCheckedExceptions(Object param1, Object param2, Object param3);

    @Message(id = 1472, value = "EventMetadata can only be injected into an observer method. {0}", format = Format.MESSAGE_FORMAT)
    DefinitionException eventMetadataInjectedOutsideOfObserver(Object param1);

    @LogMessage(level = Level.WARN)
    @Message(id = 1473, value = "javax.enterprise.inject.spi.Bean implementation {0} declared a normal scope but does not implement javax.enterprise.inject.spi.PassivationCapable. It won'''t be possible to inject this bean into a bean with a passivating scope (@SessionScoped, @ConversationScoped). This can be fixed by assigning the Bean implementation a unique id by implementing the PassivationCapable interface.", format = Format.MESSAGE_FORMAT)
    void beanNotPassivationCapable(Object param1);

    @Message(id = 1474, value = "Class {0} is on the classpath, but was ignored because a class it references was not found: {1}.\n", format = Format.MESSAGE_FORMAT)
    String unsatisfiedDependencyBecauseClassIgnored(Object param1, Object param2);

    @Message(id = 1475, value = "The following beans match by type, but none have matching qualifiers:{0}\n", format = Format.MESSAGE_FORMAT)
    String unsatisfiedDependencyBecauseQualifiersDontMatch(Object param1);

    @Message(id = 1476, value = "{0} must be @Dependent", format = Format.MESSAGE_FORMAT)
    DefinitionException interceptorMustBeDependent(Object param1);

    @Message(id = 1477, value = "The bean {0} declares a passivating scope but has a(n) {1} with a non-passivation-capable dependency {2}", format = Format.MESSAGE_FORMAT)
    UnserializableDependencyException interceptorDecoratorInjectionPointHasNonSerializableDependency(Object param1, Object param2, Object param3);
}