package blueprints.factory;

import blueprints.AfterBuild;
import blueprints.PropertyDefault;
import blueprints.Sequence;
import blueprints.SequencedSupplier;
import blueprints.UnsafeOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

class BlueprintInspector
{
    private final Class<?> blueprint;
    private final Sequence sequence;
    private final Object blueprintInstance;

    public BlueprintInspector(Class<?> blueprint, Sequence sequence)
    {
        this.blueprint = blueprint;
        this.sequence = sequence;

        blueprintInstance = UnsafeOperation.heedlessly(blueprint::newInstance);
    }

    public List<BiConsumer> extractAfterHooks()
    {
        List<BiConsumer> afterHooks = new ArrayList<>();
        Arrays.stream(blueprint.getFields()).forEach(field -> {
            UnsafeOperation.heedlessly(() -> {
                if (!field.isAnnotationPresent(AfterBuild.class)) {
                    return null;
                }

                Object value = field.get(blueprintInstance);
                if (!BiConsumer.class.isAssignableFrom(value.getClass())) {
                    return null;
                }

                return afterHooks.add((BiConsumer) value);
            });
        });

        return afterHooks;
    }

    public Map<String, Object> extractDefaults()
    {
        Map<String, Object> defaults = new HashMap<>();
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
