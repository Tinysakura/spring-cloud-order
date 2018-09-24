package com.cfh.practice.order.enums;

import lombok.Getter;

/**
 * @Author: cfh
 * @Date: 2018/9/24 17:07
 * @Description:
 */
@Getter
public enum OrderErrorsEnum {
    UNPRESENT(4, "确认不存在的订单"),
    ERRORSTATU(5, "确认状态不正确的订单")
    ;
    private Integer code;

    private String message;

    OrderErrorsEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
