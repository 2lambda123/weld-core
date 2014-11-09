package org.jboss.weld.interceptor.proxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.bean.proxy.MethodHandler;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author Marius Bogoevici
 * @author Marko Luksa
 * @author Jozef Hartinger
 */
public class InterceptorMethodHandler implements MethodHandler, Serializable {

    private static final long serialVersionUID = 1L;

    private final InterceptionContext ctx;
    private final transient ConcurrentMap<Method, List<InterceptorMethodInvocation>> cachedChains;

    public InterceptorMethodHandler(InterceptionContext ctx) {
        this.ctx = ctx;
        this.cachedChains = new ConcurrentHashMap<Method, List<InterceptorMethodInvocation>>();
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        SecurityActions.ensureAccessible(proceed);
        if (proceed == null) {
            if (thisMethod.getName().equals(InterceptionUtils.POST_CONSTRUCT)) {
                return executeInterception(self, null, null, null, InterceptionType.POST_CONSTRUCT);
            } else if (thisMethod.getName().equals(InterceptionUtils.PRE_DESTROY)) {
                return executeInterception(self, null, null, null, InterceptionType.PRE_DESTROY);
            }
        } else {
            if (isInterceptorMethod(thisMethod)) {
                return proceed.invoke(self, args);
            }
            return executeInterception(self, thisMethod, proceed, args, InterceptionType.AROUND_INVOKE);
        }
        return null;
    }

    protected Object executeInterception(Object instance, Method method, Method proceed, Object[] args, InterceptionType interceptionType) throws Throwable {
        List<InterceptorMethodInvocation> chain = getInterceptionChain(instance, method, interceptionType);
        if (chain.isEmpty()) {
            // shortcut if there are no interceptors
            if (proceed == null) {
                return null;
            } else {
                return Reflections.invokeAndUnwrap(instance, proceed, args);
            }
        } else {
            return new WeldInvocationContext(instance, method, proceed, args, chain).proceed();
        }
    }

    private List<InterceptorMethodInvocation> getInterceptionChain(Object instance, Method method, InterceptionType interceptionType) {
        if (method != null) {
            List<InterceptorMethodInvocation> chain = cachedChains.get(method);
            if (chain == null) {
                chain = ctx.buildInterceptorMethodInvocations(instance, method, interceptionType);
                List<InterceptorMethodInvocation> old = cachedChains.putIfAbsent(method, chain);
                if (old != null) {
                    chain = old;
                }
            }
            return chain;
        } else {
            return ctx.buildInterceptorMethodInvocations(instance, null, interceptionType);
        }
    }

    private boolean isInterceptorMethod(Method method) {
        return ctx.getInterceptionModel().getTargetClassInterceptorMetadata().isInterceptorMethod(method);
    }

    private Object readResolve() throws ObjectStreamException {
        return new InterceptorMethodHandler(ctx);
    }
}
