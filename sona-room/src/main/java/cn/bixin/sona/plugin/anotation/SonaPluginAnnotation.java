package cn.bixin.sona.plugin.anotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.bixin.sona.plugin.entity.PluginEnum;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SonaPluginAnnotation {
    PluginEnum value();
}
