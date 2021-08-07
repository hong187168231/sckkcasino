package com.qianyi.moduleswagger2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
//@EnableSwagger2
@EnableOpenApi
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                // 可以根据url路径设置哪些请求加入文档，忽略哪些请求
//                .paths(PathSelectors.any())
                .paths(PathSelectors.regex("(?!/error).+"))
                .paths(PathSelectors.regex("(?!/authenticationNopass).+"))
                .build();
    }
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("开发接口文档") //设置文档的标题
                .description("返回状态码(code)为0，表示成功，-1,服务器异常。 1，认证未通过。其他表示失败，展示msg内容即可") // 设置文档的描述
                .version("1.0.0") // 设置文档的版本信息-> 1.0.0 Version information
//                .termsOfServiceUrl("http://www.baidu.com") // 设置文档的License信息->1.3 License information
                .build();
    }
}
