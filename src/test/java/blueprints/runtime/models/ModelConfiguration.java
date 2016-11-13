package blueprints.runtime.models;

import blueprints.ConfigurationDSL;
import blueprints.DerivedFrom;

@DerivedFrom(ModelBlueprint.class)
public interface ModelConfiguration
    extends ConfigurationDSL<Model>
{
    void name(String value);
    void age(Integer value);
}
