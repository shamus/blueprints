package blueprints;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SequenceTest
{
    private Sequence sequence;

    @Before
    public void setUp()
    {
        sequence = new Sequence();
    }

    @Test
    public void shouldIncrementTheValue()
    {
        assertThat(sequence.next(), is(1));
        assertThat(sequence.next(), is(2));
    }
}
