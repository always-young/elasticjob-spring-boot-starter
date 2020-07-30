package com.kevin.elasticjobboot;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.CollectionUtils;

/**
 * @author Kevin Liu
 * @date 2020/7/30 上午10:00
 */
public class ElasticJobBeanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        final String[] jobScanPackage = getJobScanPackage(importingClassMetadata);
        if (null == jobScanPackage || jobScanPackage.length == 0) {
            return;
        }
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(JobBeanDefinitionPostProcessor.class);
        beanDefinitionBuilder.addConstructorArgValue(jobScanPackage);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinitionBuilder.getBeanDefinition(),JobBeanDefinitionPostProcessor.class.getSimpleName());
        BeanDefinitionReaderUtils.registerBeanDefinition(holder,registry);
    }

    private String[] getJobScanPackage(AnnotationMetadata importingClassMetadata) {
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableElasticJob.class.getName()));
        if (null == annoAttrs) {
            return null;
        }
        return annoAttrs.getStringArray("value");
    }
}
