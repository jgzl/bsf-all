package com.github.jgzl.bsf.client.format;

import com.github.jgzl.bsf.core.util.PropertyUtils;
import com.github.jgzl.bsf.client.EurekaProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.format.FormatterRegistry;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatRegister implements FeignFormatterRegistrar {
    public DateFormatRegister() {
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(Date.class, String.class, new Date2StringConverter());
    }

    private class Date2StringConverter implements Converter<Date, String> {
        @Override
        public String convert(Date source) {
            SimpleDateFormat sdf = new SimpleDateFormat(PropertyUtils.getPropertyCache(EurekaProperties.FeginDateFormat,"yyyy-MM-dd HH:mm:ss"));
            return sdf.format(source);
        }
    }
}
