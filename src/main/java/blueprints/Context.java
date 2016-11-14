package blueprints;

public interface Context
{
    Object get(String name);

    <T, D extends ConfigurationDSL<T>> Factory<T, D> getFactory(Class<T> modelClass);
}
