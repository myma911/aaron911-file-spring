package cn.aaron911.file.apiClient;

import cn.aaron911.file.alioss.api.OssApi;
import cn.aaron911.file.core.IProgressListener;
import cn.aaron911.file.entity.VirtualFile;
import cn.aaron911.file.exception.GlobalFileException;
import cn.aaron911.file.exception.OssApiException;
import cn.aaron911.file.util.FileUtil;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class AliyunOssApiClient extends BaseApiClient {

    private OssApi ossApi;

    /**
     * 阿里云Bucket 域名
     */
    private String url;

    /**
     * 阿里云Bucket 名称
     */
    private String bucketName;
    
    /**
     * 不带进度监听器
     */
    public AliyunOssApiClient() {
        super("阿里云OSS");
    }


    /**
     *
     * @param endpoint 阿里云地域节点
     * @param accessKeyId   阿里云Access Key
     * @param accessKeySecret 阿里云Access Key Secret
     * @param url   阿里云Bucket 域名
     * @param bucketName    阿里云Bucket 名称
     * @return
     */
    public AliyunOssApiClient init(String endpoint, String accessKeyId, String accessKeySecret, String url, String bucketName) {
        ossApi = new OssApi(endpoint, accessKeyId, accessKeySecret);
        this.url = url;
        this.bucketName = bucketName;
        return this;
    }
    
    /**
     * 不带进度监听器
     */
    @Override
    public VirtualFile uploadFile(InputStream is, String pathPrefix, String fileName)  throws GlobalFileException {
        this.check();
        this.createNewFileName(pathPrefix, fileName);
        try {
            Date startTime = new Date();
            ossApi.uploadFile(is, this.newFileName, bucketName);
            return new VirtualFile()
                    .setOriginalFileName(fileName)
                    .setSuffix(this.suffix)
                    .setUploadStartTime(startTime)
                    .setUploadEndTime(new Date())
                    .setFilePath(this.newFileName)
                    .setFullFilePath(this.url + this.newFileName);
        }catch (Exception ex){
            throw new OssApiException("[" + this.storageType + "]文件上传失败：" + ex.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
    }

    /**
     * 带进度监听器
     */
    @Override
    public VirtualFile uploadFile(InputStream is, String pathPrefix, String fileName, IProgressListener listener)  throws GlobalFileException{
        this.check();
        this.createNewFileName(pathPrefix, fileName);
        try {
            Date startTime = new Date();
            ossApi.uploadFile(is, this.newFileName, bucketName, listener);
            return new VirtualFile()
                    .setOriginalFileName(fileName)
                    .setSuffix(this.suffix)
                    .setUploadStartTime(startTime)
                    .setUploadEndTime(new Date())
                    .setFilePath(this.newFileName)
                    .setFullFilePath(this.url + this.newFileName);
        } catch (Exception ex){
            throw new OssApiException("[" + this.storageType + "]文件上传失败：" + ex.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
    }

    @Override
    public VirtualFile uploadFile(File file, String pathPrefix, IProgressListener listener)  throws GlobalFileException{
        return uploadFile(FileUtil.getInputStream(file), pathPrefix, file.getName(), listener);
    }

    @Override
    public boolean removeFile(String key)  throws GlobalFileException{
        this.check();
        if (StringUtils.isEmpty(key)) {
            throw new OssApiException("[" + this.storageType + "]删除文件失败：文件key为空");
        }
        try {
            this.ossApi.deleteFile(key, bucketName);
            return true;
        } catch (Exception e) {
            throw new OssApiException(e.getMessage());
        }
    }

    @Override
    public void check() throws OssApiException {
        if (null == ossApi) {
            throw new OssApiException("[" + this.storageType + "]尚未配置阿里云OSS，文件上传功能暂时不可用！");
        }
    }
}
