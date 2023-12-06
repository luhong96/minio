package com.example.minio.controller;

import io.minio.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author amber
 * @Date 2021-10-14 15:38
 */
@Component
@Slf4j
public class MinioUtils {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String BUCKETNAME;


    /**
     * 创建存储桶
     */
    private void makeBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKETNAME).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKETNAME).build());
            }
        } catch (Exception e) {
            log.error("makeBucket error : {}", ExceptionUtils.getStackTrace(e));
        }
    }


    /**
     * 批量上传文件
     *
     * @param fileList 文件Vo集合
     * @param filePath 文件路径
     */
    @SneakyThrows
    public void uploadFile(MultipartHttpServletRequest multipartRequest, List<FileVo> fileList, String filePath) {
        log.info("start execute uploadFile()...");
        makeBucket();
        List<MultipartFile> files = multipartRequest.getFiles("file");
        List<FileVo> newFileVo = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            FileVo fileVo = fileList.get(i);
            try {
                log.info("bucket:{}, path: {}", BUCKETNAME, filePath + File.separator + fileVo.getFileId() + "." + fileVo.getExtension());
                // 批量上传文件
                minioClient.putObject(PutObjectArgs.builder().bucket(BUCKETNAME)
                        .object(filePath + File.separator + fileVo.getFileId() + "." + fileVo.getExtension())
                        .stream(file.getInputStream(), -1, 10485760).build());
                newFileVo.add(fileVo);
            } catch (Exception e) {
                log.info("file delete error:{}", ExceptionUtils.getStackTrace(e));
                if (newFileVo.size() > 0) {
                    // 删除已上传文件
                    deleteFiles(newFileVo, filePath);
                }
            }
        }
    }


    /**
     * 批量删除文件
     *
     * @param fileList 文件Vo集合
     * @param filePath 文件路径
     */
    public void deleteFiles(List<FileVo> fileList, String filePath) {
        fileList.forEach(fileVo -> {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(BUCKETNAME)
                        .object(filePath + File.separator + fileVo.getFileId() + "." + fileVo.getExtension()).build());
            } catch (Exception e) {
                log.info("file delete error:{}", ExceptionUtils.getStackTrace(e));
            }
        });
    }


    public InputStream fileAsResource2(String filePathString, String fileName) {
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(BUCKETNAME).object(filePathString + File.separator + fileName).build());
        } catch (Exception ex) {
            log.error("file as resource,file not found error:{}", ExceptionUtils.getStackTrace(ex));

        }
        return inputStream;
    }


    public Resource fileAsResource(String filePathString, String fileName) {
        ByteArrayResource resource = null;
        InputStream inputStream = null;
        try {
            byte[] bytes = input2byte(inputStream);
            resource = new ByteArrayResource(bytes);
        } catch (Exception ex) {
            log.error("file as resource,file not found error:{}", ExceptionUtils.getStackTrace(ex));

        }
        return resource;
    }

    private static final byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 1024)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
//        swapStream.close();
//        inStream.close();
        return in2b;
    }

}
