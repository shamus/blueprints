package blueprints.factory;

import blueprints.Blueprint;
import blueprints.ConfigurationDSL;
import blueprints.Sequence;
import blueprints.Factory;
import blueprints.UnsafeOperation;
import blueprints.factory.builder.BuildStrategy;

import java.util.Map;
import java.util.function.Consumer;

import static java.lang.String.format;

public class DefaultFactory<T, D extends ConfigurationDSL<T>>
    implements Factory<T, D>
{
    private static final String NO_DEFAULT_CONSTRUCTOR = "%s does not have a public default constructor";
    private static final String MISSING_ANNOTATION = "%s does is not annotated with %s";

    private FactorySupport support;
    private Class<?> blueprint;
    private Class<D> dslClass;
    private BuildStrategy<T> strategy;
    private Sequence sequence;

    public DefaultFactory(FactorySupport support, Class<?> blueprint, Class<D> dslClass)
    {
        verifyBlueprint(blueprint);

        this.support = support;
        this.blueprint = blueprint;
        this.dslClass = dslClass;
        this.strategy = support.buildStrategyFor(blueprint);
        this.sequence = new Sequence();
    }

    public T create()
    {
        return create((cofiguration) -> {});
    }

    public T create(Consumer<D> configuration)
    {
        Map<String, Object> properties = support.extractDefaultsFrom(blueprint, sequence);
        D dsl = support.dslFor(dslClass, properties);
        configuration.accept(dsl);

        return strategy.apply(properties);
    }

    private void verifyBlueprint(Class<?> blueprint)
    {
        UnsafeOperation.heedlessly((e) -> {
            throw new InvalidBlueprintDefinitionException(format(NO_DEFAULT_CONSTRUCTOR, blueprint));
        }, blueprint::newInstance);

        if (blueprint.getAnnotation(Blueprint.class) == null) {
            throw new InvalidBlueprintDefinitionException(format(MISSING_ANNOTATION, blueprint, Blueprint.class));
        }
    }
}
