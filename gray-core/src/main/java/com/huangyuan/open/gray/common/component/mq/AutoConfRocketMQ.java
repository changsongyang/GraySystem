package com.huangyuan.open.gray.common.component.mq;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;


/**
 * RocketMQ 配置中心配置
 */
public abstract class AutoConfRocketMQ {
    protected static String getGuid(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    private static final Logger LOG = LoggerFactory.getLogger(AutoConfRocketMQ.class);

    //配置项目在配置中心的名字
    protected final String configName;
    //name server 在配置中心的key值
    protected final String nameServerKey;
    //group name 在配置中心的key值
    protected final String groupKey;
    //topics 在配置中心的key值
    protected final String topicKey;

    protected volatile String nameServer;

    protected volatile String groupName;

    protected volatile Map<String, String> topics;

    public AutoConfRocketMQ(String configName, String nameServerKey, String groupKey, String topicKey) {

        if (StringUtils.isBlank(configName) || StringUtils.isBlank(nameServerKey) ||
                StringUtils.isBlank(groupKey) || StringUtils.isBlank(topicKey)) {
            throw new IllegalArgumentException(String.format("configName=%s,nameServerKey=%s,groupKey=%s,topicKey=%s",
                    configName, nameServerKey, groupKey, topicKey));
        }

        this.configName = configName;
        this.nameServerKey = nameServerKey;
        this.groupKey = groupKey;
        this.topicKey = topicKey;
    }

    public void init() {
        // TODO by huangy on 2019-12-09. 从公司的配置中心拉取信息
    }

    protected abstract void doReload();

    protected abstract void shutDown();

    private Map<String, String> parseTopics(String topicsStr) {

        if (StringUtils.isBlank(topicsStr)) {
            return Collections.emptyMap();
        }
        String[] topicArray = StringUtils.split(topicsStr, ",");

        Map<String, String> ret = Maps.newHashMap();
        for (String topic : topicArray) {
            String[] entry = StringUtils.split(topic, ":");
            if (entry.length == 0) {
                continue;
            } else if (entry.length == 1) {
                ret.put(entry[0], "*");
            } else {
                ret.put(entry[0], entry[1]);
            }
        }
        return ret;
    }

    public String getNameServer() {
        return nameServer;
    }

    public String getGroupName() {
        return groupName;
    }

    public Map<String, String> getTopics() {
        return Collections.unmodifiableMap(topics);
    }

    public String getTopic() {

        if(topics != null){
            return topics.keySet().iterator().next();
        }

        return null;
    }
}

