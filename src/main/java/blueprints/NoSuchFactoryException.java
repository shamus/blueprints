package blueprints;

public class NoSuchFactoryException
    extends RuntimeException
{
    public NoSuchFactoryException(Class modelClass)
    {
        super(String.format("Could not find a factory for %s", modelClass.getCanonicalName()));
    }
}
