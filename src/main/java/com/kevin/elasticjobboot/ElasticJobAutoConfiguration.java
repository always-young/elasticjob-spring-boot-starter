package com.kevin.elasticjobboot;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kevin Liu
 * @date 2020/7/30 上午9:48
 */
@Configuration
@EnableConfigurationProperties(ElasticJobProperties.class)
public class ElasticJobAutoConfiguration {

    @Bean(initMethod = "init")
    @ConditionalOnMissingBean(ZookeeperRegistryCenter.class)
    public ZookeeperRegistryCenter coordinatorRegistryCenter(ElasticJobProperties properties){
        return new ZookeeperRegistryCenter(new ZookeeperConfiguration(properties.getServerList(), properties.getNamespace()));
    }

}
