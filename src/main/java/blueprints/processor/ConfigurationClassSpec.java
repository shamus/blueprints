package blueprints.processor;

import blueprints.Blueprint;
import blueprints.ConfigurationDSL;
import blueprints.DerivedFrom;
import blueprints.PropertyDefault;
import blueprints.SequencedSupplier;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

public class ConfigurationClassSpec
{
    private static final String SUFFIX = "Configuration";

    private final Utils utils;
    private final Element blueprint;
    private final Element model;

    private final String packageName;
    private final String className;

    public ConfigurationClassSpec(Utils utils, Element blueprint)
    {
        this.utils = utils;
        this.blueprint = blueprint;
        this.model = getModelClass(blueprint);

        this.packageName = utils.packageNameFor(blueprint);
        this.className = model.getSimpleName() + SUFFIX;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public String getClassName()
    {
        return className;
    }

    public JavaFile getSourceFile()
    {
        return JavaFile.builder(packageName, getInterfaceSpec()).build();
    }

    TypeSpec getInterfaceSpec()
    {
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(getClassName())
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(getDerivedFromAnnotation())
            .addSuperinterface(getSuperInterface());

        getMethodSpecs().forEach(builder::addMethod);

        return builder.build();
    }

    ParameterizedTypeName getSuperInterface()
    {
        ClassName parameterizedType = ClassName.get(packageName, model.toString());
        return ParameterizedTypeName.get(ClassName.get(ConfigurationDSL.class), parameterizedType);
    }

    AnnotationSpec getDerivedFromAnnotation()
    {
        return AnnotationSpec.builder(DerivedFrom.class)
            .addMember("value", "$T.class", ClassName.get(blueprint.asType()))
            .build();
    }

    List<MethodSpec> getMethodSpecs()
    {
        return blueprint.getEnclosedElements().stream()
            .filter(element -> element.getKind() == ElementKind.FIELD)
            .filter(field -> field.getAnnotation(PropertyDefault.class) != null)
            .map(this::overrideMethodSpec)
            .collect(toList());

    }

    private Element getModelClass(Element blueprint)
    {
        AnnotationMirror annotation = utils.getAnnotationMirror(blueprint, Blueprint.class);
        if (annotation == null) {
            String msg = String.format("Blueprint class %s is not annotated with @Blueprint", blueprint);
            throw new IllegalStateException(msg);
        }

        AnnotationValue value = utils.getAnnotationValue(annotation, "value");
        if (value == null) {
            String msg = String.format("Blueprint class %s is annotated with @Blueprint, but missing value", blueprint);
            throw new IllegalStateException(msg);
        }

        return utils.getElement((TypeMirror) value.getValue());
    }

    private MethodSpec overrideMethodSpec(Element defaultValueProvider)
    {
        TypeMirror parameterType = parameterTypeOf(defaultValueProvider);

        return MethodSpec.methodBuilder(defaultValueProvider.toString())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(TypeName.get(parameterType), "value")
            .build();
    }

    private TypeMirror parameterTypeOf(Element defaultValueProvider)
    {
        TypeMirror defaultValueType = defaultValueProvider.asType();
        boolean isSupplier = utils.typeErasureMatchesClass(defaultValueType, Supplier.class);
        boolean isSequencedSupplier = utils.typeErasureMatchesClass(defaultValueType, SequencedSupplier.class);
        if (!isSupplier && !isSequencedSupplier) {
            return defaultValueType;
        }

        List<? extends TypeMirror> typeArguments = ((DeclaredType) defaultValueType).getTypeArguments();
        if (typeArguments.size() != 1) {
            utils.error(defaultValueProvider, "%s must specify type of provided value", defaultValueProvider);
            throw new ProcessingException();
        }
        return typeArguments.get(0);
    }
}
