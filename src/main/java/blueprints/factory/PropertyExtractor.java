package blueprints.factory;

import blueprints.PropertyDefault;
import blueprints.UnsafeOperation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

class PropertyExtractor
{
    public Map<String, Object> extractDefaults(Class<?> blueprint)
    {
        HashMap<String, Object> defaults = new HashMap<>();
        Object blueprintInstance = UnsafeOperation.heedlessly(blueprint::newInstance);

        Arrays.stream(blueprint.getFields()).forEach(field -> {
            UnsafeOperation.heedlessly(() -> {
                if (!field.isAnnotationPresent(PropertyDefault.class)) {
                    return null;
                }

                Object value = field.get(blueprintInstance);
                if (value instanceof Supplier) {
                    value = ((Supplier) value).get();
                }

                return defaults.put(field.getName(), value);
            });
        });

        return defaults;
    }
}
