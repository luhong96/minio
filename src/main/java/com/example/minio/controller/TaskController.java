package com.example.minio.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Author amber
 * @Date 2022-12-29 19:37
 */
@Slf4j
@RestController
@RequestMapping("/evo_web/Rest/api/job")
public class TaskController {

    //createSingle
    @PostMapping("/createSingle")
    public Map<String, Object> createSingle(@RequestBody TaskVo taskVo) {
        log.info("createSingle request aaaaaa-----: {}", System.currentTimeMillis());
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> lstTargets = new ArrayList<>();
        Map<String, Object> lstTarget = new HashMap<>();
        lstTarget.put("sTargetGuid", "2141902");
        lstTarget.put("sJobGuid", UUID.randomUUID());
        lstTargets.add(lstTarget);
        result.put("lstTargetJob", lstTargets);
        result.put("code", "200");
        result.put("errorIpList", "");
        return result;
    }

    @PostMapping("/investJobResult")
    public Map<String, Object> investJobResult(@RequestBody TaskResultVo taskResultVo) {
        log.info("createSingle investJobResult bbbbb----: {}", System.currentTimeMillis());
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> lstTargets = new ArrayList<>();
        Map<String, Object> lstTarget = new HashMap<>();
        lstTarget.put("sAgentIp", "170.100.190.166");
        lstTarget.put("sRunUser", "admin");
        lstTarget.put("startTime", System.currentTimeMillis());
        lstTarget.put("endTime", System.currentTimeMillis());
        lstTarget.put("status", 4);
        lstTarget.put("resultContent", "ownner=[#{owner}] workDir=[#{workdir}] jdata=[\"user\":\"root\"]");
        lstTargets.add(lstTarget);

        result.put("returnCode", "000000");
        result.put("returnMsg", "查询成功");
        result.put("jobResult", "");
        result.put("lstRunRt", lstTargets);
        return result;
    }



}
