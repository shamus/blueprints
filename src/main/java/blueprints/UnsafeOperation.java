package blueprints;

import java.util.function.Function;

@FunctionalInterface
public interface UnsafeOperation<T>
{
    static <T> T heedlessly(UnsafeOperation<T> operation)
    {
        return heedlessly(RuntimeException::new, operation);
    }

    static <T, C extends Exception, E extends RuntimeException> T heedlessly(
        Function<C, E> exceptionTranslator,
        UnsafeOperation<T> operation)
    {
        try
        {
            return operation.invoke();
        }
        catch (Exception e)
        {
            throw exceptionTranslator.apply((C) e);
        }
    }

    T invoke() throws Exception;
}
