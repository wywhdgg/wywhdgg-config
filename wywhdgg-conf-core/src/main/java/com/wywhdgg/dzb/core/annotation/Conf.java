package com.wywhdgg.dzb.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conf {
    /**
     * conf key
     *
     * @return
     */
    String value();

    /**
     * conf default value
     *
     * @return
     */
    String defaultValue() default "";

    /**
     *  whether you need a callback refresh, when the value changes.
     *
     * @return
     */
    boolean callback() default true;
}
