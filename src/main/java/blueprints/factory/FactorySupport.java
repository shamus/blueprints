package blueprints.factory;

import blueprints.Blueprint;
import blueprints.builder.BuildStrategy;
import blueprints.builder.BuilderPatternBuildStrategy;

import java.util.Map;

public interface FactorySupport
{
    FactorySupport defaultSupport = new FactorySupport()
    {
        private PropertyExtractor propertyExtractor = new PropertyExtractor();

        @Override
        public <T> BuildStrategy<T> buildStrategyFor(Class<?> blueprint)
        {
            Blueprint annotation = blueprint.getAnnotation(Blueprint.class);
            Class<T> modelClass = annotation.value();

            return new BuilderPatternBuildStrategy<T>(modelClass);
        }

        @Override
        public <T> T dslFor(Class<T> configurationDSL, Map<String, Object> properties)
        {
            return ReflectiveProxyConfigurationDSL.proxying(configurationDSL, properties);
        }

        @Override
        public Map<String, Object> extractDefaultsFrom(Class<?> blueprint)
        {
            return propertyExtractor.extractDefaults(blueprint);
        }
    };

    <T> BuildStrategy<T> buildStrategyFor(Class<?> blueprint);
    <T> T dslFor(Class<T> configurationDSL, Map<String, Object> properties);
    Map<String, Object> extractDefaultsFrom(Class<?> blueprint);
}
