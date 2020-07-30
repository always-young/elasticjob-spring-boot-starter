package com.kevin.elasticjobboot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Kevin Liu
 * @date 2020/7/30 下午2:10
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Job {

    String jobName() default "ElasticJob";

    String corn() default "*/5 * * * * ?";

    int shardingItemParameters() default 1;

    String commandLine() default "java -version";
}
