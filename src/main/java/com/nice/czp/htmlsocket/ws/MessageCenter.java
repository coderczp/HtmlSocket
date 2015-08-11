package com.nice.czp.htmlsocket.ws;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.IMessage;
import com.nice.czp.htmlsocket.api.ISubscriber;

/**
 * 消息中心,负责管理订阅者和派发消息
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public class MessageCenter {

    private static Logger log = LoggerFactory.getLogger(MessageCenter.class);

    private Map<String, ISubscriber> allSubs = new ConcurrentHashMap<String, ISubscriber>();

    public MessageCenter() {
    }

    public void send(IMessage msg) {
        if (msg.getTo() != null) {
            sendTo(msg.getTo(), msg);
        } else if (msg.getSubject() != null) {
            broadcastGroup(msg.getSubject(), msg);
        } else {
            log.info("msg.to and msg.subject is null so send to all");
            broadcastAll(msg);
        }

    }

    /**
     * 发送给指定的订阅者
     * 
     * @param toId
     *            目标
     * @param msg
     */
    public void sendTo(String toId, IMessage msg) {
        ISubscriber sub = allSubs.get(toId);
        if (sub == null) {
            log.error("no ISubscriber'id match:[{}] msg:[{}]", toId, msg);
        } else {
            sub.onMessage(msg);
        }
    }

    /**
     * 广播给subject指定的组
     * 
     * @param subject
     *            组topic
     * @param msg
     *            msg.subject不能为null
     */
    public void broadcastGroup(String subject, IMessage msg) {
        for (ISubscriber item : allSubs.values()) {
            if (item.getTopic().equals(subject)) {
                item.onMessage(msg);
                return;
            }
        }
        log.error("no ISubscriber'topic match:[{}] msg:[{}]", subject, msg);
    }

    /***
     * 广播给所有订阅者
     * 
     * @param message
     */
    public void broadcastAll(IMessage message) {
        for (ISubscriber item : allSubs.values()) {
            item.onMessage(message);
        }
    }

    public void addSubscriber(ISubscriber sub) {
        allSubs.put(sub.getId(), sub);

    }

    public void removeSubscriber(ISubscriber sub) {
        allSubs.remove(sub.getId());
    }

    public Map<String, ISubscriber> getSubscribers() {
        return Collections.unmodifiableMap(allSubs);
    }

}
