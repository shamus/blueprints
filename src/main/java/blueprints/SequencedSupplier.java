package blueprints;

@FunctionalInterface
public interface SequencedSupplier<T>
{
    T get(Integer i);
}
