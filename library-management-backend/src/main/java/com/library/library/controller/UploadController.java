package com.library.library.controller;

import com.library.library.vo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "*")
public class UploadController {

    @Value("${upload.path.bookimages:C:/Users/Teonya/OneDrive/Desktop/58/images/bookimages}")
    private String bookImagesPath;

    @Value("${upload.path.userimages:C:/Users/Teonya/OneDrive/Desktop/58/images/userimages}")
    private String userImagesPath;

    @Value("${upload.base-url:http://localhost:8080}")
    private String baseUrl;

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "type", defaultValue = "book") String type) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成新文件名
        String newFilename = UUID.randomUUID().toString() + extension;

        // 根据type选择上传目录
        String uploadPath = "book".equals(type) ? bookImagesPath : userImagesPath;
        String folder = "book".equals(type) ? "bookimages" : "userimages";

        // 确保目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        File destFile = new File(uploadPath, newFilename);
        try {
            file.transferTo(destFile);
            // 返回访问路径（完整URL）
            String url = baseUrl + "/images/" + folder + "/" + newFilename;
            return Result.success(url);
        } catch (IOException e) {
            return Result.error("文件保存失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/image")
    public Result<Void> deleteImage(@RequestParam("url") String url) {
        try {
            if (url != null && url.contains("/images/")) {
                String path = url.substring(url.indexOf("/images/") + 8);
                String fileName = path.substring(path.indexOf("/") + 1);
                String folder = path.substring(0, path.indexOf("/"));
                
                String uploadPath;
                if ("bookimages".equals(folder)) {
                    uploadPath = bookImagesPath;
                } else if ("userimages".equals(folder)) {
                    uploadPath = userImagesPath;
                } else {
                    return Result.error("无效的图片路径");
                }
                
                File file = new File(uploadPath, fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除文件失败: " + e.getMessage());
        }
    }
}
