package cn.aaron911.file.apiClient;

import cn.aaron911.file.core.IProgressListener;
import cn.aaron911.file.entity.VirtualFile;
import cn.aaron911.file.exception.GlobalFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;


public interface IApiClient {

    VirtualFile uploadFile(MultipartFile file, String pathPrefix) throws GlobalFileException;

    VirtualFile uploadFile(File file, String pathPrefix) throws GlobalFileException;

    VirtualFile uploadFile(InputStream is, String pathPrefix, String fileName) throws GlobalFileException;
    
    VirtualFile uploadFile(InputStream is, String pathPrefix, String fileName, IProgressListener listener) throws GlobalFileException;

    VirtualFile uploadFile(File file, String pathPrefix, IProgressListener listener) throws GlobalFileException;

    boolean removeFile(String key) throws GlobalFileException;
}
