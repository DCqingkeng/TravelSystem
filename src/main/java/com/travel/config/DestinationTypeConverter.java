package com.travel.config;

import com.travel.entity.DestinationType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DestinationTypeConverter implements Converter<String, DestinationType> {

    @Override
    public DestinationType convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        // 1. 先尝试直接匹配枚举名（SCENIC_SPOT / SCHOOL）
        try {
            return DestinationType.valueOf(source);
        } catch (IllegalArgumentException ignored) {}

        // 2. 再尝试匹配中文描述（景区 / 学校）
        for (DestinationType type : DestinationType.values()) {
            if (type.getDesc().equals(source)) {
                return type;
            }
        }

        // 3. 都找不到就抛异常，Spring 会自动返回 400 错误
        throw new IllegalArgumentException("无效的目的地类型: " + source + "，允许的值: SCENIC_SPOT/景区 或 SCHOOL/学校");
    }
}
