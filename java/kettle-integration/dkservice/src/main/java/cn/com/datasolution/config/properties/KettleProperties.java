package cn.com.datasolution.config.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Administrator
 */

@ConfigurationProperties(prefix = "kettle")
@Component
@Setter
@Getter
public class KettleProperties {
    private String trans_path;
}
