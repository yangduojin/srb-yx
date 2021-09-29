package com.atguigu.srb.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.atguigu.srb.oss.util.OssProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ServiceOssApplication.class)
@RunWith(SpringRunner.class)
public class ossTest {

    @Test
    public void testProperties(){
        System.out.println(OssProperties.BUCKET_NAME);
        System.out.println(OssProperties.ENDPOINT);
        System.out.println(OssProperties.KEY_ID);
        System.out.println(OssProperties.KEY_SECRET);
    }
    
    @Test
    public void testClient(){
        OSS ossClient = new OSSClientBuilder().build(OssProperties.ENDPOINT, OssProperties.KEY_ID, OssProperties.KEY_SECRET);
        ossClient.setBucketAcl(OssProperties.BUCKET_NAME, CannedAccessControlList.PublicRead);
        ossClient.shutdown(); }



}
