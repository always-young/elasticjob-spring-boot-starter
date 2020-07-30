package com.kevin.elasticjobboot;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Kevin Liu
 * @date 2020/7/30 下午1:45
 */
@Slf4j
public class JobBeanDefinitionPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final static String SPRING_JOB_SCHEDULER = "springJobScheduler";

    private final String[] basePackages;

    public JobBeanDefinitionPostProcessor(String[] basePackages) {
        this.basePackages = basePackages;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        ClassPathBeanDefinitionScanner beanDefinitionScanner = new ClassPathBeanDefinitionScanner(beanDefinitionRegistry, false);
        beanDefinitionScanner.addIncludeFilter(new AssignableTypeFilter(ElasticJob.class));
        beanDefinitionScanner.scan(basePackages);
        if (beanDefinitionRegistry instanceof DefaultListableBeanFactory) {
            ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) beanDefinitionRegistry;
            final Map<String, ElasticJob> beansOfType = beanFactory.getBeansOfType(ElasticJob.class);
            beansOfType.forEach((k, v) -> {
                if (!v.getClass().isAnnotationPresent(Job.class)) {
                    throw new RuntimeException(k + "is not annotation  by Job");
                }
                final Job job = v.getClass().getAnnotation(Job.class);
                JobCoreConfiguration coreConfiguration = JobCoreConfiguration.newBuilder(job.jobName(), job.corn(), job.shardingItemParameters()).build();
                JobTypeConfiguration jobTypeConfiguration;
                //TODO@kevin 🚀 🚀 🚀 工厂模式
                if (v instanceof SimpleJob) {
                    jobTypeConfiguration = new SimpleJobConfiguration(coreConfiguration, v.getClass().getCanonicalName());
                } else if (v instanceof DataflowJob) {
                    jobTypeConfiguration = new DataflowJobConfiguration(coreConfiguration, v.getClass().getCanonicalName(), true);
                } else {
                    jobTypeConfiguration = new ScriptJobConfiguration(coreConfiguration, job.getCommandLine());
                }
                final LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(jobTypeConfiguration).overwrite(true).build();
                BeanDefinitionBuilder springJobScheduler = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
                springJobScheduler.addConstructorArgValue(v);
                //RuntimeBeanReference Spring Bean的预加载
                springJobScheduler.addConstructorArgValue(new RuntimeBeanReference(ZookeeperRegistryCenter.class));
                springJobScheduler.addConstructorArgValue(liteJobConfiguration);
                springJobScheduler.addConstructorArgValue(new ArrayList<>());
                springJobScheduler.setInitMethodName("init");
                BeanDefinitionHolder holder = new BeanDefinitionHolder(springJobScheduler.getBeanDefinition(), k + SPRING_JOB_SCHEDULER);
                BeanDefinitionReaderUtils.registerBeanDefinition(holder, beanDefinitionRegistry);
                log.info("{} register to spring success", k);
            });
        } else {
            throw new RuntimeException("please user DefaultListableBeanFactory to start the spring container");
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        //final ZookeeperRegistryCenter zookeeperRegistryCenter = configurableListableBeanFactory.getBean(ZookeeperRegistryCenter.class);

    }
}
