package blueprints.runtime;

import blueprints.ConfigurationDSL;
import blueprints.Factory;
import blueprints.NoSuchFactoryException;
import blueprints.runtime.models.Model;
import blueprints.runtime.models.ModelConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class FactoryScannerTest
{

    private FactoryScanner factories;

    @Before
    public void setUp()
    {
        factories = new FactoryScanner("blueprints.runtime.models");
    }

    @Test
    public void shouldFindFactoriesInTheSpecifiedPackages()
    {
        Factory<Model, ModelConfiguration> factory = factories.get(Model.class);
        assertThat(factory, is(notNullValue()));
    }

    @Test(expected = NoSuchFactoryException.class)
    public void shouldReturnNullForUnknownFactories()
    {
        Factory<Object, ConfigurationDSL<Object>> factory = factories.get(Object.class);
    }
}
