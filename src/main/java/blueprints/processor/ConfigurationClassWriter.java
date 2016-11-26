package blueprints.processor;

import com.squareup.javapoet.JavaFile;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

public class ConfigurationClassWriter
{
    private final Utils utils;

    public ConfigurationClassWriter(Utils utils)
    {
        this.utils = utils;
    }

    public void save(ConfigurationClassSpec configurationSpec)
        throws IOException
    {
        String packageName = configurationSpec.getPackageName();
        String className = configurationSpec.getClassName();
        JavaFile interfaceFile = configurationSpec.getSourceFile();

        JavaFileObject jfo = utils.createSourceFile(packageName, className);
        Writer writer = jfo.openWriter();
        interfaceFile.writeTo(writer);
        writer.close();
    }
}
