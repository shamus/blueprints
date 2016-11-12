package blueprints.factory;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ReflectiveProxyConfigurationDSLTest
{
    private Map<String, Object> properties;
    private Configuration configuration;

    @Before
    public void setUp()
    {
        properties = new HashMap<>();
        configuration = ReflectiveProxyConfigurationDSL.proxying(Configuration.class, properties);
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

    private interface Configuration
    {
        void name(String value);
        void invalid();
        void ignoredArguments(String value, String ignored);
    }
}
