package com.atguigu.srb.core;


import com.atguigu.srb.base.dto.SmsDTO;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.rabbitutil.constant.MQConst;
import com.atguigu.srb.rabbitutil.service.MQService;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CoreTest {

    @Autowired
    private RedisTemplate  redisTemplate;

    @Autowired
    private DictMapper dictMapper;

    @Resource
    private MQService mqService;
    
    @Test
    public void testAutoGenerator(){
        System.out.println(1);
    }


    @Test
    public void genCode() {


        // 1、创建代码生成器

        AutoGenerator mpg = new AutoGenerator();


        // 2、全局配置

        GlobalConfig gc = new GlobalConfig();

        String projectPath = System.getProperty("user.dir");

        gc.setOutputDir(projectPath + "/src/main/java");

        gc.setAuthor("Helen");

        gc.setOpen(false); //生成后是否打开资源管理器

        gc.setServiceName("%sService"); //去掉Service接口的首字母I

        gc.setIdType(IdType.AUTO); //主键策略

        gc.setSwagger2(true);//开启Swagger2模式

        mpg.setGlobalConfig(gc);


        // 3、数据源配置

        DataSourceConfig dsc = new DataSourceConfig();

        dsc.setUrl("jdbc:mysql://localhost:3306/srb_core?serverTimezone=GMT%2B8&characterEncoding=utf-8");

        dsc.setDriverName("com.mysql.cj.jdbc.Driver");

        dsc.setUsername("root");

        dsc.setPassword("root");

        dsc.setDbType(DbType.MYSQL);

        mpg.setDataSource(dsc);


        // 4、包配置

        PackageConfig pc = new PackageConfig();

        pc.setParent("com.atguigu.srb.core");

        pc.setEntity("pojo.entity"); //此对象与数据库表结构一一对应，通过 DAO 层向上传输数据源对象。

        mpg.setPackageInfo(pc);


        // 5、策略配置

        StrategyConfig strategy = new StrategyConfig();

        strategy.setNaming(NamingStrategy.underline_to_camel);//数据库表映射到实体的命名策略

        strategy.setColumnNaming(NamingStrategy.underline_to_camel);//数据库表字段映射到实体的命名策略

        strategy.setEntityLombokModel(true); // lombok

        strategy.setLogicDeleteFieldName("is_deleted");//逻辑删除字段名

        strategy.setEntityBooleanColumnRemoveIsPrefix(true);//去掉布尔值的is_前缀（确保tinyint(1)）

        strategy.setRestControllerStyle(true); //restful api风格控制器

        mpg.setStrategy(strategy);

        // 6、执行

        mpg.execute();

    }

    @Test
    public void testRedis(){
        Dict dict = dictMapper.selectById(1);
        redisTemplate.opsForValue().set("dict",dict,5, TimeUnit.MINUTES);
    }

    @Test
    public void testGetDict(){
        Dict dict = (Dict) redisTemplate.opsForValue().get("dict");
        System.out.println(dict);
    }



}
