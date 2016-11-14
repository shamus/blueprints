package blueprints.factory;

import blueprints.Blueprint;
import blueprints.ConfigurationDSL;
import blueprints.Context;
import blueprints.Factories;
import blueprints.Factory;
import blueprints.Sequence;
import blueprints.factory.builder.BuildStrategy;
import blueprints.factory.builder.BuilderPatternBuildStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public interface FactorySupport
{
    static FactorySupport defaultSupport(Factories factories)
    {
        return new FactorySupport()
        {
            private PropertyExtractor propertyExtractor = new PropertyExtractor();

            @Override
            public Context createContext()
            {
                return createContext(new HashMap<>());
            }

            @Override
            public Context createContext(Map<String, Object> values)
            {
                return new Context() {
                    @Override
                    public Object get(String name)
                    {
                        return values.get(name);
                    }

                    @Override
                    public <T, D extends ConfigurationDSL<T>> Factory<T, D> getFactory(Class<T> modelClass)
                    {
                        return factories.get(modelClass);
                    }
                };
            }

            @Override
            public <T> BuildStrategy<T> buildStrategyFor(Class<?> blueprint)
            {
                Blueprint annotation = blueprint.getAnnotation(Blueprint.class);
                Class<T> modelClass = annotation.value();

                return new BuilderPatternBuildStrategy<>(modelClass);
            }

            @Override
            public <T> T dslFor(Class<T> configurationDSL, Map<String, Object> properties, List<BiConsumer> afterHooks)
            {
                return ReflectiveProxyConfigurationDSL.proxying(configurationDSL, properties, afterHooks);
            }

            @Override
            public Map<String, Object> extractDefaultsFrom(Class<?> blueprint, Sequence sequence)
            {
                return propertyExtractor.extractDefaults(blueprint, sequence);
            }
        };
    }

    Context createContext();
    Context createContext(Map<String, Object> values);
    <T> BuildStrategy<T> buildStrategyFor(Class<?> blueprint);
    <T> T dslFor(Class<T> configurationDSL, Map<String, Object> properties, List<BiConsumer> afterHooks);
    Map<String, Object> extractDefaultsFrom(Class<?> blueprint, Sequence sequence);
}
