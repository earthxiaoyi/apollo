package cn.com.apollo.common.spi;

import java.lang.annotation.*;

/**
 * @author jiaming
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Active {

    String[] group() default "";

    String value() default "";

    int order() default 0;

}
