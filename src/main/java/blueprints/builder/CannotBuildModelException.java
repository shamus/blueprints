package blueprints.builder;

public class CannotBuildModelException
    extends RuntimeException
{
    CannotBuildModelException(Exception e)
    {
        super(e);
    }
}
