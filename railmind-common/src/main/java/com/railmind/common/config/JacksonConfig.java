package com.railmind.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // Long → String，前端JS精度问题
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);

            // 日期格式
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

            builder.serializers(new LocalDateSerializer(dateFormatter));
            builder.serializers(new LocalDateTimeSerializer(dateTimeFormatter));
            builder.deserializers(new LocalDateDeserializer(dateFormatter));
            builder.deserializers(new LocalDateTimeDeserializer(dateTimeFormatter));

            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}
