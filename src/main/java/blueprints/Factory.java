package blueprints;

import java.util.function.Consumer;

public interface Factory<T, D extends ConfigurationDSL<T>>
{
    Factory<T, D> with(Consumer<D>... trait);

    T create();
    T create(Consumer<D> configuration);
}
