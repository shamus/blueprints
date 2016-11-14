package blueprints.factory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ReflectiveProxyConfigurationDSL
    implements InvocationHandler
{
    @SuppressWarnings("unchecked")
    public static <T> T proxying(Class<T> proxyClass, Map<String, Object> defaults, List<BiConsumer> afterHooks)
    {
        return (T) Proxy.newProxyInstance(
            proxyClass.getClassLoader(),
            new Class<?>[] { proxyClass },
            new ReflectiveProxyConfigurationDSL(defaults, afterHooks));
    }

    private Map<String, Object> properties;
    private List<BiConsumer> afterHooks;

    private ReflectiveProxyConfigurationDSL(Map<String, Object> defaults, List<BiConsumer> afterHooks)
    {
        this.properties = defaults;
        this.afterHooks = afterHooks;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
        if (method.getName().equals("after")) {
            afterHooks.add((BiConsumer) args[0]);
            return proxy;
        }

        properties.put(method.getName(), args[0]);
        return proxy;
    }
}
