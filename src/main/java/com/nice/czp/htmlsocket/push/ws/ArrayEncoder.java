package com.nice.czp.htmlsocket.push.ws;


/*
 * FastPath char[]->byte[] encoder, REPLACE on malformed input or
 * unmappable input.
 */
interface ArrayEncoder {
    int encode(char[] src, int off, int len, byte[] dst);
}
