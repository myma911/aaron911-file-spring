package cn.aaron911.file.alioss.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.aaron911.file.core.IProgressListener;
import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.BucketReferer;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectPermission;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyun.oss.model.SetBucketCORSRequest;
import com.aliyun.oss.model.StorageClass;
import cn.aaron911.file.alioss.entity.BucketEntity;
import cn.aaron911.file.alioss.entity.CorsRoleEntity;
import cn.aaron911.file.alioss.entity.ObjectsRequestEntity;
import cn.aaron911.file.alioss.entity.RefererEntity;
import cn.aaron911.file.exception.OssApiException;


public class OssApi {
    private static final Logger log = LoggerFactory.getLogger(OssApi.class);

    private OSSClient client;

    public OssApi(OSSClient client) {
        this.client = client;
    }

    public OssApi(String endpoint, String accessKeyId, String accessKeySecret) {
        this.client = new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 授权访问文件的URL
     *
     * @param fileName       待授权的文件名
     * @param bucketName     存储空间
     * @param expirationTime 授权失效时间，单位秒
     */
    public String authFile(String fileName, String bucketName, long expirationTime) throws OssApiException {
        try {
            if (!this.client.doesBucketExist(bucketName)) {
                throw new OssApiException("[阿里云OSS] 无法授权访问文件的URL！Bucket不存在：" + bucketName);
            }
            if (!this.client.doesObjectExist(bucketName, fileName)) {
                throw new OssApiException("[阿里云OSS] 文件授权失败！文件不存在：" + bucketName + "/" + fileName);
            }
            // 设置URL过期时间为1小时
            Date expiration = new Date(System.currentTimeMillis() + expirationTime * 1000);
            // 生成URL
            return this.client.generatePresignedUrl(bucketName, fileName, expiration).toString();
        } finally {
            this.shutdown();
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param fileName   OSS中保存的文件名
     * @param bucketName 存储空间
     */
    public boolean isExistFile(String fileName, String bucketName) throws OssApiException {
        try {
            if (!this.client.doesBucketExist(bucketName)) {
                throw new OssApiException("[阿里云OSS] Bucket不存在：" + bucketName);
            }
            return this.client.doesObjectExist(bucketName, fileName);
        } finally {
            this.shutdown();
        }
    }

    /**
     * 获取指定bucket下的文件的访问权限
     *
     * @param fileName   OSS中保存的文件名
     * @param bucketName 存储空间
     * @return
     */
    public ObjectPermission getFileAcl(String fileName, String bucketName) throws OssApiException {
        try {
            if (!this.client.doesBucketExist(bucketName)) {
                throw new OssApiException("[阿里云OSS] 无法获取文件的访问权限！Bucket不存在：" + bucketName);
            }
            if (!this.client.doesObjectExist(bucketName, fileName)) {
                throw new OssApiException("[阿里云OSS] 无法获取文件的访问权限！文件不存在：" + bucketName + "/" + fileName);
            }
            return this.client.getObjectAcl(bucketName, fileName).getPermission();
        } finally {
            this.shutdown();
        }
    }

    /**
     * 获取文件列表
     *
     * @param bucketName 存储空间名
     * @param request    查询条件
     * @return 文件列表
     */
    public List<OSSObjectSummary> listFile(String bucketName, ObjectsRequestEntity request) throws OssApiException {
        try {
            if (!this.client.doesBucketExist(bucketName)) {
                throw new OssApiException("[阿里云OSS] 无法获取文件列表！Bucket不存在：" + bucketName);
            }
            ListObjectsRequest listRequest = new ListObjectsRequest(bucketName);
            if (null != request) {
                listRequest.withDelimiter(request.getDelimiter())
                        .withEncodingType(request.getEncodingType())
                        .withMarker(request.getMarker())
                        .withMaxKeys(request.getMaxKeys())
                        .withPrefix(request.getPrefix());
            }
            // 列举Object
            ObjectListing objectListing = this.client.listObjects(listRequest);
            return objectListing.getObjectSummaries();
        } finally {
            this.shutdown();
        }
    }

    /**
     * 修改指定bucket下的文件的访问权限
     *
     * @param fileName   OSS中保存的文件名
     * @param bucketName 保存文件的目标bucket
     * @param acl        权限
     */
    public void setFileAcl(String fileName, String bucketName, CannedAccessControlList acl) throws OssApiException {
        try {
            boolean exists = this.client.doesBucketExist(bucketName);
            if (!exists) {
                throw new OssApiException("[阿里云OSS] 无法修改文件的访问权限！Bucket不存在：" + bucketName);
            }
            if (!this.client.doesObjectExist(bucketName, fileName)) {
                throw new OssApiException("[阿里云OSS] 无法修改文件的访问权限！文件不存在：" + bucketName + "/" + fileName);
            }
            this.client.setObjectAcl(bucketName, fileName, acl);
        } finally {
            this.shutdown();
        }
    }

    /**
     * 删除文件
     *
     * @param bucketName 保存文件的目标bucket
     * @param fileName   OSS中保存的文件名
     */
    public void deleteFile(String fileName, String bucketName) throws OssApiException {
        try {
            boolean exists = this.client.doesBucketExist(bucketName);
            if (!exists) {
                throw new OssApiException("[阿里云OSS] 文件删除失败！Bucket不存在：" + bucketName);
            }
            if (!this.client.doesObjectExist(bucketName, fileName)) {
                throw new OssApiException("[阿里云OSS] 文件删除失败！文件不存在：" + bucketName + "/" + fileName);
            }
            this.client.deleteObject(bucketName, fileName);
        } finally {
            this.shutdown();
        }
    }

    /**
     * 创建存储空间
     *
     * @param bucketName 存储空间名称
     */
    public void createBucket(String bucketName) throws OssApiException {
        try {
            boolean exists = this.client.doesBucketExist(bucketName);
            if (exists) {
                throw new OssApiException("[阿里云OSS] Bucket创建失败！Bucket名称[" + bucketName + "]已被使用！");
            }
            // -- 创建指定类型的Bucket，请使用Java SDK 2.6.0及以上版本。
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
            // 设置bucket权限为公共读，默认是私有读写
            createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
            // 设置bucket存储类型为低频访问类型，默认是标准类型
            createBucketRequest.setStorageClass(StorageClass.IA);
            this.client.createBucket(createBucketRequest);
        } finally {
            this.shutdown();
        }
    }

    /**
     * 设置bucket的访问权限
     *
     * @param bucket bucket
     */
    public void setBucketAcl(BucketEntity bucket) throws OssApiException {
        try {
            if (!this.client.doesBucketExist(bucket.getBucketName())) {
                throw new OssApiException("[阿里云OSS] 无法修改Bucket的访问权限！Bucket不存在：" + bucket.getBucketName());
            }
            this.client.setBucketAcl(bucket.getBucketName(), bucket.getAcl());
        } finally {
            this.shutdown();
        }
    }

    /**
     * 跨域访问管理：跨域资源共享(CORS)允许web端的应用程序访问不属于本域的资源
     *
     * @param corsRole 跨域规则
     */
    public void setBucketCors(CorsRoleEntity corsRole) throws OssApiException {
        try {
            if (!this.client.doesBucketExist(corsRole.getBucketName())) {
                throw new OssApiException("[阿里云OSS] 无法修改Bucket的跨域设置！Bucket不存在：" + corsRole.getBucketName());
            }
            SetBucketCORSRequest request = new SetBucketCORSRequest(corsRole.getBucketName());

            //CORS规则的容器,每个bucket最多允许10条规则
            ArrayList<SetBucketCORSRequest.CORSRule> putCorsRules = new ArrayList<>();
            SetBucketCORSRequest.CORSRule corRule = new SetBucketCORSRequest.CORSRule();

            corRule.setAllowedMethods(corsRole.getAllowedMethod());
            corRule.setAllowedOrigins(corsRole.getAllowedOrigin());
            corRule.setAllowedHeaders(corsRole.getAllowedHeader());
            corRule.setExposeHeaders(corsRole.getExposedHeader());
            //指定浏览器对特定资源的预取(OPTIONS)请求返回结果的缓存时间,单位为秒。
            corRule.setMaxAgeSeconds(corsRole.getMaxAgeSeconds());
            //最多允许10条规则
            putCorsRules.add(corRule);

            request.setCorsRules(putCorsRules);
            this.client.setBucketCORS(request);
        } finally {
            this.shutdown();
        }
    }

    /**
     * 创建模拟文件夹本质上来说是创建了一个名字以“/”结尾的文件；<br>
     * 对于这个文件照样可以上传下载,只是控制台会对以“/”结尾的文件以文件夹的方式展示；<br>
     * 多级目录创建最后一级即可，比如dir1/dir2/dir3/，创建dir1/dir2/dir3/即可，dir1/、dir1/dir2/不需要创建；
     *
     * @param folder     目录名
     * @param bucketName 存储空间
     */
    public void createFolder(String folder, String bucketName) throws OssApiException {
        try {
            if (null == bucketName) {
                throw new OssApiException("[阿里云OSS] 尚未指定Bucket！");
            }

            if (!this.client.doesBucketExist(bucketName)) {
                throw new OssApiException("[阿里云OSS] 无法创建目录！Bucket不存在：" + bucketName);
            }
            folder = folder.endsWith("/") ? folder : folder + "/";
            this.client.putObject(bucketName, folder, new ByteArrayInputStream(new byte[0]));
        } finally {
            this.shutdown();
        }
    }

    /**
     * 批量设置Referer白名单
     *
     * @param refererEntity refererEntity
     */
    public void addReferers(RefererEntity refererEntity) throws OssApiException {
        try {
            if (!this.client.doesBucketExist(refererEntity.getBucketName())) {
                throw new OssApiException("[阿里云OSS] 无法设置Referer白名单！Bucket不存在：" + refererEntity.getBucketName());
            }
            if (CollectionUtils.isEmpty(refererEntity.getRefererList())) {
                return;
            }
            BucketReferer br = new BucketReferer(true, refererEntity.getRefererList());
            this.client.setBucketReferer(refererEntity.getBucketName(), br);
        } finally {
            this.shutdown();
        }
    }

    /**
     * 清空Referer白名单
     *
     * @param bucketName 存储空间名
     */
    public void removeReferers(String bucketName) throws OssApiException {
        try {
            if (!this.client.doesBucketExist(bucketName)) {
                throw new OssApiException("[阿里云OSS] 无法清空Referer白名单！Bucket不存在：" + bucketName);
            }
            // 默认允许referer字段为空，且referer白名单为空。
            BucketReferer br = new BucketReferer();
            client.setBucketReferer(bucketName, br);
        } finally {
            this.shutdown();
        }
    }

    /**
     * 获取Referer白名单
     *
     * @param bucketName 存储空间名
     */
    public List<String> getReferers(String bucketName) throws OssApiException {
        try {
            if (!this.client.doesBucketExist(bucketName)) {
                throw new OssApiException("[阿里云OSS] 无法获取Referer白名单！Bucket不存在：" + bucketName);
            }
            BucketReferer br = this.client.getBucketReferer(bucketName);
            return br.getRefererList();
        } finally {
            this.shutdown();
        }
    }
    
    /**
     * @param localFile 待上传的文件
     * @param fileName  文件名:最终保存到云端的文件名
     * @param bucket    需要上传到的目标bucket
     * 
     */
    public String uploadFile(File localFile, String fileName, String bucket) throws OssApiException {
        try {
            InputStream inputStream = new FileInputStream(localFile);
            return this.uploadFile(inputStream, fileName, bucket, null);
        } catch (Exception e) {
            throw new OssApiException("[阿里云OSS] 文件上传失败！" + localFile, e);
        } finally {
            this.shutdown();
        }
    }
    

    /**
     * @param localFile 待上传的文件
     * @param fileName  文件名:最终保存到云端的文件名
     * @param bucket    需要上传到的目标bucket
     * @param listener    上传进度监听器
     */
    public String uploadFile(File localFile, String fileName, String bucket, IProgressListener listener) throws OssApiException {
        try {
            InputStream inputStream = new FileInputStream(localFile);
            return this.uploadFile(inputStream, fileName, bucket, listener);
        } catch (Exception e) {
            throw new OssApiException("[阿里云OSS] 文件上传失败！" + localFile, e);
        } finally {
            this.shutdown();
        }
    }
    
    
    /**
     *不 带进度监听器
     * @param inputStream 待上传的文件流
     * @param fileName    文件名:最终保存到云端的文件名
     * @param bucketName  需要上传到的目标bucket
     */
    public String uploadFile(InputStream inputStream, String fileName, String bucketName) throws OssApiException {
        try {
            if (!this.client.doesBucketExist(bucketName)) {
                throw new OssApiException("[阿里云OSS] 无法上传文件！Bucket不存在：" + bucketName);
            }
            PutObjectResult result = this.client.putObject(bucketName, fileName, inputStream);
            return result.getETag();
        } finally {
            this.shutdown();
        }
    }
    
    

    /**
     * 带进度监听器
     * @param inputStream 待上传的文件流
     * @param fileName    文件名:最终保存到云端的文件名
     * @param bucketName  需要上传到的目标bucket
     */
    public String uploadFile(InputStream inputStream, String fileName, String bucketName, IProgressListener listener) throws OssApiException {
        try {
            if (!this.client.doesBucketExist(bucketName)) {
                throw new OssApiException("[阿里云OSS] 无法上传文件！Bucket不存在：" + bucketName);
            }
            PutObjectResult result = null;
            if (null != listener) {
            	// 上传文件的同时指定了进度条参数。
                PutObjectRequest withProgressListener = new PutObjectRequest(bucketName, fileName, inputStream).<PutObjectRequest>withProgressListener(new ProgressListener() {
                    private long bytesWritten = 0;
                    private long totalBytes = -1;
                    private boolean succeed = false;

                    @Override
                    public void progressChanged(ProgressEvent progressEvent) {
                        long bytes = progressEvent.getBytes();
                        ProgressEventType eventType = progressEvent.getEventType();
                        switch (eventType) {
                            case TRANSFER_STARTED_EVENT:
                                if (log.isDebugEnabled()) {
                                    log.debug("Start to upload......");
                                }
                                listener.start();
                                break;
                            case REQUEST_CONTENT_LENGTH_EVENT:
                                this.totalBytes = bytes;
                                if (log.isDebugEnabled()) {
                                    log.debug(this.totalBytes + " bytes in total will be uploaded to OSS");
                                }
                                break;
                            case REQUEST_BYTE_TRANSFER_EVENT:
                                this.bytesWritten += bytes;
                                if (this.totalBytes != -1) {
                                    int percent = (int)(this.bytesWritten * 100.0 / this.totalBytes);
                                    if (log.isDebugEnabled()) {
                                        log.debug(bytes + " bytes have been written at this time, upload progress: " + percent + "%(" + this.bytesWritten + "/" + this.totalBytes + ")");
                                    }
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug(bytes + " bytes have been written at this time, upload ratio: unknown" + "(" + this.bytesWritten + "/...)");
                                    }
                                }
                                listener.progress(bytesWritten);
                                break;
                            case TRANSFER_COMPLETED_EVENT:
                                this.succeed = true;
                                if (log.isDebugEnabled()) {
                                    log.debug("Succeed to upload, " + this.bytesWritten + " bytes have been transferred in total");
                                }
                                listener.finish();
                                break;
                            case TRANSFER_FAILED_EVENT:
                                if (log.isDebugEnabled()) {
                                    log.debug("Failed to upload, " + this.bytesWritten + " bytes have been transferred");
                                }
                                listener.finish();
                                break;
                            default:
                                break;
                        }
                    }
                });
                result = this.client.putObject(withProgressListener);
            } else {
            	result = this.client.putObject(bucketName, fileName, inputStream);
            }
            return result.getETag();
        } finally {
            this.shutdown();
        }
    }

    private void shutdown() {
        this.client.shutdown();
    }
}

