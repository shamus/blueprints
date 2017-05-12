package blueprints.factory.builder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static blueprints.UnsafeOperation.heedlessly;

public class BuilderPatternBuildStrategy<R>
    implements BuildStrategy<R>
{
    private static final String BUILDER = "builder";
    private static final String BUILD = "build";

    private Class<R> modelClass;

    public BuilderPatternBuildStrategy(Class<R> modelClass)
    {
        this.modelClass = modelClass;
    }

    @Override
    public R apply(Map<String, Object> properties)
    {
        Object builder = getBuilder();
        properties.keySet().forEach((property) -> setValue(builder, property, properties.get(property)));

        return build(builder);
    }

    private Object getBuilder()
    {
        return heedlessly(CannotBuildModelException::new, () ->
            modelClass.getMethod(BUILDER).invoke(modelClass, (Object[]) null)
        );
    }

    private R build(Object builder)
    {
        return heedlessly(CannotBuildModelException::new, () ->
            (R) builder.getClass().getMethod(BUILD).invoke(builder, (Object[]) null)
        );
    }

    private void setValue(Object builder, String methodName, Object value)
    {
        heedlessly(
            CannotBuildModelException::new,
            () -> getMethod(builder, methodName, value).invoke(builder, value)
        );
    }

    private Method getMethod(Object builder, String methodName, Object value)
    throws NoSuchFieldException
    {
        Class<?> builderClass = builder.getClass();
        try {
            Class[] types = new Class[]{modelClass.getDeclaredField(methodName).getType()};
            return builderClass.getMethod(methodName, types);
        }
        catch (NoSuchMethodException e) {
            return Arrays.stream(builderClass.getMethods())
                .filter(method -> method.getName().equals(methodName))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> {
                    Parameter[] parameters = method.getParameters();
                    return Objects.nonNull(value) && Arrays.stream(parameters).allMatch(param ->
                        param.getType().isAssignableFrom(value.getClass())
                    );
                })
                .findFirst()
                .get();
        }
    }
}
