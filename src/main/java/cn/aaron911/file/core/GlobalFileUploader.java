package cn.aaron911.file.core;

import cn.aaron911.file.apiClient.IApiClient;
import cn.aaron911.file.entity.VirtualFile;
import cn.aaron911.file.exception.GlobalFileException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class GlobalFileUploader extends BaseFileUploader implements IFileUploader {

	/**
	 * 不带监听器进度
	 */
	@Override
	public VirtualFile upload(InputStream is, String pathPrefix, String fileName) throws GlobalFileException {
		IApiClient apiClient = this.getApiClient();
		VirtualFile virtualFile = apiClient.uploadFile(is, pathPrefix, fileName);
		return virtualFile;
	}

	/**
	 * 不带监听器进度
	 */
	@Override
	public VirtualFile upload(File file, String pathPrefix) throws GlobalFileException {
		IApiClient IFileUpload = this.getApiClient();
		VirtualFile virtualFile = IFileUpload.uploadFile(file, pathPrefix);
		return virtualFile;
	}

	/**
	 * 不带监听器进度
	 */
	@Override
	public VirtualFile upload(MultipartFile file, String pathPrefix) throws GlobalFileException {
		IApiClient IFileUpload = this.getApiClient();
		VirtualFile virtualFile = IFileUpload.uploadFile(file, pathPrefix);
		return virtualFile;
	}



	/**
	 * 带监听器进度
	 */
	@Override
	public VirtualFile upload(InputStream is, String pathPrefix, String fileName , IProgressListener listener) throws GlobalFileException {
		IApiClient IFileUpload = this.getApiClient();
		VirtualFile virtualFile = IFileUpload.uploadFile(is, pathPrefix, fileName, listener);
		return virtualFile;
	}

	/**
	 * 带监听器进度
	 */
	@Override
	public VirtualFile upload(File file, String pathPrefix , IProgressListener listener) throws GlobalFileException {
		IApiClient IFileUpload = this.getApiClient();
		VirtualFile virtualFile = IFileUpload.uploadFile(file, pathPrefix, listener);
		return virtualFile;
	}

	/**
	 * 带监听器进度
	 */
	@Override
	public VirtualFile upload(MultipartFile multipartFile, String pathPrefix, IProgressListener listener) throws GlobalFileException {
		IApiClient IFileUpload = this.getApiClient();
		VirtualFile virtualFile;
		try {
			virtualFile = IFileUpload.uploadFile(multipartFile.getInputStream(), pathPrefix, multipartFile.getOriginalFilename(), listener);
			return virtualFile;
		} catch (IOException e) {
			throw new GlobalFileException("文件上传失败：" + e.getMessage());
		}
	}


	@Override
	public boolean delete(String filePath) throws GlobalFileException {
		if (StringUtils.isEmpty(filePath)) {
			throw new GlobalFileException("[文件服务]文件删除失败，文件为空！");
		}

		IApiClient IFileUpload = this.getApiClient();
		return IFileUpload.removeFile(filePath);
	}

}
