package cn.aaron911.file.core;

import cn.aaron911.file.exception.GlobalFileException;
import org.springframework.web.multipart.MultipartFile;

import cn.aaron911.file.entity.VirtualFile;

import java.io.File;
import java.io.InputStream;


public interface IFileUploader {
	

    /**
     * 上传文件，不带监听器
     *
     * @param inputStream       待上传的文件流
     * @param pathPrefix 文件前缀，可以理解为目录
     * @param fileName       文件名
     */
    VirtualFile upload(InputStream inputStream, String pathPrefix, String fileName) throws GlobalFileException;

    /**
     * 上传文件，不带监听器
     *
     * @param file       待上传的文件
     * @param pathPrefix 文件前缀，可以理解为目录
     */
    VirtualFile upload(File file, String pathPrefix) throws GlobalFileException;

    /**
     * 上传文件，不带监听器
     *
     * @param file       待上传的文件
     */
    VirtualFile upload(MultipartFile file, String pathPrefix) throws GlobalFileException;
	
	

    /**
     * 上传文件，带监听器
     *
     * @param file       待上传的文件流
     * @param pathPrefix 文件前缀，可以理解为目录
     * @param listener
     * @param fileName       文件名
     */
    VirtualFile upload(InputStream file, String pathPrefix, String fileName, IProgressListener listener) throws GlobalFileException;

    /**
     * 上传文件
     *
     * @param file       待上传的文件
     * @param pathPrefix 文件前缀，可以理解为目录
     * @param listener
     */
    VirtualFile upload(File file, String pathPrefix, IProgressListener listener) throws GlobalFileException;

    /**
     * 上传文件
     *
     * @param file       待上传的文件
     * @param pathPrefix 文件前缀，可以理解为目录
     * @param listener
     */
    VirtualFile upload(MultipartFile file, String pathPrefix, IProgressListener listener) throws GlobalFileException;

    /**
     * 删除文件
     *
     * @param filePath   文件路径
     */
    boolean delete(String filePath) throws GlobalFileException;
}
