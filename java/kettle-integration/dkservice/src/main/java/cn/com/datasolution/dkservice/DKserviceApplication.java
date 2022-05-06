package cn.com.datasolution.dkservice;

import cn.com.datasolution.config.properties.KettleProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.bridge.ConcreteTextLayoutFactory;
import org.apache.commons.lang.RandomStringUtils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@EnableAsync

@SpringBootApplication
@ComponentScan("cn.com.datasolution.config.properties")
@ComponentScan("cn.com.datasolution.dkservice.controller")

public class DKserviceApplication {

    public static DKserviceApplication instance;
    @Value("${kettle.trans_path}")
    static String transPath;
    public static void main(String[] args) {



        log.warn("DKserviceApplication starting...\n");
            ApplicationContext applicationContext = SpringApplication.run(DKserviceApplication.class, args);
        log.warn("DKserviceApplication started successfully.\n");



    }





}