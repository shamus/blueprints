package blueprints.factory;

import blueprints.PropertyDefault;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PropertyExtractorTest
{
    private PropertyExtractor propertyExtractor;

    @Before
    public void setUp()
    {
        propertyExtractor = new PropertyExtractor();
    }

    @Test
    public void shouldExtractPublicFieldsAnnotatedWithPropertyDefault()
    {
        Map<String, Object> defaults = propertyExtractor.extractDefaults(ModelBlueprint.class);
        assertThat(defaults.size(), is(2));
        assertThat(defaults.containsKey("ignoredProperty"), is(false));
        assertThat(defaults.containsKey("privateProperty"), is(false));
        assertThat(defaults.get("name"), is("Jeremy"));
        assertThat(defaults.get("age"), is(40));
    }

    @Test
    public void shouldHandlePublicFieldsWhichAreSuppliersAndAnnotatedWithPropertyDefaultDifferently()
    {
        Map<String, Object> defaults = propertyExtractor.extractDefaults(SupplierBasedModelBlueprint.class);
        assertThat(defaults.size(), is(1));
        assertThat(defaults.get("name"), is("Jeremy"));
    }

    @Test(expected = RuntimeException.class)
    public void shouldBlowUpIfNoPublicConstructorIsPresent()
    {
        propertyExtractor.extractDefaults(PrivateConstructorModelBlueprint.class);
    }

    static class ModelBlueprint
    {
        @PropertyDefault
        public String name = "Jeremy";

        @PropertyDefault
        public Integer age = 40;

        public String ignoredProperty = "ignored";

        @PropertyDefault
        private String privateProperty = "private";
    }

    static class SupplierBasedModelBlueprint
    {
        @PropertyDefault
        public Supplier<String> name = () -> "Jeremy";
    }

    static class PrivateConstructorModelBlueprint
    {
        private PrivateConstructorModelBlueprint()
        {
        }
    }
}
