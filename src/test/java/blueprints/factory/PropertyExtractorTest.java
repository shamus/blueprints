package blueprints.factory;

import blueprints.Sequence;
import blueprints.PropertyDefault;
import blueprints.SequencedSupplier;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PropertyExtractorTest
{
    private PropertyExtractor propertyExtractor;
    private Sequence sequence;

    @Before
    public void setUp()
    {
        sequence = mock(Sequence.class);
        propertyExtractor = new PropertyExtractor();
    }

    @Test
    public void shouldExtractPublicFieldsAnnotatedWithPropertyDefault()
    {
        Map<String, Object> defaults = propertyExtractor.extractDefaults(ModelBlueprint.class, sequence);
        assertThat(defaults.size(), is(2));
        assertThat(defaults.containsKey("ignoredProperty"), is(false));
        assertThat(defaults.containsKey("privateProperty"), is(false));
        assertThat(defaults.get("name"), is("Jeremy"));
        assertThat(defaults.get("age"), is(40));
    }

    @Test
    public void shouldHandlePublicFieldsWhichAreSuppliersAndAnnotatedWithPropertyDefaultDifferently()
    {
        when(sequence.next()).thenReturn(36);

        Map<String, Object> defaults = propertyExtractor.extractDefaults(SupplierBasedModelBlueprint.class, sequence);
        assertThat(defaults.size(), is(2));
        assertThat(defaults.get("name"), is("Supplied"));
        assertThat(defaults.get("age"), is(36));
        verify(sequence).next();
    }

    @Test(expected = RuntimeException.class)
    public void shouldBlowUpIfNoPublicConstructorIsPresent()
    {
        propertyExtractor.extractDefaults(PrivateConstructorModelBlueprint.class, sequence);
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
        public Supplier<String> name = () -> "Supplied";

        @PropertyDefault
        public SequencedSupplier<Integer> age = (i) -> i;
    }

    static class PrivateConstructorModelBlueprint
    {
        private PrivateConstructorModelBlueprint()
        {
        }
    }
}
