package blueprints.runtime.models;

import blueprints.Blueprint;

import java.util.function.Supplier;

@Blueprint(Model.class)
public class ModelBlueprint
{
    public String name = "Jeremy";
    public Supplier<Integer> age = () -> 40;
}
