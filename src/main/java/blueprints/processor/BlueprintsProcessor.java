package blueprints.processor;

import blueprints.Blueprint;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

public class BlueprintsProcessor
    extends AbstractProcessor
{
    @Override
    public synchronized void init(ProcessingEnvironment env)
    {
        super.init(env);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env)
    {
        Utils utils = new Utils(processingEnv);
        ConfigurationClassWriter configurationWriter = new ConfigurationClassWriter(utils);

        for (Element element : env.getElementsAnnotatedWith(Blueprint.class)) {
            try {
                ConfigurationClassSpec configurationClassSpec = new ConfigurationClassSpec(utils, element);
                configurationWriter.save(configurationClassSpec);
            } catch (ProcessingException e) {
                return true;
            }
            catch (Exception e) {
                utils.error(element, e.getMessage());
                return true;
            }
        }

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        Set<String> annotations = new HashSet<>();
        annotations.add(Blueprint.class.getCanonicalName());

        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latestSupported();
    }
}
