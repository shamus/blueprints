package blueprints.runtime;

import blueprints.Blueprint;
import blueprints.ConfigurationDSL;
import blueprints.DerivedFrom;
import blueprints.Factories;
import blueprints.factory.DefaultFactory;
import blueprints.Factory;
import blueprints.factory.FactorySupport;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import static blueprints.UnsafeOperation.heedlessly;

public class FactoryScanner
    implements Factories
{
    private ConcurrentHashMap<Class, Factory> factories;

    public FactoryScanner(String... pkgs)
    {
        FactorySupport support = FactorySupport.defaultSupport(this);
        factories = new ConcurrentHashMap<>();
        Arrays.stream(pkgs).forEach(pkg -> {
            Reflections reflections = new Reflections(pkg);

            reflections.getSubTypesOf(ConfigurationDSL.class).stream().forEach(dsl -> {
                Class<?> blueprint = getClassFromAnnotation(dsl, DerivedFrom.class);
                Class<?> model = getClassFromAnnotation(blueprint, Blueprint.class);
                factories.put(model, new DefaultFactory<>(support, blueprint, dsl));
            });
        });
    }

    @Override
    public <T, D extends ConfigurationDSL<T>> Factory<T, D> get(Class<T> modelClass)
    {
        return factories.get(modelClass);
    }

    private Class<?> getClassFromAnnotation(Class<?> annotatedClass, Class<? extends Annotation> annotation)
    {
        return heedlessly(() -> {
            Method value = annotation.getDeclaredMethod("value");
            value.setAccessible(true);

            return (Class<?>) value.invoke(annotatedClass.getAnnotation(annotation));
        });
    }
}
