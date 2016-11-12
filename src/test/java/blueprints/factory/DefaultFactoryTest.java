package blueprints.factory;

import blueprints.Blueprint;
import blueprints.ConfigurationDSL;
import blueprints.factory.builder.BuildStrategy;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultFactoryTest
{
    private static final Object NEWLY_BUILT = new Object();
    private BuildStrategy buildStrategy;
    private FactorySupport factorySupport;
    private HashMap<String, Object> defaults;

    @Before
    public void setUp()
    {
        factorySupport = mock(FactorySupport.class);
        buildStrategy = mock(BuildStrategy.class);

        defaults = new HashMap<>();
        when(factorySupport.buildStrategyFor(any())).thenReturn(buildStrategy);
        when(factorySupport.extractDefaultsFrom(any())).thenReturn(defaults);
        when(factorySupport.dslFor(any(), any())).thenAnswer(invocation ->
            ReflectiveProxyConfigurationDSL.proxying(invocation.getArgument(0), invocation.getArgument(1))
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
        verify(factorySupport).extractDefaultsFrom(ModelBlueprint.class);
        verify(factorySupport).dslFor(ModelConfiguration.class, defaults);
        verify(buildStrategy).apply(defaults);
    }

    @Test
    public void shouldCreateWithConfiguration()
    {
        Consumer configuration = mock(Consumer.class);
        DefaultFactory<Object, ModelConfiguration> factory = new DefaultFactory<>(
            factorySupport,
            ModelBlueprint.class,
            ModelConfiguration.class
        );

        assertThat(factory.create(configuration), is(NEWLY_BUILT));

        verify(factorySupport).buildStrategyFor(ModelBlueprint.class);
        verify(factorySupport).extractDefaultsFrom(ModelBlueprint.class);
        verify(factorySupport).dslFor(ModelConfiguration.class, defaults);
        verify(buildStrategy).apply(defaults);
        verify(configuration).accept(isA(ModelConfiguration.class));
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
