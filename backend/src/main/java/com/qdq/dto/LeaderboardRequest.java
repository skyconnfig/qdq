package com.qdq.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 排行版配置请求DTO
 */
@Data
public class LeaderboardRequest {

    @NotNull(message = "场次ID不能为空")
    private Long sessionId;

    @NotBlank(message = "排行版名称不能为空")
    private String leaderboardName;

    private List<String> displayFields;

    private Integer sortType;
}
