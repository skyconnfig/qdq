package com.qdq;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 知识竞赛抢答系统 - 主启动类
 *
 * @author QDQ
 */
@SpringBootApplication
@MapperScan("com.qdq.mapper")
@EnableAsync
@EnableScheduling
public class QuizCompetitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizCompetitionApplication.class, args);
        System.out.println("===============================================");
        System.out.println("    知识竞赛抢答系统启动成功！");
        System.out.println("    访问地址: http://localhost:8080");
        System.out.println("===============================================");
    }
}
