package com.qdq.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.qdq.common.R;
import com.qdq.entity.SysFile;
import com.qdq.service.FileStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理控制器
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * 上传音频文件
     */
    @PostMapping("/audio/upload")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<SysFile> uploadAudio(@RequestParam("file") MultipartFile file) {
        SysFile sysFile = fileStorageService.uploadFile(file, "audio");
        return R.ok("音频上传成功", sysFile);
    }

    /**
     * 上传视频文件
     */
    @PostMapping("/video/upload")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<SysFile> uploadVideo(@RequestParam("file") MultipartFile file) {
        SysFile sysFile = fileStorageService.uploadFile(file, "video");
        return R.ok("视频上传成功", sysFile);
    }

    /**
     * 上传图片文件
     */
    @PostMapping("/image/upload")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<SysFile> uploadImage(@RequestParam("file") MultipartFile file) {
        SysFile sysFile = fileStorageService.uploadFile(file, "image");
        return R.ok("图片上传成功", sysFile);
    }

    /**
     * 下载文件
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        SysFile sysFile = fileStorageService.getFileInfo(fileId);
        
        Resource resource = new FileSystemResource(sysFile.getFilePath());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + sysFile.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, sysFile.getMimeType())
                .body(resource);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    @SaCheckRole({"SUPER_ADMIN", "HOST"})
    public R<Void> deleteFile(@PathVariable Long fileId) {
        fileStorageService.deleteFile(fileId);
        return R.ok("删除成功", null);
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/{fileId}")
    public R<SysFile> getFileInfo(@PathVariable Long fileId) {
        SysFile sysFile = fileStorageService.getFileInfo(fileId);
        return R.ok(sysFile);
    }
}
