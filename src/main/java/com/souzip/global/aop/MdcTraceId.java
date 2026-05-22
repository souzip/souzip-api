package com.souzip.global.aop;

import java.util.UUID;
import org.slf4j.MDC;

public class MdcTraceId {

    private static final String TRACE_ID = "traceId";

    private MdcTraceId() {}

    public static String generate() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static void put(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static void remove() {
        MDC.remove(TRACE_ID);
    }

    public static String get() {
        return MDC.get(TRACE_ID);
    }

    public static boolean isPresent() {
        return get() != null;
    }

    public static boolean putIfAbsent() {
        if (isPresent()) {
            return false;
        }

        put(generate());
        return true;
    }
}
