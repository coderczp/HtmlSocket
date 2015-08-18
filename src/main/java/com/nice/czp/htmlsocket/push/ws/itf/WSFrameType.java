package com.nice.czp.htmlsocket.push.ws.itf;

import com.nice.czp.htmlsocket.push.ws.err.ProtocolError;

/**
 * TODO Please Descrip This Class
 * 
 * @author coder_czp@126.com-2015年8月17日
 * 
 */
public enum WSFrameType {

    Continuation(0x0), TXT(0x1), BIN(2), CLOSE(0x08), PING(0x09), PONG(0x0A);

    int value;

    private WSFrameType(int value) {
        this.value = value;
    }

    public static WSFrameType from(int code) {
        int opcode = code & 0xF;
        switch (opcode) {
        case 0x00:
            return Continuation;
        case 0x01:
            return TXT;
        case 0x02:
            return BIN;
        case 0x08:
            return CLOSE;
        case 0x09:
            return PING;
        case 0x0A:
            return PONG;
        default:
            throw new ProtocolError(IWebsocket.BAD_DATA, String.format("Unknown frame type:0x%s",
                    Integer.toHexString(opcode)));
        }
    }
}
