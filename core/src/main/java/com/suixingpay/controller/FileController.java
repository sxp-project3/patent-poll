package com.suixingpay.controller;

import com.suixingpay.aspect.Action;
import com.suixingpay.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

/**
 * @author duansiyu
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;
    Map<String, Object> map = new HashMap<>();

    /**
     * 上传文件
     *
     * @param filePatentId
     * @param file
     * @return
     */
    @Action(name = "upload")
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("id") Integer filePatentId, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return fileService.insert(file, filePatentId, request);
    }


    /**
     * @param patentId
     * @return
     */
    @RequestMapping("/select")
    public Map<String, Object> selectById(@RequestParam("id") Integer patentId) {
        return fileService.selectById(patentId);
    }


    /**
     * @param fileId
     * @return
     */
    @Action(name="update")
    @GetMapping("/update")
    public Map<String, Object> update(@RequestParam("fileId") Integer fileId) {
        //用于装数据、状态码、信息并返回
        Map<String, Object> map = new HashMap<>();

        //传值的判空
        if (fileId == null) {
            map.put("result", "参数为空");
            return map;
        }

        int info = fileService.update(fileId);

        //是否删除成功的判断
        if (info > 0) {
            map.put("result", 1);
            LOGGER.info("删除成功");
        } else {
            map.put("result", 0);
            LOGGER.info("删除失败");
        }
        return map;
    }


    /**
     * @param fileId
     * @param request
     */
    @GetMapping("/download")
    public Map<String, Object> selectPathByFileId(@Param("fileId") Integer fileId, HttpServletRequest request) {
        return fileService.selectPathByFileId(fileId, request);
    }
}