package blueprints.processor;

import blueprints.AfterBuild;
import blueprints.Blueprint;
import blueprints.Context;
import blueprints.FactoryTest;
import blueprints.PropertyDefault;
import blueprints.SequencedSupplier;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static java.lang.String.format;

public class ConfigurationClassTest
{
    @Test
    public void should()
    {

    }

    @Builder
    @Value
    public static class Model
    {
        String name;
        Integer age;
        String email;

        @NonFinal
        boolean someLogicPerformed;

        public void someLogic()
        {
            someLogicPerformed = true;
        }
    }

    @Blueprint(FactoryTest.Model.class)
    public static class ModelBlueprint
    {
        @PropertyDefault
        public String name = "Jeremy";

        @PropertyDefault
        public Supplier<Integer> age = () -> 40;

        @PropertyDefault
        public SequencedSupplier<String> email = (i) -> format("model+%d@example.com", i);

        @AfterBuild
        public BiConsumer<FactoryTest.Model, Context> after = (model, context) -> {
            model.someLogic();
        };
    }
}
