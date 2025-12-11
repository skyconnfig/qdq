package com.qdq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qdq.entity.QuizQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目 Mapper 接口
 */
@Mapper
public interface QuizQuestionMapper extends BaseMapper<QuizQuestion> {
}
