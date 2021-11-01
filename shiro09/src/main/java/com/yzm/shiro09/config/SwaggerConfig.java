package com.yzm.shiro09.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.yzm.shiro09.controller"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(getGlobalParameters())
                ;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("swagger服务")
                .description("swagger服务 API 接口文档")
                .version("1.0.0")
                .contact(new Contact("跳转网址", "https://www.baidu.com", "联系邮箱"))
                .build();
    }


    /**
     * 全局参数
     */
    private List<Parameter> getGlobalParameters() {
        // 添加请求参数，我们这里把token作为请求头部参数传入后端
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(
                new ParameterBuilder()
                        .name("Authorization")
                        .description("令牌")
                        .modelRef(new ModelRef("string"))
                        .parameterType("header")
                        .required(false)
                        .build());
        return parameters;
    }

}
