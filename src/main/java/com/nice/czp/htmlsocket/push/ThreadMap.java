package com.nice.czp.htmlsocket.push;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.IMessage;
import com.nice.czp.htmlsocket.api.ISubscriber;

/**
 * 和线程绑定的map,遍历时在自己的线程中进行
 * 
 * @author coder_czp@126.com-2015年8月14日
 * 
 */
public class ThreadMap extends ConcurrentHashMap<Long, ISubscriber> implements ThreadFactory {

    private static final long serialVersionUID = 1L;
    private static Logger log = LoggerFactory.getLogger(ThreadMap.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor(this);

    public boolean sendTo(long toId, IMessage message) {
        ISubscriber iSubscriber = get(toId);
        if (iSubscriber != null) {
            iSubscriber.onMessage(message);
            return true;
        }
        return false;
    }

    public void broadcastGroup(final String subject, final IMessage message) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                boolean hasFound = false;
                for (ISubscriber sub : values()) {
                    if (sub.getTopic().equals(subject)) {
                        sendMessage(sub, message);
                        hasFound = true;
                    }
                }
                if (!hasFound)
                    log.error("no sub'topic match subject:[{}] in this group", subject);
            }
        });
    }

    public void broadcastAll(final IMessage message) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                for (ISubscriber sub : values()) {
                    sendMessage(sub, message);
                }
            }
        });
    }

    @Override
    public Thread newThread(Runnable task) {
        Thread th = new Thread(task);
        th.setName("ThreadMap-" + th.getId());
        th.setDaemon(true);
        return th;
    }

    private void sendMessage(ISubscriber sub, IMessage message) {
        try {
            sub.onMessage(message);
        } catch (Exception e) {
            log.error("send message error", e);
        }
    }
}
