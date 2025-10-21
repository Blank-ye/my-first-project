package com.sky.controller.admin;

import com.sky.config.OSSConfiguration;
import com.sky.constant.AutoFillConstant;
import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/admin/common")
public class CommonController {


    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){


        try {
            //获取文件的原始名称
            String originalFilename = file.getOriginalFilename();
            //截取文件的后缀名
            String indexOf = originalFilename.substring(originalFilename.lastIndexOf("."));
            //将生成的uuid和文件的后缀名组成一个新的文件名称
            String fileName = UUID.randomUUID().toString() + indexOf;

            String filePath = aliOssUtil.upload(file.getBytes(), fileName);
            return Result.success(filePath);
        } catch (IOException e) {
           log.error("文件上传出错，{}",e);
        }
       return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
