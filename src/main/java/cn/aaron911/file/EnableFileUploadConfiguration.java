package cn.aaron911.file;

import cn.aaron911.file.core.FileAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({FileAutoConfiguration.class})
public class EnableFileUploadConfiguration {

}
