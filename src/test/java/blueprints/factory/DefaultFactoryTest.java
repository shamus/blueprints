package blueprints.factory;

import blueprints.Blueprint;
import blueprints.ConfigurationDSL;
import blueprints.Context;
import blueprints.Sequence;
import blueprints.factory.builder.BuildStrategy;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultFactoryTest
{
    private static final Object NEWLY_BUILT = new Object();
    private BuildStrategy buildStrategy;
    private FactorySupport factorySupport;
    private HashMap<String, Object> defaults;
    private Context context;
    private BlueprintInspector blueprintInspector;
    private ArrayList afterHooks;

    @Before
    public void setUp()
    {
        factorySupport = mock(FactorySupport.class);
        blueprintInspector = mock(BlueprintInspector.class);
        buildStrategy = mock(BuildStrategy.class);
        context = mock(Context.class);

        defaults = new HashMap<>();
        afterHooks = new ArrayList();
        when(factorySupport.createContext()).thenReturn(context);
        when(factorySupport.createContext(any())).thenReturn(context);
        when(factorySupport.buildStrategyFor(any())).thenReturn(buildStrategy);
        when(factorySupport.inspectorFor(any(), any())).thenReturn(blueprintInspector);
        when(blueprintInspector.extractDefaults()).thenReturn(defaults);
        when(blueprintInspector.extractAfterHooks()).thenReturn(afterHooks);
        when(factorySupport.dslFor(any(), any(), any())).thenAnswer(invocation ->
            ReflectiveProxyConfigurationDSL.proxying(
                invocation.getArgument(0),
                invocation.getArgument(1),
                invocation.getArgument(2))
        );

        when(buildStrategy.apply(any())).thenReturn(NEWLY_BUILT);
    }

    @Test
    public void shouldCreate()
    {
        DefaultFactory<Object, ModelConfiguration> factory = new DefaultFactory<>(
            factorySupport,
            ModelBlueprint.class,
            ModelConfiguration.class
        );

        assertThat(factory.create(), is(NEWLY_BUILT));

        verify(factorySupport).buildStrategyFor(ModelBlueprint.class);
        verify(factorySupport).inspectorFor(eq(ModelBlueprint.class), isA(Sequence.class));
        verify(factorySupport).dslFor(ModelConfiguration.class, defaults, afterHooks);
        verify(factorySupport).createContext(eq(new HashMap<>()));
        verify(blueprintInspector).extractDefaults();
        verify(blueprintInspector).extractAfterHooks();
        verify(buildStrategy).apply(defaults);
    }

    @Test
    public void shouldCreateWithCreateProperties()
    {
        DefaultFactory<Object, ModelConfiguration> factory = new DefaultFactory<>(
            factorySupport,
            ModelBlueprint.class,
            ModelConfiguration.class
        );

        Map<String, Object> properties = new HashMap<>();
        assertThat(factory.create(properties), is(NEWLY_BUILT));

        verify(factorySupport).buildStrategyFor(ModelBlueprint.class);
        verify(factorySupport).inspectorFor(eq(ModelBlueprint.class), isA(Sequence.class));
        verify(factorySupport).dslFor(ModelConfiguration.class, defaults, afterHooks);
        verify(factorySupport).createContext(properties);
        verify(blueprintInspector).extractDefaults();
        verify(blueprintInspector).extractAfterHooks();
        verify(buildStrategy).apply(defaults);
    }

    @Test
    public void shouldCreateWithConfiguration()
    {
        BiConsumer<Object, Context> afterHook = mock(BiConsumer.class);
        DefaultFactory<Object, ModelConfiguration> factory = new DefaultFactory<>(
            factorySupport,
            ModelBlueprint.class,
            ModelConfiguration.class
        );

        assertThat(factory.create(configuration -> configuration.after(afterHook)), is(NEWLY_BUILT));

        verify(factorySupport).buildStrategyFor(ModelBlueprint.class);
        verify(factorySupport).inspectorFor(eq(ModelBlueprint.class), isA(Sequence.class));
        verify(factorySupport).dslFor(ModelConfiguration.class, defaults, afterHooks);
        verify(factorySupport).createContext(eq(new HashMap<>()));
        verify(buildStrategy).apply(defaults);
        verify(blueprintInspector).extractDefaults();
        verify(blueprintInspector).extractAfterHooks();
        verify(afterHook).accept(NEWLY_BUILT, context);
    }

    @Test
    public void shouldCreateWithPropertiesAndConfiguration()
    {
        BiConsumer<Object, Context> afterHook = mock(BiConsumer.class);
        DefaultFactory<Object, ModelConfiguration> factory = new DefaultFactory<>(
            factorySupport,
            ModelBlueprint.class,
            ModelConfiguration.class
        );

        Map<String, Object> properties = new HashMap<>();
        assertThat(factory.create(properties, configuration -> configuration.after(afterHook)), is(NEWLY_BUILT));

        verify(factorySupport).buildStrategyFor(ModelBlueprint.class);
        verify(factorySupport).inspectorFor(eq(ModelBlueprint.class), isA(Sequence.class));
        verify(factorySupport).dslFor(ModelConfiguration.class, defaults, afterHooks);
        verify(factorySupport).createContext(properties);
        verify(buildStrategy).apply(defaults);
        verify(blueprintInspector).extractDefaults();
        verify(blueprintInspector).extractAfterHooks();
        verify(afterHook).accept(NEWLY_BUILT, context);
    }

    @Test
    public void shouldAcceptTraitsAndApplyBeforeConfiguration()
    {
        Consumer<ModelConfiguration> trait = mock(Consumer.class);
        Consumer<ModelConfiguration> anotherTrait = mock(Consumer.class);
        DefaultFactory<Object, ModelConfiguration> factory = new DefaultFactory<>(
            factorySupport,
            ModelBlueprint.class,
            ModelConfiguration.class
        );

        assertThat(factory.with(trait, anotherTrait).create(), is(NEWLY_BUILT));

        verify(factorySupport, times(2)).buildStrategyFor(ModelBlueprint.class);
        verify(factorySupport).dslFor(ModelConfiguration.class, defaults, afterHooks);
        verify(trait).accept(isA(ModelConfiguration.class));
        verify(anotherTrait).accept(isA(ModelConfiguration.class));
        verify(blueprintInspector).extractDefaults();
        verify(blueprintInspector).extractAfterHooks();
        verify(buildStrategy).apply(defaults);
    }

    @Test(expected = InvalidBlueprintDefinitionException.class)
    public void shouldRejectBlueprintsWithoutPublicDefaultConstructors()
    {
        new DefaultFactory<>(factorySupport, PrivateConstructorModelBlueprint.class, ModelConfiguration.class);
    }

    @Test(expected = InvalidBlueprintDefinitionException.class)
    public void shouldRejectBlueprintsWithoutTheBlueprintAnnotation()
    {
        new DefaultFactory<>(factorySupport, NoAnnotationModelBlueprint.class, ModelConfiguration.class);
    }

    @Blueprint(Object.class)
    static class ModelBlueprint
    {
    }

    interface ModelConfiguration
    extends ConfigurationDSL<Object>
    {
    }

    @Blueprint(Object.class)
    static class PrivateConstructorModelBlueprint
    {
        private PrivateConstructorModelBlueprint() {}
    }

    static class NoAnnotationModelBlueprint
    {
    }
}
