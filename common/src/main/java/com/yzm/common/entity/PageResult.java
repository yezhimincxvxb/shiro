package com.yzm.common.entity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = -7016165505111217188L;

    // 显示数量
    private long count;
    // 当前页
    private long current;
    // 总数量
    private long total;
    // 总页数
    private long pages;
    // 数据
    private List<T> list;

    public PageResult() {
    }

    public PageResult(IPage<?> page, List<T> list) {
        this.count = page.getSize();
        this.current = page.getCurrent();
        this.total = page.getTotal();
        this.pages = page.getPages();
        this.list = list;
    }

}
