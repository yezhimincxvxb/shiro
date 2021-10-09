package com.yzm.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Generator {

    //创建人
    private static final String author = "Yzm";
    //需要生成实体类等基础信息的表名，多个用逗号隔开
    private static final String[] tables = {"user","role","permissions"};
    //生成文件指定在哪个目录下
    private static final String baoPath = "com.yzm.shiro05";
    private static final String module = "shiro05";
    //数据源连接
    private static final String driverName = "com.mysql.cj.jdbc.Driver";
    private static final String url = "jdbc:mysql://localhost:3306/test3?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    private static final String username = "root";
    private static final String password = "root";

    public static void main(String[] args) {
        //全局配置(必须)
        GlobalConfig globalConfig = globalConfig();
        //数据源配置(必须)
        DataSourceConfig dataSourceConfig = dataSourceConfig();
        //策略配置(必须)
        StrategyConfig strategyConfig = strategyConfig();
        //包名策略配置(必须)
        PackageConfig packageConfig = packageConfig();
        //自定义配置(可无)
        InjectionConfig injectionConfig = injectionConfig();
        //模板配置(可无)
        TemplateConfig templateConfig = templateConfig();
        //整合配置
        AutoGenerator autoGenerator = new AutoGenerator();
        autoGenerator.setGlobalConfig(globalConfig)
                .setDataSource(dataSourceConfig)
                .setStrategy(strategyConfig)
                .setPackageInfo(packageConfig)
                .setCfg(injectionConfig)
                .setTemplate(templateConfig)
                .setTemplateEngine(new FreemarkerTemplateEngine());
        autoGenerator.execute();
    }

    /**
     * 全局配置
     */
    private static GlobalConfig globalConfig() {
        String path = StringUtils.hasLength(module) ? "/" + module + "/src/main/java" : "/src/main/java";
        return new GlobalConfig()
                //是否开启 ActiveRecord 模式
                .setActiveRecord(false)
                //是否开启 Kotlin 模式
                .setKotlin(false)
                .setAuthor(author)
                //生成文件的输出目录
                .setOutputDir(System.getProperty("user.dir") + path)
                //是否打开输出目录
                .setOpen(false)
                //文件是否覆盖
                .setFileOverride(false)
                //主键策略
                .setIdType(IdType.AUTO)
                //默认情况下生成的Service接口的名字首字母都带有I
                .setServiceName("%sService")
                //是否生成基本的sql中的ResultMap
                .setBaseResultMap(true)
                //是否生成基本的sql列
                .setBaseColumnList(true)
                //实体类是否使用swagger注解
                .setSwagger2(false)
                //是否在xml中添加二级缓存配置
                .setEnableCache(false);
    }

    /**
     * 数据源配置
     */
    private static DataSourceConfig dataSourceConfig() {
        return new DataSourceConfig()
                .setDbType(DbType.MYSQL)
                .setDriverName(driverName)
                .setUrl(url)
                .setUsername(username)
                .setPassword(password);
    }

    /**
     * 策略配置(数据库表<-->实体类，表字段<-->类属性)
     */
    private static StrategyConfig strategyConfig() {
        return new StrategyConfig()
                //实体是否链式模型
                .setChainModel(true)
                //controller层使用@RestController注解
                .setRestControllerStyle(true)
                //实体是否使用Lombok工具
                .setEntityLombokModel(true)
                //实体是否生成字段注释
                .setEntityTableFieldAnnotationEnable(true)
                //指定实体类需要继承的父类
                //.setSuperEntityClass(BaseEntity.class)
                //逻辑删除属性名称
                .setLogicDeleteFieldName("deleted")
                //数据库表名，有多个用逗号隔开
                .setInclude(tables)
                //表前缀、字段前缀
                //.setTablePrefix("")
                //.setFieldPrefix("")
                //数据库表名跟实体类名映射的命名策略
                .setNaming(NamingStrategy.underline_to_camel)
                //数据库表字段名跟实体类属性名映射的命名策略
                .setColumnNaming(NamingStrategy.underline_to_camel);
    }

    /**
     * 包名策略配置
     */
    private static PackageConfig packageConfig() {
        return new PackageConfig()
                .setModuleName("")
                .setParent(baoPath)
                .setEntity("entity")
                .setMapper("mapper")
                .setService("service")
                .setServiceImpl("service.impl")
                .setController("controller")
                .setXml("mapper.xml");
    }

    /**
     * 模板配置
     */
    private static TemplateConfig templateConfig() {
        //配置模板
        return new TemplateConfig()
                //配置自定义输出模板
                //指定自定义模板路径，注意不要带上.ftl/.vm, 会根据使用的模板引擎自动识别
                //.setEntity("templates/entity2.java");
                //.setService();
                //.setController();
                .setXml(null);
    }

    /**
     * 自定义配置
     */
    private static InjectionConfig injectionConfig() {
        //自定义配置，该对象可以传递到模板引擎通过 cfg.xxx 引用
        InjectionConfig injectionConfig = new InjectionConfig() {
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<>();
                map.put("abc", this.getConfig().getGlobalConfig().getAuthor() + "-mp");
                this.setMap(map);
            }
        };

        /* 如果模板引擎是 freemarker
        String templatePath = "/templates/mapper.xml.ftl";
        //如果模板引擎是 velocity
        String templatePath = "/templates/mapper.xml.vm";*/

        //自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        //自定义配置会被优先输出
        String path = StringUtils.hasLength(module) ? "/" + module + "/src/main/resources/mapper/" : "/src/main/resources/mapper/";
        focList.add(new FileOutConfig("/templates/mapper.xml.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                //自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return System.getProperty("user.dir") + path + "/" + tableInfo.getEntityName() + "Mapper.xml";
            }
        });
        injectionConfig.setFileOutConfigList(focList);
        return injectionConfig;
    }

}
