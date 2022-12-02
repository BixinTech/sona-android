package cn.bixin.sona.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import cn.bixin.sona.annotation.BindSona;
import cn.bixin.sona.annotation.Constants;

@AutoService(Processor.class)
public class SonaAnnotationProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elementUtils = processingEnvironment.getElementUtils();
        messager.printMessage(Diagnostic.Kind.WARNING, "sona processor init");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            messager.printMessage(Diagnostic.Kind.WARNING, "sona processor process");
            createComponent(roundEnvironment);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(BindSona.class.getCanonicalName());
        return types;
    }

    private void createComponent(RoundEnvironment roundEnv) throws IOException {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(BindSona.class);
        for (Element element : set) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                TypeMirror typeMirror = typeElement.getSuperclass();
                if (typeMirror instanceof DeclaredType) {
                    Element parentElement = ((DeclaredType) typeMirror).asElement();
                    Component child = new Component(elementUtils, (TypeElement) element);
                    Component parent = new Component(elementUtils, (TypeElement) parentElement);
                    createComponentFactory(child, parent);
                }
            }
        }
    }

    private void createComponentFactory(Component child, Component parent) throws IOException {
        messager.printMessage(Diagnostic.Kind.WARNING, child.packageName + "=>" + child.className);
        messager.printMessage(Diagnostic.Kind.WARNING, parent.packageName + "=>" + parent.className);

        ClassName childClassName = ClassName.get(child.packageName, child.className);
        ClassName parentClassName = ClassName.get(parent.packageName, parent.className);

        MethodSpec getMethod = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return new $T()", childClassName)
                .returns(parentClassName)
                .build();

        ArrayList<MethodSpec> methods = new ArrayList<>();
        methods.add(getMethod);

        TypeSpec moduleFactory = TypeSpec.classBuilder(Constants.NAME + parent.className)
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methods)
                .build();

        JavaFile javaFile = JavaFile.builder(Constants.PACKAGE, moduleFactory)
                .build();
        javaFile.writeTo(filer);
    }
}
