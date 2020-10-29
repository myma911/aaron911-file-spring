package cn.aaron911.file.apiClient;

import cn.aaron911.file.core.IProgressListener;
import cn.aaron911.file.entity.VirtualFile;
import cn.aaron911.file.exception.GlobalFileException;
import cn.aaron911.file.exception.LocalApiException;
import cn.aaron911.file.util.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.StreamProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * 本地文件上传
 */
public class LocalApiClient extends BaseApiClient {
    private static final Logger log = LoggerFactory.getLogger(LocalApiClient.class);

    private String url;
    private String rootPath;
    
    public LocalApiClient() {
        super("Nginx文件服务器");
    }

    public LocalApiClient init(String url, String rootPath) {
        this.url = url;
        this.rootPath = rootPath;
        return this;
    }
    
    /**
     * 不带监听器
     */
    @Override
    public VirtualFile uploadFile(InputStream is, String pathPrefix, String fileName)  throws GlobalFileException {
        this.check();

        String key = FileUtil.generateTempFileName(fileName);
        this.createNewFileName(key, pathPrefix);
        String realFilePath = this.rootPath + this.newFileName;
        FileUtil.checkFilePath(realFilePath);
        try (
            FileOutputStream fos = new FileOutputStream(realFilePath)
        ){
            Date startTime = new Date();
            FileCopyUtils.copy(is, fos);
            return new VirtualFile()
                    .setOriginalFileName(FileUtil.getName(key))
                    .setSuffix(this.suffix)
                    .setUploadStartTime(startTime)
                    .setUploadEndTime(new Date())
                    .setFilePath(this.newFileName)
                    .setFullFilePath(this.url + this.newFileName);
        } catch (Exception e) {
            throw new LocalApiException("[" + this.storageType + "]文件上传失败：" + e.getMessage() + fileName);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
    }
    

    /**
     * 带监听器
     */
    @Override
    public VirtualFile uploadFile(InputStream is, String pathPrefix, String fileName, IProgressListener listener)  throws GlobalFileException{
        this.check();
        String key = FileUtil.generateTempFileName(fileName);
        this.createNewFileName(key, pathPrefix);
        String realFilePath = this.rootPath + this.newFileName;
        FileUtil.checkFilePath(realFilePath);
        try (
             FileOutputStream fos = new FileOutputStream(realFilePath)
        ) {
            Date startTime = new Date();
            IoUtil.copy(is, fos, 1024, new StreamProgress() {
                @Override
                public void start() {
                    if (log.isDebugEnabled()) {
                        log.debug("Start to upload......");
                    }
                    listener.start();
                }

                @Override
                public void progress(long progressSize) {
                    if (log.isDebugEnabled()) {
                        log.debug(progressSize + " bytes have been written at this time");
                    }
                    listener.progress(progressSize);
                }

                @Override
                public void finish() {
                    if (log.isDebugEnabled()) {
                        log.debug("Succeed to upload");
                    }
                    listener.finish();
                }
            });
            return new VirtualFile()
                    .setOriginalFileName(FileUtil.getName(key))
                    .setSuffix(this.suffix)
                    .setUploadStartTime(startTime)
                    .setUploadEndTime(new Date())
                    .setFilePath(this.newFileName)
                    .setFullFilePath(this.url + this.newFileName);
        } catch (Exception e) {
            throw new LocalApiException("[" + this.storageType + "]文件上传失败：" + e.getMessage() + fileName);
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
            throw new LocalApiException("[" + this.storageType + "]删除文件失败：文件key为空");
        }
        File file = new File(this.rootPath + key);
        if (!file.exists()) {
            throw new LocalApiException("[" + this.storageType + "]删除文件失败：文件不存在[" + this.rootPath + key + "]");
        }
        try {
            return file.delete();
        } catch (Exception e) {
            throw new LocalApiException("[" + this.storageType + "]删除文件失败：" + e.getMessage());
        }
    }

    @Override
    public void check() throws LocalApiException {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(rootPath)) {
            throw new LocalApiException("[" + this.storageType + "]尚未配置Nginx文件服务器，文件上传功能暂时不可用！");
        }
    }
}
