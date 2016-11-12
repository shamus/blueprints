package blueprints.factory;

public class InvalidBlueprintDefinitionException
extends RuntimeException
{
    public InvalidBlueprintDefinitionException(String message)
    {
        super(message);
    }
}
