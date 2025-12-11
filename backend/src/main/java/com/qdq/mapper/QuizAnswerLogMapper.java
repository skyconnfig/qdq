package com.qdq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qdq.entity.QuizAnswerLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题记录 Mapper 接口
 */
@Mapper
public interface QuizAnswerLogMapper extends BaseMapper<QuizAnswerLog> {
}
