package com.huangyuan.open.gray.common.component.mq;

import com.google.common.collect.Lists;
import com.huangyuan.open.gray.common.support.GrayHandlerHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 控制MQ监听器
 * 用于实现MQ灰度，这里的MQ灰度指的是灰度机器负责监听MQ，而正是机器不监听MQ
 */
@Component("mqListenerController")
public class MQListenerController implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MQListenerController.class);

    public final static String REDIS_KEY_MQ_LISTENER_IP_LIST = "fs-eservice-cases-provider:mqlistener:iplist";

    List<AutoConfRocketMQProcessor> listenerList = Lists.newArrayList();

    //默认状态是没有监听
    boolean currentIsListen = false;
    volatile String currentIp = null;

    @Resource
    private Jedis jedis;

    /**
     * 定时去redis获取,哪些机器需要监听
     */
    public void reloadListener() {
        String listenerIpList = jedis.get(REDIS_KEY_MQ_LISTENER_IP_LIST);

        LOGGER.info("MQListenerController start reload, listener ip list: {}", listenerIpList);

        //如果没有配置,则打开监听(总不能所有服务器都不监听吧)
        if (StringUtils.isEmpty(listenerIpList)) {
            startConsume();
            return;
        }

        List<String> ipList = Lists.newArrayList(listenerIpList.split(";|,"));
        if (ipList.contains("all") || ipList.contains("ALL") || ipList.contains(getCunrrentIP())) {
            startConsume();
            return;
        } else {
            stopConsume();
            return;
        }

    }

    private String getCunrrentIP() {
        if (currentIp == null) {
            currentIp = GrayHandlerHelper.getLocalIp();
        }
        return currentIp;
    }

    /**
     * 暂停所有mq的监听
     */
    private void stopConsume() {
        if (currentIsListen) {
            LOGGER.info("MQListenerController change: stop consume");
            //如果当前打开,那么关闭
            listenerList.forEach(x -> {
                x.shutDown();
            });
            currentIsListen = false;
        }
    }

    /**
     * 重新启动所有mq的监听
     */
    private void startConsume() {
        if (!currentIsListen) {
            LOGGER.info("MQListenerController change: start consume");

            //如果当前没打开,那么打开
            listenerList.forEach(x -> {
                x.init();
            });
            currentIsListen = true;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AutoConfRocketMQProcessor> listenerMap = applicationContext.getBeansOfType(AutoConfRocketMQProcessor.class);
        listenerMap.forEach((x, y) -> {
            listenerList.add(y);
        });
    }
}
