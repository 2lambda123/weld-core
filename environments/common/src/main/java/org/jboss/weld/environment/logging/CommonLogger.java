package org.jboss.weld.environment.logging;


import java.net.URL;

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
 * Message IDs: 000002 - 000099
 *
 * @author Matej Briškár
 * @author Martin Kouba
 * @author Kirill Gaevskii
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

    @LogMessage(level = Level.INFO)
    @Message(id = 14, value = "Falling back to Java Reflection for bean-discovery-mode=\"annotated\" discovery. Add org.jboss:jandex to the classpath to speed-up startup.", format = Format.MESSAGE_FORMAT)
    void reflectionFallback();

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

    @LogMessage(level = Level.INFO)
    @Message(id = 20, value = "Using jandex for bean discovery", format = Format.MESSAGE_FORMAT)
    void usingJandex();

    @LogMessage(level = Level.WARN)
    @Message(id = 21, value = "Could not close the stream for of the jandex index file for {0}.", format = Format.MESSAGE_FORMAT)
    void couldNotCloseStreamOfJandexIndex(Object param1, @Cause Throwable cause);

    @LogMessage(level = Level.INFO)
    @Message(id = 22, value = "Found jandex index at {0}", format = Format.MESSAGE_FORMAT)
    void foundJandexIndex(URL url);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 23, value = "Archive isolation disabled - only one bean archive will be created", format = Format.MESSAGE_FORMAT)
    void archiveIsolationDisabled();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 24, value = "Archive isolation enabled - creating multiple isolated bean archives if needed", format = Format.MESSAGE_FORMAT)
    void archiveIsolationEnabled();

    @Message(id = 25, value = "Index for name: {0} not found.", format = Format.MESSAGE_FORMAT)
    IllegalStateException indexForNameNotFound(Object param1);

    @Message(id = 26, value = "Unable to instantiate {0} using parameters: {1}.", format = Format.MESSAGE_FORMAT)
    IllegalStateException unableToInstantiate(Object param1, Object param2, @Cause Throwable cause);

    @Message(id = 27, value = "Unable to find constructor for of {0} accepting parameters: {1}.", format = Format.MESSAGE_FORMAT)
    IllegalStateException unableToFindConstructor(Object param1, Object param2);
}
