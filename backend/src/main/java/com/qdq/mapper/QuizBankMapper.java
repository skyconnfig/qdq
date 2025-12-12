package com.qdq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qdq.entity.QuizBank;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库Mapper
 */
@Mapper
public interface QuizBankMapper extends BaseMapper<QuizBank> {
}
