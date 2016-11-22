package blueprints.factory;

import blueprints.AfterBuild;
import blueprints.Context;
import blueprints.PropertyDefault;
import blueprints.Sequence;
import blueprints.SequencedSupplier;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BlueprintInspectorTest
{
    private Sequence sequence;

    @Before
    public void setUp()
    {
        sequence = mock(Sequence.class);
    }

    @Test
    public void shouldExtractPublicFieldsAnnotatedWithPropertyDefault()
    {
        BlueprintInspector blueprintInspector = new BlueprintInspector(ModelBlueprint.class, sequence);

        Map<String, Object> defaults = blueprintInspector.extractDefaults();
        assertThat(defaults.size(), is(2));
        assertThat(defaults.containsKey("ignoredProperty"), is(false));
        assertThat(defaults.containsKey("privateProperty"), is(false));
        assertThat(defaults.get("name"), is("Jeremy"));
        assertThat(defaults.get("age"), is(40));
    }

    @Test
    public void shouldExtractPublicFieldsAnnotatedWithAfterBuild()
    {
        BlueprintInspector blueprintInspector = new BlueprintInspector(ModelBlueprint.class, sequence);

        List<BiConsumer> afterHooks = blueprintInspector.extractAfterHooks();
        assertThat(afterHooks.size(), is(1));
        assertThat(afterHooks, hasItem(new ModelBlueprint().afterHook));
    }

    @Test
    public void shouldHandlePublicFieldsWhichAreSuppliersAndAnnotatedWithPropertyDefaultDifferently()
    {
        when(sequence.next()).thenReturn(36);
        BlueprintInspector blueprintInspector = new BlueprintInspector(SupplierBasedModelBlueprint.class, sequence);

        Map<String, Object> defaults = blueprintInspector.extractDefaults();
        assertThat(defaults.size(), is(2));
        assertThat(defaults.get("name"), is("Supplied"));
        assertThat(defaults.get("age"), is(36));
        verify(sequence).next();
    }

    @Test(expected = RuntimeException.class)
    public void shouldBlowUpIfNoPublicConstructorIsPresent()
    {
        BlueprintInspector blueprintInspector = new BlueprintInspector(PrivateConstructorModelBlueprint.class, sequence);
        blueprintInspector.extractDefaults();
    }

    static class ModelBlueprint
    {
        @PropertyDefault
        public String name = "Jeremy";

        @PropertyDefault
        public Integer age = 40;

        @AfterBuild
        public BiConsumer<Object, Context> afterHook = (a,b) -> {};

        public String ignoredProperty = "ignored";

        @PropertyDefault
        private String privateProperty = "private";

        @AfterBuild
        public Object notACallback = new Object();
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
