package blueprints.factory.builder;

import java.util.Map;

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
        heedlessly(CannotBuildModelException::new, () -> {
            Class[] types = new Class[]{modelClass.getDeclaredField(methodName).getType()};
            return builder.getClass().getMethod(methodName, types).invoke(builder, value);
        });
    }
}
