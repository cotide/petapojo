package com.viviframework.petapojo.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EnumUtils {

    private static final ReentrantReadWriteLock rrwl = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock rl = rrwl.readLock();
    private static final ReentrantReadWriteLock.WriteLock wl = rrwl.writeLock();
    private static Map<Class<? extends IEnumMessage>, Map<Integer, IEnumMessage>> ENUM_MAPS = new HashMap<>();

    public static <T extends IEnumMessage> T getEnum(Class<T> type, int value) {
        return (T) getEnumValues(type).get(value);
    }

    public static <T extends IEnumMessage> Map<Integer, String> getEnumItems(Class<T> type) {
        Map<Integer, IEnumMessage> map = getEnumValues(type);
        Map<Integer, String> resultMap = new HashMap<>();
        map.forEach((k, v) -> {
            resultMap.put(k, v.getName());
        });
        return resultMap;
    }

    private static <T extends IEnumMessage> Map<Integer, IEnumMessage> getEnumValues(Class<T> clazz) {
        rl.lock();
        try {
            if (ENUM_MAPS.containsKey(clazz))
                return ENUM_MAPS.get(clazz);
        } finally {
            rl.unlock();
        }

        wl.lock();
        try {
            if (ENUM_MAPS.containsKey(clazz))
                return ENUM_MAPS.get(clazz);

            Map<Integer, IEnumMessage> map = new HashMap<>();
            try {
                for (IEnumMessage enumMessage : clazz.getEnumConstants()) {
                    map.put(enumMessage.getValue(), enumMessage);
                }
            } catch (Exception e) {
                throw new RuntimeException("getEnumValues error", e);
            }
            ENUM_MAPS.put(clazz, map);

            return map;
        } finally {
            wl.unlock();
        }
    }
}
