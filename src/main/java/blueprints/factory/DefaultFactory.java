package blueprints.factory;

import blueprints.Blueprint;
import blueprints.ConfigurationDSL;
import blueprints.Context;
import blueprints.Factory;
import blueprints.Sequence;
import blueprints.UnsafeOperation;
import blueprints.factory.builder.BuildStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
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
    private final List<Consumer<D>> traits;

    public DefaultFactory(FactorySupport support, Class<?> blueprint, Class<D> dslClass)
    {
        this(support, blueprint, dslClass, new ArrayList<>());
    }

    private DefaultFactory(FactorySupport support, Class<?> blueprint, Class<D> dslClass, List<Consumer<D>> traits)
    {
        verifyBlueprint(blueprint);

        this.support = support;
        this.blueprint = blueprint;
        this.dslClass = dslClass;
        this.traits = traits;
        this.sequence = new Sequence();
        this.strategy = support.buildStrategyFor(blueprint);
    }

    @Override
    public Factory<T, D> with(Consumer<D>... newTraits)
    {
        List<Consumer<D>> traitsCopy = new ArrayList<>(traits);
        Arrays.stream(newTraits).forEach(traitsCopy::add);

        return new DefaultFactory<>(support, blueprint, dslClass, traitsCopy);
    }

    public T create()
    {
        return create(new HashMap<>(), (cofiguration) -> {});
    }

    public T create(Map<String, Object> createProperties)
    {
        return create(createProperties, (cofiguration) -> {});
    }

    public T create(Consumer<D> configuration)
    {
        return create(new HashMap<>(), configuration);
    }

    public T create(Map<String, Object> createProperties, Consumer<D> configuration)
    {
        BlueprintInspector blueprintInspector = support.inspectorFor(blueprint, sequence);
        List<BiConsumer> afterHooks = blueprintInspector.extractAfterHooks();
        Map<String, Object> properties = blueprintInspector.extractDefaults();
        D dsl = support.dslFor(dslClass, properties, afterHooks);
        traits.forEach(trait -> trait.accept(dsl));
        configuration.accept(dsl);

        T newInstance = strategy.apply(properties);
        Context context = support.createContext(createProperties);
        afterHooks.forEach(hook -> hook.accept(newInstance, context));

        return newInstance;
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
