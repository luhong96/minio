package com.example.minio.controller;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author amber
 * @Date 2021-10-14 16:21
 */
@Data
@Accessors(chain = true)
public class FileVo {

    private String accountId;

    private String userId;


    /**
     * 文件ID
     */
    private String fileId;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件大小
     */
    private long size;
    /**
     * 文件的MD5值
     */
    private String fileDigest;
    /**
     * 创建时间
     */
    private long createTime;
    /**
     * 创建用户
     */
    private String createUser;
    /**
     * 文件后缀名
     */
    private String extension;
    /**
     * 临时文件路径
     */
    private String tempFilePath;
    /**
     * 修改人ID
     */
    private String modifyUser;
    /**
     * 操作的最后修改时间
     */
    private long modifyTime;
    /**
     * 文件菜单Id
     */
    private String menuId;
    /**
     * 文件Id集合
     * 文件类型 local 本地 server 服务器
     */
    private List<String> fileIds;

    private String fileType;
}
