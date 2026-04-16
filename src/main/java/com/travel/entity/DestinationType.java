package com.travel.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DestinationType {
    SCENIC_SPOT("景区"),
    SCHOOL("学校");

    private final String desc;

    DestinationType(String desc) {
        this.desc = desc;
    }

    @JsonValue
    public String getDesc() {
        return desc;
    }

    // 关键添加：支持从中文反向查找枚举
    @JsonCreator
    public static DestinationType fromDesc(String desc) {
        for (DestinationType type : values()) {
            if (type.desc.equals(desc) || type.name().equals(desc)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的目的地类型: " + desc);
    }
}
