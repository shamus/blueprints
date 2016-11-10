package blueprints;

import org.junit.Test;

public class UnsafeOperationTest
{
    @Test(expected = RuntimeException.class)
    public void shouldTranslateExceptionsToRuntimeExceptionByDefault()
    {
        UnsafeOperation.heedlessly(() -> { throw new Exception(); });
    }

    @Test(expected = CustomRuntimeException.class)
    public void heedlessly1() throws Exception
    {
        UnsafeOperation.heedlessly(CustomRuntimeException::new, () -> { throw new Exception(); });
    }

    private static class CustomRuntimeException
    extends RuntimeException
    {
        CustomRuntimeException(Exception e)
        {
            super(e);
        }
    }
}
