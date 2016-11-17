package blueprints;

import blueprints.factory.DefaultFactory;
import blueprints.factory.FactorySupport;
import lombok.Builder;
import lombok.Value;
import org.junit.Before;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FactoryTest
{
    private Factory<Model, ModelConfiguration> factory;

    @Before
    public void setUp()
    {
        factory = new DefaultFactory<>(
            FactorySupport.defaultSupport(new Factories()
            {
                @Override
                public <T, D extends ConfigurationDSL<T>> Factory<T, D> get(Class<T> modelClass)
                {
                    if (modelClass.equals(Model.class)) {
                        return (Factory<T, D>) factory;
                    }

                    return null;
                }
            }),
            ModelBlueprint.class,
            ModelConfiguration.class
        );
    }

    @Test
    public void shouldCreateAModelFromDefaults()
    {
        Model model = factory.create();
        assertThat(model.getName(), is("Jeremy"));
        assertThat(model.getAge(), is(40));
        assertThat(model.getEmail(), is("model+1@example.com"));
    }

    @Test
    public void shouldCreateAModelFromConfiguration()
    {
        Model model = factory.create((configuration) -> {
            configuration.name("Ada");
            configuration.age(36);
        });
        assertThat(model.getName(), is("Ada"));
        assertThat(model.getAge(), is(36));
        assertThat(model.getEmail(), is("model+1@example.com"));
    }

    @Test
    public void shouldCreateAModelWithAnAfterHook()
    {
        BiConsumer<Model, Context> afterHook = mock(BiConsumer.class);

        Model model = factory.create(configuration -> {
            configuration.after(afterHook);
        });

        assertThat(model.getName(), is("Jeremy"));
        assertThat(model.getAge(), is(40));
        assertThat(model.getEmail(), is("model+1@example.com"));
        verify(afterHook).accept(eq(model), isA(Context.class));
    }

    @Test
    public void shouldCreateAModelWithATrait()
    {
        BiConsumer<Model, Context> afterHook = mock(BiConsumer.class);
        Consumer<ModelConfiguration> trait = configuration -> {
            configuration.name("Ada");
            configuration.age(36);

            configuration.after(afterHook);
        };

        Model model = factory.with(trait).create();

        assertThat(model.getName(), is("Ada"));
        assertThat(model.getAge(), is(36));
        assertThat(model.getEmail(), is("model+1@example.com"));
        verify(afterHook).accept(eq(model), isA(Context.class));
    }

    @Test
    public void shouldCreateAModelWithATraitAndConfiguration()
    {
        BiConsumer<Model, Context> afterHook = mock(BiConsumer.class);
        Consumer<ModelConfiguration> trait = configuration -> {
            configuration.name("Ada");
            configuration.age(36);
        };

        Model model = factory.with(trait).create(configuration -> {
            configuration.age(30);
            configuration.after(afterHook);
        });

        assertThat(model.getName(), is("Ada"));
        assertThat(model.getAge(), is(30));
        assertThat(model.getEmail(), is("model+1@example.com"));
        verify(afterHook).accept(eq(model), isA(Context.class));
    }

    @Builder
    @Value
    public static class Model
    {
        String name;
        Integer age;
        String email;
    }

    @Blueprint(Model.class)
    public static class ModelBlueprint
    {
        @PropertyDefault
        public String name = "Jeremy";

        @PropertyDefault
        public Supplier<Integer> age = () -> 40;

        @PropertyDefault
        public SequencedSupplier<String> email = (i) -> format("model+%d@example.com", i);
    }

    interface ModelConfiguration
        extends ConfigurationDSL<Model>
    {
        void name(String value);
        void age(Integer value);
        void email(String value);
    }
}
