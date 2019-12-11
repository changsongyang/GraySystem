package com.huangyuan.open.gray.common.component.api;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

import java.util.List;

/**
 * 自定义LoadBalance需要实现此接口，以保证提供调用父类方法的能力
 * @author huangy on 2018/9/11
 */
public interface CustomLoadBalance {

    /**
     * 调用父类的方法，实现负载均衡
     */
    <T> Invoker<T> superFilterSelect(List<Invoker<T>> invokers, URL url, Invocation invocation);

}
