package com.huangyuan.open.gray.common.component;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * 支持自动化配置的RocketMQ消息处理器
 */
public class AutoConfRocketMQProcessor extends AutoConfRocketMQ implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(AutoConfRocketMQProcessor.class);

    private volatile DefaultMQPushConsumer consumer;

    private final MessageListener listener;

    private int fetchSize = 10;

    public AutoConfRocketMQProcessor(String configName, String nameServerKey, String groupKey, String topicKey,
                                     MessageListener listener) {
        super(configName, nameServerKey, groupKey, topicKey);
        this.listener = listener;
    }

    @Override
    protected void doReload() {

        if (consumer == null) {
            LOG.info("CreateRocketMQConsumer");
            createConsumer();
        } else {
            LOG.info("ReCreateRocketMQConsumer");
            shutDownConsumer();
            createConsumer();
        }
    }

    private void createConsumer() {
        consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(nameServer);
        subscribeTopics();
        consumer.setInstanceName(getGuid());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setPullBatchSize(fetchSize);

        if (listener instanceof MessageListenerConcurrently) {
            consumer.registerMessageListener((MessageListenerConcurrently) this.listener);
        } else if (listener instanceof MessageListenerOrderly) {
            consumer.registerMessageListener((MessageListenerOrderly) this.listener);
        } else {
            throw new RuntimeException("UnSupportListenerType");
        }

        try {
            consumer.start();
        } catch (MQClientException e) {
            LOG.error("StartRocketMQConsumer Error", e);
        }
        LOG.info("RocketMQConsumer Started! group={} instance={}", consumer.getConsumerGroup(),
                consumer.getInstanceName());
    }

    private void subscribeTopics() {
        try {
            for (Map.Entry<String, String> i : topics.entrySet()) {
                consumer.subscribe(i.getKey(), i.getValue());
            }
        } catch (MQClientException e) {
            LOG.error("SubscribeTopic Error!", e);
        }
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    private void shutDownConsumer() {
        if (this.consumer != null) {
            try {
                this.consumer.shutdown();
                this.consumer = null;
            } catch (Exception e) {
                LOG.error("ShutRocketMQConsumer Error,nameServer={} group={}", consumer.getNamesrvAddr(),
                        consumer.getConsumerGroup(), e);
            }
        }
    }

    @Override
    public void shutDown() {
        shutDownConsumer();
    }

    public void afterPropertiesSet() throws Exception {
        init();
    }

    public void destroy() throws Exception {
        shutDown();
    }
}
