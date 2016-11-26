package blueprints.processor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Map;

class Utils
{
    private final Types typeUtils;
    private final Elements elementUtils;
    private final Filer filer;
    private final Messager messager;

    public Utils(ProcessingEnvironment env)
    {
        typeUtils = env.getTypeUtils();
        elementUtils = env.getElementUtils();
        filer = env.getFiler();
        messager = env.getMessager();
    }

    public Element getElement(TypeMirror mirror)
    {
        return typeUtils.asElement(mirror);
    }

    public String packageNameFor(Element classElement)
    {
        PackageElement pkg = elementUtils.getPackageOf(classElement);
        if (pkg.isUnnamed()) {
            return "";
        }

        return pkg.getQualifiedName().toString();
    }

    public AnnotationMirror getAnnotationMirror(Element typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for(AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if(m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }

        return null;
    }

    public AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet() ) {
            if(entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public boolean typeErasureMatchesClass(TypeMirror t, Class c)
    {
        return typeUtils.erasure(t).toString().equals(c.getName());
    }

    public JavaFileObject createSourceFile(String packageName, String className)
        throws IOException
    {
        String qualifiedClassName = className;
        if (!packageName.isEmpty()) {
            qualifiedClassName = packageName + "." + className;
        }

        return filer.createSourceFile(qualifiedClassName);
    }

    public void error(Element e, String msg, Object... args) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            String.format(msg, args),
            e);
    }
}
