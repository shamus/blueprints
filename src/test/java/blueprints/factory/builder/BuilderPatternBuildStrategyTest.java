package blueprints.factory.builder;

import lombok.Builder;
import lombok.Value;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BuilderPatternBuildStrategyTest
{
    private BuilderPatternBuildStrategy<Model> buildStrategy;
    private Map<String, Object> properties;

    @Before
    public void setUp()
    {
        buildStrategy = new BuilderPatternBuildStrategy<>(Model.class);
        properties = new HashMap<>();
    }

    @Test
    public void shouldPopulateModelWithValues()
    {
        properties.put("name", "Jeremy");
        properties.put("age", 40);

        Model model = buildStrategy.apply(properties);
        assertThat(model.getName(), is("Jeremy"));
        assertThat(model.getAge(), is(40));
    }

    @Test(expected=CannotBuildModelException.class)
    public void shouldThrowIfPropertyIsOfWrongType()
    {
        properties.put("name", 40);
        buildStrategy.apply(properties);
    }

    @Test(expected=CannotBuildModelException.class)
    public void shouldThrowIfPropertyIsUnknown()
    {
        properties.put("unknown", 40);
        buildStrategy.apply(properties);
    }

    @Test(expected=CannotBuildModelException.class)
    public void shouldThrowIfObjectDoesNotSupportBuilder()
    {
        new BuilderPatternBuildStrategy<>(Object.class).apply(properties);
    }

    @Value
    @Builder
    private static class Model
    {
        String name;
        Integer age;
    }
}
