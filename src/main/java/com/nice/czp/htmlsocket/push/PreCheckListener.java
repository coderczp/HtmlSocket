package com.nice.czp.htmlsocket.push;

import java.util.Map;

import com.nice.czp.htmlsocket.api.IConnListener;
import com.nice.czp.htmlsocket.api.ISubscriber;
import com.nice.czp.htmlsocket.api.PushError;

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
    public PushError beforeConnect(ISubscriber sub, Map<String, String[]> params) {
        String[] subIds = params.get("id");
        if (params.get("topic") == null || subIds == null) {
            return PushError.IDERR;
        }
        String id = subIds[0];
        for (char c : id.toCharArray()) {
            if (!Character.isDigit(c)) {
                return PushError.IDNOTNUMBER;
            }
        }
        long subId = Long.valueOf(subIds[0]);
        for (ThreadMap map : mc.getSubscribers()) {
            if (map.get(subId) != null) {
                return PushError.RELOGIN;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "PreCheckListener@" + hashCode();
    }

}
