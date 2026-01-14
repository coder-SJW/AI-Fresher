package com.crm.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CRM 系统主启动类
 *
 * @author AI Assistant
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.crm.system.**.mapper")
public class CrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("CRM 客户管理系统启动成功！");
        System.out.println("API 文档地址: http://localhost:8080/api/doc.html");
        System.out.println("========================================\n");
    }
}
