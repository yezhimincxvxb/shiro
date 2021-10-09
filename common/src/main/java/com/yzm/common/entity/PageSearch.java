package com.yzm.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class PageSearch<T> implements Serializable {
    private static final long serialVersionUID = 2083993467648559840L;

    private Integer page = 1;
    private Integer size = 10;
    private T where;

    public PageSearch(Integer page, Integer size, T t) {
        this.page = page;
        this.size = size;
        this.where = t;
    }
}
