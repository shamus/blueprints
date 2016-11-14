package blueprints;

import java.util.function.BiConsumer;

public interface ConfigurationDSL<T>
{
    void after(BiConsumer<T, Context> hook);
}
