package blueprints.factory;

import blueprints.Sequence;
import blueprints.PropertyDefault;
import blueprints.SequencedSupplier;
import blueprints.UnsafeOperation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

class PropertyExtractor
{
    public Map<String, Object> extractDefaults(Class<?> blueprint, Sequence sequence)
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

                if (value instanceof SequencedSupplier) {
                    value = ((SequencedSupplier) value).get(sequence.next());
                }

                return defaults.put(field.getName(), value);
            });
        });

        return defaults;
    }
}
