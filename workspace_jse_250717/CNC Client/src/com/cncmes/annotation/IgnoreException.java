package com.cncmes.annotation;

public @interface IgnoreException {
    Class<? extends Throwable>[] value() default {};

}
