package com.example.minio.controller;

import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @Author amber
 * @Date 2021-10-14 15:08
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/automation" + "/task")
public class FileController {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioUtils minioUtils;

    /**
     * 上传本地文件到临时目录
     *
     * @param accountId
     * @param userId
     * @param fileMd5
     * @param fileName
     * @param request
     * @return
     */
    @PostMapping("/file/upload/{fileMd5}/{fileName:.+}")
    public ResponseResult fileUpload(@RequestParam("accountId") String accountId,
                                     @RequestParam("userId") String userId,
                                     @PathVariable(value = "fileMd5") String fileMd5,
                                     @PathVariable(value = "fileName") String fileName,
                                     HttpServletRequest request) {
        FileVo fileVo = new FileVo();
        fileVo.setFileName(fileName);
        fileVo.setFileDigest(fileMd5);
        fileVo.setAccountId(accountId);
        fileVo.setUserId(userId);

        List<FileVo> result = ddd(request, fileVo);
        return ResponseResult.OK(result);
    }


    /**
     * 文件下载
     */
    @GetMapping("/file/download")
    public ResponseEntity<Resource> fileDownload(@RequestParam("accountId") String accountId,
                                                 @RequestParam("userId") String userId,
                                                 @RequestParam(value = "fileId") String fileId,
                                                 @RequestParam(value = "fileName") String fileName,
                                                 @RequestParam(value = "fileType") String fileType,
                                                 HttpServletRequest request) {
        FileVo fileVo = new FileVo();
        fileVo.setFileId(fileId)
                .setFileName(fileName)
                .setFileType(fileType)
                .setAccountId(accountId)
                .setUserId(userId);
        ResponseEntity<Resource> resource;
        try {
            resource = downloadFile(fileVo, request);
            return resource;
        } catch (Exception e) {
            log.error("file fileDownload error:{}", ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    @GetMapping("/file/remove")
    public void removeObject(@RequestParam("accountId") String accountId,
                             @RequestParam("userId") String userId,
                             @RequestParam("filePath")String filePath,
                             @RequestBody List<FileVo> files) {
//        List<FileVo> o = JsonUtil.parseObject(files, List.class);
        minioUtils.deleteFiles(files,filePath);

    }

    /**
     * 文件下载
     */
    @GetMapping("/file/download2")
    public ResponseEntity<InputStreamResource> fileDownload2(@RequestParam("accountId") String accountId,
                                                             @RequestParam("userId") String userId,
                                                             @RequestParam(value = "fileId") String fileId,
                                                             @RequestParam(value = "fileName") String fileName,
                                                             @RequestParam(value = "fileType") String fileType,
                                                             HttpServletRequest request) {
        FileVo fileVo = new FileVo();
        fileVo.setFileId(fileId)
                .setFileName(fileName)
                .setFileType(fileType)
                .setAccountId(accountId)
                .setUserId(userId);
        ResponseEntity<InputStreamResource> resource;
        try {
            resource = downloadFile2(fileVo, request);
            return resource;
        } catch (Exception e) {
            log.error("file fileDownload error:{}", ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    /**
     * contentType 参数
     */
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream;charset=UTF-8";
//    public static final String CONTENT_TYPE_OCTET_STREAM = "application/x-download;charset=UTF-8";
    /**
     * header参数
     */
    public static final String CONTENT_DISPOSITION_ATTACHMENT_FILENAME = "attachment;filename=";

    public ResponseEntity<InputStreamResource> downloadFile2(FileVo fileVo, HttpServletRequest request) {
        ResponseEntity<InputStreamResource> responseEntity = null;
        InputStream inputStream = null;
        try {
            // 获取文件名称
            String fileNameSuffix = fileVo.getFileName().substring(fileVo.getFileName().lastIndexOf("."));
            String fileName = fileVo.getFileId() + fileNameSuffix;

            // 获取临时文件路径
            String fileTempPath = "/Users";

            inputStream = minioUtils.fileAsResource2(fileTempPath, fileName);
            responseEntity = fileStreamHeader(inputStream, request, fileVo.getFileName());
        } catch (Exception e) {
            log.error("file download error:{}", ExceptionUtils.getStackTrace(e));
//            throw new BaseException(ResponseCode.FILE_DOWNLOAD_ERROR);
        }
        return responseEntity;
    }

    public ResponseEntity<Resource> downloadFile(FileVo fileVo, HttpServletRequest request) {
        Resource resource = null;
        ResponseEntity<Resource> responseEntity = null;
        try {
            // 获取文件名称
            String fileNameSuffix = fileVo.getFileName().substring(fileVo.getFileName().lastIndexOf("."));
            String fileName = fileVo.getFileId() + fileNameSuffix;

            // 获取临时文件路径
            String fileTempPath = "/Users";

            // 加载文件
            resource = minioUtils.fileAsResource(fileTempPath, fileName);
            responseEntity = fileStreamHeader(resource, request, fileVo.getFileName());
        } catch (Exception e) {
            log.error("file download error:{}", ExceptionUtils.getStackTrace(e));
//            throw new BaseException(ResponseCode.FILE_DOWNLOAD_ERROR);
        }
        return responseEntity;
    }

    public static ResponseEntity<Resource> fileStreamHeader(Resource resource, HttpServletRequest request, String filename) throws UnsupportedEncodingException {
        String contentType = CONTENT_TYPE_OCTET_STREAM;
        try {
            if (contentType == null) {
                contentType = CONTENT_TYPE_OCTET_STREAM;
            }

        } catch (Exception e) {
            log.error("file Stream Header error:{}", ExceptionUtils.getStackTrace(e));
//            throw new BaseException("file not found ");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, CONTENT_DISPOSITION_ATTACHMENT_FILENAME + URLEncoder.encode(filename, "utf-8"))
                .body(resource);
    }


    public static ResponseEntity<InputStreamResource> fileStreamHeader(InputStream inputStream, HttpServletRequest request, String filename) throws UnsupportedEncodingException {
        String contentType = CONTENT_TYPE_OCTET_STREAM;
        try {
            if (contentType == null) {
                contentType = CONTENT_TYPE_OCTET_STREAM;
            }

        } catch (Exception e) {
            log.error("file Stream Header error:{}", ExceptionUtils.getStackTrace(e));
//            throw new BaseException("file not found ");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, CONTENT_DISPOSITION_ATTACHMENT_FILENAME + URLEncoder.encode(filename, "utf-8"))
                .body(new InputStreamResource(inputStream));
    }


    private void fileUploads(HttpServletRequest request, FileVo fileVo, String tempPath, List<FileVo> fileList) {
        //上传
        try {

//            File destDir = new File(tempPath);
//            if (!destDir.exists()) {
//                FileUtils.forceMkdir(destDir);
//            }
            //上传时间
            long time = System.currentTimeMillis();
            // 文件List Vo数据集
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            processFiles(multipartRequest, fileVo, time, fileList);
        } catch (Exception e) {
            log.error("uploadFile error, error: {}", ExceptionUtils.getStackTrace(e));
//            throw new BaseException(ResponseCode.FILE_UPLOAD_ERROR);
        }
    }

    public static void processFiles(MultipartHttpServletRequest multipartRequest, FileVo fileVo, long time, List<FileVo> fileList) throws IOException {
        List<MultipartFile> files = multipartRequest.getFiles("file");
        for (MultipartFile file : files) {
            long fileSize = file.getSize();
            // 文件名称
            String originalFilename = file.getOriginalFilename();
            log.info("file Name,fileName: {}", originalFilename);
            // 文件名称长度校验
            String fileNameSize = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            if (fileNameSize.length() > 100) {
                log.info("file name too long,fileName: {}", fileNameSize);
//                throw new BaseException(ResponseCode.FILE_NAME_TOO_LONG);
            }
            String fileNewName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            String fileNameSuffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            // FileVo对象赋值
            fileVo.setFileId(fileVo.getFileDigest());
            fileVo.setFileName(fileNewName);
            fileVo.setFileDigest(fileVo.getFileDigest());
            fileVo.setCreateTime(time);
            fileVo.setSize(fileSize);
            fileVo.setExtension(fileNameSuffix);
            fileVo.setModifyTime(time);
            fileList.add(fileVo);
        }
    }

    public List<FileVo> ddd(HttpServletRequest request, FileVo fileVo) {
        log.info("start execuete fileUpload params fileVo{}", fileVo);
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("file");
        List<FileVo> fileList = new ArrayList<>();
        try {
            File files = new File("/Users");
            //校验文件md5是否相同
            if (files.exists()) {
                if (Arrays.stream(files.list()).parallel().anyMatch(fileNames -> fileNames.contains(fileVo.getFileDigest()))) {
//                    throw new BaseException(ResponseCode.FILE_UPLOAD_FINISHED);
                }
            }
            //上传
            fileUploads(request, fileVo, "/Users", fileList);
            minioUtils.uploadFile(multipartRequest, fileList, "/Users");

        } catch (Exception e) {
            log.error("checkFileMd5 error,err: {}", ExceptionUtils.getStackTrace(e));
//            if (e instanceof BaseException) {
//                if (((BaseException) e).getCode() == ResponseCode.FILE_UPLOAD_FINISHED) {
            long currTime = System.currentTimeMillis();
            String fileNewName = fileVo.getFileName().substring(0, fileVo.getFileName().lastIndexOf("."));
            String fileNameSuffix = fileVo.getFileName().substring(fileVo.getFileName().lastIndexOf(".") + 1);
            Map<String, Object> returnMap = new HashMap<>();
            fileVo.setFileId(fileVo.getFileDigest());
            fileVo.setFileName(fileNewName);
            fileVo.setSize(multipartFile.getSize());
            fileVo.setCreateTime(currTime);
            fileVo.setExtension(fileNameSuffix);
            fileVo.setModifyTime(currTime);
            fileList.add(fileVo);
            log.debug("{} has exists,return ok", fileNewName);
        }
        return fileList;
    }
}
