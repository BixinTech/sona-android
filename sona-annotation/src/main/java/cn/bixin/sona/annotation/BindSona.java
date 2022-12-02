package cn.bixin.sona.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author luokun
 * @Date 2020/3/25
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface BindSona {}
