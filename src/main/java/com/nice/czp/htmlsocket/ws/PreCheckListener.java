package com.nice.czp.htmlsocket.ws;

import java.util.Map;

import com.nice.czp.htmlsocket.api.IConnListener;
import com.nice.czp.htmlsocket.api.ISubscriber;
import com.nice.czp.htmlsocket.api.WSError;

/**
 * 检测请求是否携带必须的参数,是否重新登录等
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public class PreCheckListener implements IConnListener {

    private MessageCenter mc;

    public PreCheckListener(MessageCenter mc) {
        this.mc = mc;
    }

    @Override
    public void beforeClose(ISubscriber subscriber) {

    }

    @Override
    public WSError beforeConnect(ISubscriber sub, Map<String, String[]> params) {
        String[] subIds = params.get("id");
        if (params.get("topic") == null || subIds == null) {
            return WSError.IDERR;
        }
        String subId = subIds[0];
        if (sub.getId() != null) {
            subId = sub.getId();
        }
        if (mc.getSubscribers().get(subId) != null) {
            return WSError.RELOGIN;
        }
        return null;
    }

}
