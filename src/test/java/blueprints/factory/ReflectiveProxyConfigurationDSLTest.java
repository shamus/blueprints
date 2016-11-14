package blueprints.factory;

import blueprints.ConfigurationDSL;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ReflectiveProxyConfigurationDSLTest
{
    private Map<String, Object> properties;
    private Configuration configuration;
    private List<BiConsumer> afterHooks;

    @Before
    public void setUp()
    {
        afterHooks = new ArrayList<>();
        properties = new HashMap<>();
        configuration = ReflectiveProxyConfigurationDSL.proxying(Configuration.class, properties, afterHooks);
    }

    @Test
    public void shouldStoreTheValueInTheSuppliedMap()
    {
        configuration.name("Jeremy");
        assertThat(properties.get("name"), is("Jeremy"));
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowIfNoMethodAcceptsNoArguments()
    {
        configuration.invalid();
    }

    @Test
    public void shouldIgnoreMutlipleArguments()
    {
        configuration.ignoredArguments("foo", "bar");
        assertThat(properties.get("ignoredArguments"), is("foo"));
    }

    @Test
    public void shouldStoreSuppliedAfterHooks()
    {
        BiConsumer hook = (instance, context) -> {};
        configuration.after(hook);
        assertThat(afterHooks.size(), is(1));
        assertThat(afterHooks, hasItem(hook));
    }

    private interface Configuration
        extends ConfigurationDSL
    {
        void name(String value);
        void invalid();
        void ignoredArguments(String value, String ignored);
    }
}
