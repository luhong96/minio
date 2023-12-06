package com.example.minio.controller;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author amber
 * @Date 2022-12-29 19:40
 */
@Data
public class TaskVo {

    private String sourceIp;
    private String sourceCode;
    private List<Map<String, Object>> lstRunAgent;
    private List<Map<String, Object>> lstRunScript;


}
