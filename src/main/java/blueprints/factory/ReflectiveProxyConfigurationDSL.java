package blueprints.factory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class ReflectiveProxyConfigurationDSL
    implements InvocationHandler
{
    @SuppressWarnings("unchecked")
    public static <T> T proxying(Class<T> proxyClass, Map<String, Object> defaults)
    {
        return (T) Proxy.newProxyInstance(
            proxyClass.getClassLoader(),
            new Class<?>[] { proxyClass },
            new ReflectiveProxyConfigurationDSL(defaults));
    }

    private Map<String, Object> properties;

    private ReflectiveProxyConfigurationDSL(Map<String, Object> defaults)
    {
        this.properties = defaults;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
        properties.put(method.getName(), args[0]);
        return proxy;
    }
}
