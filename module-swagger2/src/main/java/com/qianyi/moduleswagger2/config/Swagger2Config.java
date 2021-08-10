package com.qianyi.moduleswagger2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
//@EnableSwagger2
@EnableOpenApi
public class Swagger2Config {

    @Value("${project.title}")
    String title;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                // 可以根据url路径设置哪些请求加入文档，忽略哪些请求
//                .paths(PathSelectors.any())
                .paths(PathSelectors.regex("(?!/error).+"))
                .paths(PathSelectors.regex("(?!/authenticationNopass).+"))
                .paths(PathSelectors.regex("(?!/risk).+"))
                .build();
    }
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(title+"开发接口文档") //设置文档的标题
                .description("1.返回状态码(code)为0，表示成功。-1,服务器异常。 1，认证失败。2.授权失败。 6.风险操作。其他代号，展示msg内容即可.<br>" +
                        "2.需要权限的接口，请求头加上authorization字段，值为服务器颁发的jwt令牌。令牌无感刷新，需实时更新") // 设置文档的描述
                .version("1.0.0") // 设置文档的版本信息-> 1.0.0 Version information
//                .termsOfServiceUrl("https://www.baidu.com") // 设置文档的License信息->1.3 License information
                .build();
    }
}
