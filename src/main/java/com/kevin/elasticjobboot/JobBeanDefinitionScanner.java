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
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

/**
 * @author Kevin Liu
 * @date 2020/7/30 下午8:10
 */
@Slf4j
public class JobBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    private final static String SPRING_JOB_SCHEDULER = "springJobScheduler";

    public JobBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        super(registry,false);
    }

    protected boolean contains(String[] interfaceNames,String name){
        for (String interfaceName : interfaceNames) {
            if(Objects.equals(interfaceName,name)){
                return true;
            }
        }
        return false;
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        final Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        beanDefinitionHolders.forEach(item->{
            ScannedGenericBeanDefinition beanDefinition = (ScannedGenericBeanDefinition) item.getBeanDefinition();
            final AnnotationAttributes job = AnnotationAttributes.fromMap(beanDefinition.getMetadata().getAnnotationAttributes(Job.class.getName()));
            if(job == null) {
                throw new RuntimeException(item.getBeanName() + "is not annotation  by Job");
            }
            JobCoreConfiguration coreConfiguration = JobCoreConfiguration.newBuilder(job.getString("jobName"), job.getString("corn"), job.getNumber("shardingItemParameters")).build();
            JobTypeConfiguration jobTypeConfiguration;
            final String[] interfaceNames = beanDefinition.getMetadata().getInterfaceNames();
            //TODO@kevin 🚀 🚀 🚀 工厂模式
            if (contains(interfaceNames,SimpleJob.class.getCanonicalName())) {
                jobTypeConfiguration = new SimpleJobConfiguration(coreConfiguration, beanDefinition.getMetadata().getClassName());
            } else if (contains(interfaceNames,DataflowJob.class.getCanonicalName())) {
                jobTypeConfiguration = new DataflowJobConfiguration(coreConfiguration,beanDefinition.getMetadata().getClassName(), true);
            } else {
                jobTypeConfiguration = new ScriptJobConfiguration(coreConfiguration, job.getString("commandLine"));
            }
            final LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(jobTypeConfiguration).overwrite(true).build();
            BeanDefinitionBuilder springJobScheduler = BeanDefinitionBuilder.rootBeanDefinition(SpringJobScheduler.class);
            springJobScheduler.addConstructorArgValue(new RuntimeBeanReference(item.getBeanName()));
            //RuntimeBeanReference Spring Bean的预加载
            springJobScheduler.addConstructorArgValue(new RuntimeBeanReference(ZookeeperRegistryCenter.class));
            springJobScheduler.addConstructorArgValue(liteJobConfiguration);
            springJobScheduler.addConstructorArgValue(new ArrayList<>());
            springJobScheduler.setInitMethodName("init");
            BeanDefinitionHolder holder = new BeanDefinitionHolder(springJobScheduler.getBeanDefinition(), item.getBeanName() + SPRING_JOB_SCHEDULER);
            BeanDefinitionReaderUtils.registerBeanDefinition(holder, getRegistry());
            log.info("{} register to spring success", item.getBeanName());

        });
        return beanDefinitionHolders;
    }
}
