package blueprints;

public interface Factories
{
    <T, D extends ConfigurationDSL<T>> Factory<T, D> get(Class<T> modelClass);
}
