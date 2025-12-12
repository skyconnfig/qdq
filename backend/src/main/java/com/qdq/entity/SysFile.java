package com.qdq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件记录实体类
 */
@Data
@TableName("sys_file")
public class SysFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 存储路径 */
    private String filePath;

    /** 文件类型(audio/video/image) */
    private String fileType;

    /** 文件大小(字节) */
    private Long fileSize;

    /** MIME类型 */
    private String mimeType;

    /** 上传人ID */
    private Long uploadedBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 删除标记 */
    @TableLogic
    private Integer deleted;
}
