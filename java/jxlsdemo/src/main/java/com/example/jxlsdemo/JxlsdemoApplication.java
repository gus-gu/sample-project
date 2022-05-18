package com.example.jxlsdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import org.jxls.common.Context;
import org.jxls.jdbc.JdbcHelper;
import org.jxls.util.JxlsHelper;

@SpringBootApplication
public class JxlsdemoApplication {

    public static void main(String[] args) throws ClassNotFoundException {

        //SpringApplication.run(JxlsdemoApplication.class, args);
        Class.forName("ru.yandex.clickhouse.ClickHouseDriver");
        try (Connection conn = DriverManager.getConnection("jdbc:clickhouse://localhost:8123/sakila", "default", "111111")) {
            //initData(conn);
            JdbcHelper jdbcHelper = new JdbcHelper(conn);
            try (InputStream is = new FileInputStream("D:\\temp\\jxls\\demo\\sql_demo_template.xls")) {
                try (OutputStream os = new FileOutputStream("D:\\temp\\jxls\\demo\\sql_demo_output.xls")) {
                    Context context = new Context();
                    context.putVar("conn", conn);
                    context.putVar("jdbc", jdbcHelper);
                    //JxlsHelper.getInstance().getExpressionEvaluatorFactory()
                    JxlsHelper.getInstance().processTemplate(is, os, context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
