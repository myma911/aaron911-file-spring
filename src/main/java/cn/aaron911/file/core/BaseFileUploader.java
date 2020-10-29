package cn.aaron911.file.core;

import cn.aaron911.file.apiClient.*;
import cn.aaron911.file.property.FileProperty;
import org.springframework.beans.factory.annotation.Autowired;

import cn.aaron911.file.exception.GlobalFileException;
import cn.aaron911.file.property.StorageTypeEnum;


public class BaseFileUploader {

	@Autowired
    FileProperty fileProperty;
	

	public IApiClient getApiClient() throws GlobalFileException {
		StorageTypeEnum storageTypeEnum = fileProperty.getStorageTypeEnum();
		if (null == storageTypeEnum) {
			throw new GlobalFileException("[文件服务]当前系统暂未配置文件服务相关的内容！");
		}

        IApiClient res;
		switch (storageTypeEnum) {
		case local:
			String localFileUrl = fileProperty.getLocalFileUrl();
			String localFilePath = fileProperty.getLocalFilePath();
			res = new LocalApiClient().init(localFileUrl, localFilePath);
			break;
		case qiniu:
			String accessKey = fileProperty.getQiniuAccessKey();
			String secretKey = fileProperty.getQiniuSecretKey();
			String qiniuBucketName = fileProperty.getQiniuBucketName();
			String baseUrl = fileProperty.getQiniuBasePath();
			res = new QiniuApiClient().init(accessKey, secretKey, qiniuBucketName, baseUrl);
			break;
		case aliyun:
			String endpoint = fileProperty.getAliyunEndpoint();
			String accessKeyId = fileProperty.getAliyunAccessKey();
			String accessKeySecret = fileProperty.getAliyunAccessKeySecret();
			String url = fileProperty.getAliyunFileUrl();
			String aliYunBucketName = fileProperty.getAliyunBucketName();
			res = new AliyunOssApiClient().init(endpoint, accessKeyId, accessKeySecret, url, aliYunBucketName);
			break;
		case minio:
			String minioEndpoint = fileProperty.getMinioEndpoint();
			String minioAccessKey = fileProperty.getMinioAccessKey();
			String minioSecretKey = fileProperty.getMinioSecretKey();
            String minioBucketName = fileProperty.getMinioBucketName();
            res = new MinIoApiClient().init(minioEndpoint, minioAccessKey, minioSecretKey, minioBucketName);
			break;
		default:
            throw new GlobalFileException("[文件服务]当前系统暂未配置文件服务相关的内容！");
		}
		return res;
	}
}
