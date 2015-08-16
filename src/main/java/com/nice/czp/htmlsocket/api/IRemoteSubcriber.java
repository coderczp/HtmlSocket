package com.nice.czp.htmlsocket.api;

/**
 * 远程接入的订阅者
 * 
 * @author coder_czp@126.com-2015年8月13日
 * 
 */
public interface IRemoteSubcriber extends ISubscriber {

    void doClose();
}
