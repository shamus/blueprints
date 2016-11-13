package blueprints;

public class Sequence
{
    private int sequence;

    public Sequence()
    {
        sequence = 1;
    }

    public Integer next()
    {
        return sequence++;
    }
}
