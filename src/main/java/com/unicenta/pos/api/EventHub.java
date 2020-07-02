package com.unicenta.pos.api;


import com.google.common.eventbus.EventBus;

public class EventHub {
    public static final String API_ORIGINATED_CHANGE = "API_ORIGINATED_CHANGE";
    public static final String APP_ORIGINATED_CHANGE = "APP_ORIGINATED_CHANGE";

    private static EventBus instance;

    public static EventBus getBus() {
        if (instance != null) {
            return instance;
        }
        instance = new EventBus("APP");
        return instance;
    }

    public static void post(String type, Object payload) {
        getBus().post(new MessageEvent(type, payload));
    }

    public static void post(String type) {
        getBus().post(new MessageEvent(type, null));
    }


    public static void subscribe(Object o) {
        getBus().register(o);
    }

    public static void unsubscribe(Object o) {
        getBus().unregister(o);
    }

}

