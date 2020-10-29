package cn.aaron911.file.apiClient;

import cn.aaron911.file.core.IProgressListener;
import cn.aaron911.file.entity.VirtualFile;
import cn.aaron911.file.exception.GlobalFileException;
import cn.aaron911.file.exception.QiniuApiException;
import cn.aaron911.file.util.FileUtil;
import cn.hutool.json.JSONUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Region;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

/**
 * Qiniu云操作文件的api
 *
 *
 */
public class QiniuApiClient extends BaseApiClient {
    private static final Logger log = LoggerFactory.getLogger(QiniuApiClient.class);

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String path;

    public QiniuApiClient() {
        super("七牛云");
    }

    public QiniuApiClient init(String baseUrl, String accessKey, String secretKey, String bucketName) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucketName;
        this.path = baseUrl;
        return this;
    }

    /**
     * 上传
     *
     * @param is       流
     * @param fileName 路径
     * @return 上传后的路径
     */
    @Override
    public VirtualFile uploadFile(InputStream is, String pathPrefix, String fileName)  throws GlobalFileException {
        this.check();

        String key = FileUtil.generateTempFileName(fileName);
        this.createNewFileName(key, pathPrefix);
        Date startTime = new Date();
        //Zone.zone0:华东
        //Zone.zone1:华北
        //Zone.zone2:华南
        //Zone.zoneNa0:北美
        Configuration cfg = new Configuration(Region.autoRegion());
        UploadManager uploadManager = new UploadManager(cfg);
        try {
            Auth auth = Auth.create(this.accessKey, this.secretKey);
            String upToken = auth.uploadToken(this.bucket);
            Response response = uploadManager.put(is, this.newFileName, upToken, null, null);

            //解析上传成功的结果
            DefaultPutRet putRet = JSONUtil.toBean(response.bodyString(), DefaultPutRet.class);

            return new VirtualFile()
                    .setOriginalFileName(key)
                    .setSuffix(this.suffix)
                    .setUploadStartTime(startTime)
                    .setUploadEndTime(new Date())
                    .setFilePath(putRet.key)
                    .setFullFilePath(this.path + putRet.key);
        } catch (QiniuException ex) {
            throw new QiniuApiException("[" + this.storageType + "]文件上传失败：" + ex.error());
        }
    }
    
    /**
     * 带监听器， 还未实现
     */
    @Override
	public VirtualFile uploadFile(InputStream is, String fileUrl, String pathPrefix, IProgressListener listener)  throws GlobalFileException{
        log.error("不好意思，还没实现，先使用不带进度的上传吧");
        return uploadFile(is, fileUrl, pathPrefix);
	}

    @Override
    public VirtualFile uploadFile(File file, String pathPrefix, IProgressListener listener)  throws GlobalFileException{
        return uploadFile(FileUtil.getInputStream(file), pathPrefix, file.getName(), listener);
    }

    /**
     * 删除七牛空间图片方法
     *
     * @param key 七牛空间中文件名称
     */
    @Override
    public boolean removeFile(String key)  throws GlobalFileException{
        this.check();

        if (StringUtils.isNullOrEmpty(key)) {
            throw new QiniuApiException("[" + this.storageType + "]删除文件失败：文件key为空");
        }
        Auth auth = Auth.create(this.accessKey, this.secretKey);
        Configuration config = new Configuration(Region.autoRegion());
        BucketManager bucketManager = new BucketManager(auth, config);
        try {
            Response re = bucketManager.delete(this.bucket, key);
            return re.isOK();
        } catch (QiniuException e) {
            Response r = e.response;
            throw new QiniuApiException("[" + this.storageType + "]删除文件发生异常：" + r.toString());
        }
    }

    @Override
    public void check() throws QiniuApiException {
        if (StringUtils.isNullOrEmpty(this.accessKey) || StringUtils.isNullOrEmpty(this.secretKey) || StringUtils.isNullOrEmpty(this.bucket)) {
            throw new QiniuApiException("[" + this.storageType + "]尚未配置七牛云，文件上传功能暂时不可用！");
        }
    }
}
