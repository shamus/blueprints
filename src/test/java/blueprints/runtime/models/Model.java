package blueprints.runtime.models;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Model
{
    String name;
    Integer age;
}
