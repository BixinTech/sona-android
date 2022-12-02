package cn.bixin.sona.compiler;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * @Author luokun
 * @Date 2020/3/26
 */
public class Component {
    public String packageName;
    public String className;

    public Component(Elements elementUtils, TypeElement typeElement) {
        packageName = getPackageName(elementUtils, typeElement);
        className = getClassName(typeElement, packageName);
    }

    public String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen)
                .replace('.', '$');
    }

    public String getPackageName(Elements elementUtils, TypeElement classElement) {
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        return packageElement.getQualifiedName().toString();
    }

    public String getFullClassName() {
        return packageName + "." + className;
    }

    public String getClassName(){
        return className;
    }
}
