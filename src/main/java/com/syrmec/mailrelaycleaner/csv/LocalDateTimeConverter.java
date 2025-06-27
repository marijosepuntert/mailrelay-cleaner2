package com.syrmec.mailrelaycleaner.csv;

import com.opencsv.bean.AbstractBeanField;
import java.time.LocalDateTime;

public class LocalDateTimeConverter extends AbstractBeanField<LocalDateTime, String> {

    @Override
    protected LocalDateTime convert(String value) {
        if (value == null || value.isBlank()) return null;
        // Formato ISO 2025-06-10T15:30
        return LocalDateTime.parse(value);
    }
}
