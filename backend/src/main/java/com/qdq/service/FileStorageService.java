package com.qdq.service;

import cn.dev33.satoken.stp.StpUtil;
import com.qdq.entity.SysFile;
import com.qdq.exception.BusinessException;
import com.qdq.mapper.SysFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 文件存储服务
 */
@Slf4j
@Service
public class FileStorageService {

    private final SysFileMapper fileMapper;

    @Value("${quiz.upload.path:./uploads}")
    private String uploadPath;

    public FileStorageService(SysFileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    /**
     * 上传文件
     */
    @Transactional(rollbackFor = Exception.class)
    public SysFile uploadFile(MultipartFile file, String fileType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 验证文件类型
        if (!isValidFileType(fileType)) {
            throw new BusinessException("不支持的文件类型");
        }

        // 验证文件大小(默认100MB)
        long maxFileSize = 100 * 1024 * 1024;
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小不能超过100MB");
        }

        try {
            // 创建上传目录
            String filePath = uploadPath + File.separator + fileType;
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String storedFileName = UUID.randomUUID() + "." + fileExtension;
            String fullFilePath = filePath + File.separator + storedFileName;

            // 保存文件
            file.transferTo(new File(fullFilePath));

            // 记录文件信息到数据库
            SysFile sysFile = new SysFile();
            sysFile.setFileName(originalFileName);
            sysFile.setFilePath(fullFilePath);
            sysFile.setFileType(fileType);
            sysFile.setFileSize(file.getSize());
            sysFile.setMimeType(file.getContentType());
            sysFile.setUploadedBy(StpUtil.getLoginIdAsLong());
            fileMapper.insert(sysFile);

            log.info("文件上传成功: {}", originalFileName);
            return sysFile;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId) {
        SysFile sysFile = fileMapper.selectById(fileId);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }

        // 删除物理文件
        File file = new File(sysFile.getFilePath());
        if (file.exists()) {
            if (!file.delete()) {
                log.warn("物理文件删除失败: {}", sysFile.getFilePath());
            }
        }

        // 逻辑删除数据库记录
        fileMapper.deleteById(fileId);
        log.info("文件删除成功: {}", sysFile.getFileName());
    }

    /**
     * 获取文件信息
     */
    public SysFile getFileInfo(Long fileId) {
        SysFile sysFile = fileMapper.selectById(fileId);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }
        return sysFile;
    }

    /**
     * 验证文件类型
     */
    private boolean isValidFileType(String fileType) {
        return fileType != null && (fileType.equals("audio") || fileType.equals("video") || fileType.equals("image"));
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "bin";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 检查音频/视频文件
     */
    public boolean isAudioOrVideoFile(String fileType) {
        return "audio".equals(fileType) || "video".equals(fileType);
    }
}
