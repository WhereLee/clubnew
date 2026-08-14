package com.club.vo;

import lombok.Data;

import java.util.List;

@Data
public class RouterVO {
    private String name;
    private String path;
    private String component;
    private String icon;
    private List<RouterVO> children;

    public RouterVO() {}

    public RouterVO(String name, String path, String component, String icon) {
        this.name = name;
        this.path = path;
        this.component = component;
        this.icon = icon;
    }
}
