package cn.aaron911.file.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.aaron911.file.core.FileAutoConfiguration;
import org.springframework.context.annotation.Import;


/**
 * 在springboot application 类上启用注解 
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import({FileAutoConfiguration.class})
public @interface EnableFileUpload {

}
