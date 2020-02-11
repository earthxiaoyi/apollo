package cn.com.apollo.common.spi;

import java.lang.annotation.*;

/**
 * @author jiaming
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    String value() default "";

}
