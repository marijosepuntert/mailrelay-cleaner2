package com.syrmec.mailrelaycleaner.csv;

import com.opencsv.bean.AbstractBeanField;

public class BooleanConverter extends AbstractBeanField<Boolean, String> {

    @Override
    protected Boolean convert(String value) {
        if (value == null) return false;
        return value.trim().equalsIgnoreCase("true");
    }
}

