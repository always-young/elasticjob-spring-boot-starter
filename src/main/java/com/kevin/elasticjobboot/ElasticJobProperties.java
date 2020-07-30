package com.kevin.elasticjobboot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Kevin Liu
 * @date 2020/7/30 上午9:49
 */
@ConfigurationProperties(prefix = "elastic.job")
public class ElasticJobProperties {

    private String serverList;

    private String namespace;

    public ElasticJobProperties() {
    }

    public ElasticJobProperties(String serverList, String namespace) {
        this.serverList = serverList;
        this.namespace = namespace;
    }

    public String getServerList() {
        return serverList;
    }

    public void setServerList(String serverList) {
        this.serverList = serverList;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
