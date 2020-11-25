package cn.aaron911.file.apiClient;

import cn.aaron911.file.entity.VirtualFile;
import cn.aaron911.file.exception.*;
import cn.aaron911.file.util.FileUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;


public abstract class BaseApiClient implements IApiClient {

    protected String storageType;
    protected String newFileName;
    protected String suffix;

    

    public BaseApiClient(String storageType) {
        this.storageType = storageType; 
    }


    @Override
    public VirtualFile uploadFile(MultipartFile multipartFile, String pathPrefix) throws GlobalFileException{
        this.check();
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new OssApiException("[" + this.storageType + "]文件上传失败：文件不可为空");
        }
        try {
            VirtualFile res = this.uploadFile(multipartFile.getInputStream(), pathPrefix, multipartFile.getOriginalFilename());
            return res.setSize(multipartFile.getSize()).setOriginalFileName(multipartFile.getOriginalFilename());
        } catch (IOException e) {
            throw new GlobalFileException("[" + this.storageType + "]文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public VirtualFile uploadFile(File file, String pathPrefix) throws GlobalFileException{
        this.check();
        if (file == null || !file.exists()) {
            throw new OssApiException("[" + this.storageType + "]文件上传失败：文件不可为空");
        }
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            int available = is.available();
            VirtualFile res = this.uploadFile(is, pathPrefix,"temp" + FileUtil.getSuffix(file));
            return res.setSize(available).setOriginalFileName(file.getName());
        } catch (IOException e) {
            throw new GlobalFileException("[" + this.storageType + "]文件上传失败：" + e.getMessage());
        }
    }

    protected void createNewFileName(String pathPrefix, String fileName) {
        if(!pathPrefix.endsWith("/")){
            pathPrefix = pathPrefix + "/";
        }
        this.suffix = FileUtil.getSuffix(fileName);
        String newfileName = DateUtil.format(new Date(), "yyyyMMddHHmmssSSS") + "_" + IdUtil.fastSimpleUUID();
        this.newFileName = pathPrefix + (newfileName + this.suffix);
    }

    protected abstract void check() throws OssApiException, MinIoApiException, LocalApiException, QiniuApiException;
}
