package com.example.minio.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * ResponseResult
 *
 * @author liam
 * @date 2020/8/6
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Data
@JsonIgnoreProperties({"args","params"})
public class ResponseResult<T> implements Serializable {
    private static final String SUCCESS_STATUS = "ok";
    private static final String SUCCESS_MSG = "success";
    private static final String FAIL_STATUS = "fail";
    private static final String FAIL_MSG = "fail";

    private static final String PAGE_TOTAL_KEY = "total";

    private int code;
    private String status;
    private String msg;
    private Object[] args;
    private Object[] params;
    private Object data;


    /**
     * 正常返回，携带数据
     * @param data 返回数据
     * @return
     */
    public static ResponseResult OK(Object data) {
        return builder().code(10000).status(SUCCESS_STATUS).msg(SUCCESS_MSG).data(data).build();
    }


}
