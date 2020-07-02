package com.unicenta.pos.api;

public class MessageEvent {

    public final String type;
    public final Object payload;

    public MessageEvent(String type, Object payload) {
        this.payload = payload;
        this.type = type;
    }
}
