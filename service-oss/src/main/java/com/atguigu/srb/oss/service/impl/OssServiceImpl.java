package com.atguigu.srb.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.atguigu.srb.oss.service.OssService;
import com.atguigu.srb.oss.util.OssProperties;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.UUID;

@Service
public class OssServiceImpl implements OssService {

    public OSS getOssClient(){
        OSS ossClient = new OSSClientBuilder().build(OssProperties.ENDPOINT, OssProperties.KEY_ID, OssProperties.KEY_SECRET);
        return  ossClient;
    }

    @Override
    public String upload(String originalFilename, String moduleName, InputStream inputStream) {

//        https://yangxoss.oss-cn-shenzhen.aliyuncs.com/1.jfif
//        https://yangxoss.oss-cn-shenzhen.aliyuncs.com/bbb/2021-09-28/6671da65-6063-48c8-84b1-a860d8f751ef.jpeg
        OSS ossClient = getOssClient();

        if(!ossClient.doesBucketExist(OssProperties.BUCKET_NAME)){
            ossClient.createBucket(OssProperties.BUCKET_NAME);
            ossClient.setBucketAcl(OssProperties.BUCKET_NAME, CannedAccessControlList.PublicRead);
        }

        String date = new DateTime().toString("yyyy-MM-dd");

        String uuid = UUID.randomUUID().toString();
        String filenameExtension = StringUtils.getFilenameExtension(originalFilename);
        String filename = uuid + "." + filenameExtension;



        String key = moduleName+ "/" + date+ "/" + filename ;

        ossClient.putObject(OssProperties.BUCKET_NAME,key,inputStream);

        ossClient.shutdown();

        return "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/" + key;
    }


    @Override
    public void delFile(String filePath) {
        OSS ossClient = getOssClient();
        String host = "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/";
        String fileName = filePath.substring(host.length());
        ossClient.deleteObject(OssProperties.BUCKET_NAME,fileName);
        ossClient.shutdown();
    }

}
