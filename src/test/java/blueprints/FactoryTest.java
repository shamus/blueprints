package blueprints;

import blueprints.factory.DefaultFactory;
import blueprints.factory.FactorySupport;
import lombok.Builder;
import lombok.Value;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FactoryTest
{
    private Factory<Model, ModelConfiguration> factory;

    @Before
    public void setUp()
    {
        factory = new DefaultFactory<>(FactorySupport.defaultSupport, ModelBlueprint.class, ModelConfiguration.class);
    }

    @Test
    public void shouldCreateAModelFromDefaults()
    {
        Model model = factory.create();
        assertThat(model.getName(), is("Jeremy"));
        assertThat(model.getAge(), is(40));
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
    }

    @Builder
    @Value
    public static class Model
    {
        String name;
        Integer age;
    }

    @Blueprint(Model.class)
    public static class ModelBlueprint
    {
        @PropertyDefault
        public String name = "Jeremy";

        @PropertyDefault
        public Supplier<Integer> age = () -> 40;
    }

    interface ModelConfiguration
        extends ConfigurationDSL<Model>
    {
        void name(String value);
        void age(Integer value);
    }
}
