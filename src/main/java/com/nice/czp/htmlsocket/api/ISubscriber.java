package com.nice.czp.htmlsocket.api;


/**
 * 消息订阅者
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public interface ISubscriber {

    long getId();

    String getTopic();

    void onMessage(IMessage message);

}