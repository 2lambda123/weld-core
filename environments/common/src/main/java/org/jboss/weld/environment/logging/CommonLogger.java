package org.jboss.weld.environment.logging;


import javax.enterprise.inject.UnsatisfiedResolutionException;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.resources.spi.ClassFileInfoException;

/**
 *
 * @author Matej Briškár
 * @author Martin Kouba
 */
@MessageLogger(projectCode = WeldEnvironmentLogger.WELD_ENV_PROJECT_CODE)
public interface CommonLogger extends WeldEnvironmentLogger {

    CommonLogger LOG = Logger.getMessageLogger(CommonLogger.class, Category.BOOTSTRAP.getName());

    @LogMessage(level = Level.WARN)
    @Message(id = 2, value = "Could not read resource with name: {0}", format = Format.MESSAGE_FORMAT)
    void couldNotReadResource(Object param1, @Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 4, value = "Could not invoke JNLPClassLoader#getJarFile(URL) on context class loader, expecting Web Start class loader", format = Format.MESSAGE_FORMAT)
    void unexpectedClassLoader(@Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 5, value = "JNLPClassLoader#getJarFile(URL) threw exception", format = Format.MESSAGE_FORMAT)
    void jnlpClassLoaderInternalException(@Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 6, value = "Could not invoke JNLPClassLoader#getJarFile(URL) on context class loader", format = Format.MESSAGE_FORMAT)
    void jnlpClassLoaderInvocationException(@Cause Throwable cause);

    @Message(id = 7, value = "Error handling file {0}", format = Format.MESSAGE_FORMAT)
    RuntimeException cannotHandleFile(Object param1, @Cause Throwable cause);

    @LogMessage(level = Level.ERROR)
    @Message(id = 8, value = "Error loading file {0}", format = Format.MESSAGE_FORMAT)
    void errorLoadingFile(Object param1);

    @LogMessage(level = Level.WARN)
    @Message(id = 10, value = "Could not open the stream on the url {0} when adding to the jandex index.", format = Format.MESSAGE_FORMAT)
    void couldNotOpenStreamForURL(Object param1, @Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 11, value = "Could not close the stream on the url {0} when adding to the jandex index.", format = Format.MESSAGE_FORMAT)
    void couldNotCloseStreamForURL(Object param1, @Cause Throwable cause);

    @Message(id = 12, value = "Unable to load class {0}", format = Format.MESSAGE_FORMAT)
    ClassFileInfoException unableToLoadClass(Object param1);

    @Message(id = 13, value = "beans.xml defines unrecognized bean-discovery-mode value: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException undefinedBeanDiscoveryValue(Object param1);

    @Message(id = 14, value = "bean-discovery-mode=\"annotated\" support is disabled. Add org.jboss:jandex to the classpath to enable it.", format = Format.MESSAGE_FORMAT)
    IllegalStateException annotatedBeanDiscoveryNotSupported();

    @Message(id = 15, value = "Unable to load annotation: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException unableToLoadAnnotation(Object param1);

    @Message(id = 16, value = "Missing beans.xml file in META-INF", format = Format.MESSAGE_FORMAT)
    IllegalStateException missingBeansXml();

    @Message(id = 17, value = "Error loading Weld bootstrap, check that Weld is on the classpath", format = Format.MESSAGE_FORMAT)
    IllegalStateException errorLoadingWeld();

    @Message(id = 18, value = "Unable to resolve a bean for {0} with bindings {1}", format = Format.MESSAGE_FORMAT)
    UnsatisfiedResolutionException unableToResolveBean(Object param1, Object param2);

    @Message(id = 19, value = "Jandex index is null in the constructor of class: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException jandexIndexNotCreated(Object param1);

}
