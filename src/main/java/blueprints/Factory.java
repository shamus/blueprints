package blueprints;

import java.util.function.Consumer;

public interface Factory<T, D extends ConfigurationDSL<T>>
{
    T create();
    T create(Consumer<D> configuration);
}
