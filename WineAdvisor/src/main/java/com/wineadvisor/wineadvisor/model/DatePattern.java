package com.wineadvisor.wineadvisor.model;

import java.util.Date;

import lombok.Data;

@Data
public abstract class DatePattern {
    private Date date;
    private Integer age;
}
