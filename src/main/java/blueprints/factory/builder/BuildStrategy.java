package blueprints.factory.builder;

import java.util.Map;

@FunctionalInterface
public interface BuildStrategy<R>
{
    R apply(Map<String, Object> properties);
}
