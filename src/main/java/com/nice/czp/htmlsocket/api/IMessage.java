package com.nice.czp.htmlsocket.api;

import java.io.Serializable;

import com.nice.czp.htmlsocket.push.MessageCenter;

/**
 * 
 * 可序列化的消息对象
 * 
 * @author coder_czp@126.com-2015年8月8日
 */
public interface IMessage extends Serializable {

    /**
     * 每个消息都应该携带主题{@link MessageCenter} <br>
     * 会根据这个主题将消息分发到响应的订阅者
     * 
     * @return String 消息主题
     */
    String getSubject();

    /**
     * 消息携带的内容数据
     * 
     * @return Object
     */
    Object getData();

    long getFrom();

    long getTo();

}
