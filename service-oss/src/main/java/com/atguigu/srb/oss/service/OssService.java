package com.atguigu.srb.oss.service;

import java.io.InputStream;

public interface OssService {
    String upload(String originalFilename, String moduleName, InputStream inputStream);

    void delFile(String fileName);
}
