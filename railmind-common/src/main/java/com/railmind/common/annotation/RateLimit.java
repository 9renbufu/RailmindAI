package com.railmind.common.annotation;

import java.lang.annotation.*;

/**
 * 限流注解，标注在Controller方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key前缀
     */
    String key() default "";

    /**
     * 时间窗口内最大请求数
     */
    int limit() default 100;

    /**
     * 时间窗口(秒)
     */
    int window() default 60;

    /**
     * 限流维度: GLOBAL-全局, USER-用户级
     */
    Dimension dimension() default Dimension.USER;

    enum Dimension {
        GLOBAL, USER
    }
}
