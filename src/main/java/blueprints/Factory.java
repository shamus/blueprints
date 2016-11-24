package blueprints;

import java.util.Map;
import java.util.function.Consumer;

public interface Factory<T, D extends ConfigurationDSL<T>>
{
    Factory<T, D> with(Consumer<D>... trait);

    T create();
    T create(Map<String, Object> createProperties);

    T create(Consumer<D> configuration);
    T create(Map<String, Object> createProperties, Consumer<D> configuration);
}
