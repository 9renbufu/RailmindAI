package com.railmind.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 幂等注解，防止重复提交
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等key的SpEL表达式，如 "#request.orderNo"
     */
    String key();

    /**
     * 过期时间
     */
    long timeout() default 5;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 提示信息
     */
    String message() default "请勿重复提交";
}
