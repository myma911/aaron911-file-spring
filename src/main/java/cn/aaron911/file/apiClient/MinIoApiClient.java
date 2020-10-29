package cn.aaron911.file.apiClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import cn.aaron911.file.core.IProgressListener;
import cn.aaron911.file.exception.GlobalFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qiniu.util.StringUtils;
import cn.aaron911.file.alioss.api.MinIoApi;
import cn.aaron911.file.entity.VirtualFile;
import cn.aaron911.file.exception.MinIoApiException;
import cn.aaron911.file.util.FileUtil;


public class MinIoApiClient extends BaseApiClient {
	private static final Logger log = LoggerFactory.getLogger(MinIoApiClient.class);

	private MinIoApi minIoApi;

	private String endpoint;

	private String bucketName;

	public MinIoApiClient() {
		super("MinIo存储");
	}

	public MinIoApiClient init(String endpoint, String accessKey, String secretKey, String bucketName) throws MinIoApiException {
		minIoApi = new MinIoApi(endpoint, accessKey, secretKey);
		this.bucketName = bucketName;
        this.endpoint = endpoint;
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
		try {
			minIoApi.putObject(bucketName, this.newFileName, is);
			return new VirtualFile()
                    .setOriginalFileName(FileUtil.getName(key))
                    .setSuffix(this.suffix)
					.setUploadStartTime(startTime)
                    .setUploadEndTime(new Date())
                    .setFilePath(this.newFileName)
                    .setFullFilePath(this.endpoint + this.newFileName);
		} catch (Exception e) {
			final String errInfo = "[" + this.storageType + "]文件上传失败：" + e.getMessage();
			log.error(errInfo);
			throw new MinIoApiException(errInfo);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {}
			}
		}
	}
	
	/**
	 * 带监听器， 还未实现
	 */
	@Override
	public VirtualFile uploadFile(InputStream is, String imageUrl, String pathPrefix, IProgressListener listener)  throws GlobalFileException{
		log.error("真惭愧，还没有在MinIO中找到带进度上传的相关方法");
		return uploadFile(is, imageUrl, pathPrefix);
	}

	@Override
	public VirtualFile uploadFile(File file, String pathPrefix, IProgressListener listener)  throws GlobalFileException{
		return uploadFile(FileUtil.getInputStream(file), pathPrefix, file.getName(), listener);
	}

	/**
	 * 删除
	 *
	 * @param key 文件名称
	 */
	@Override
	public boolean removeFile(String key)  throws GlobalFileException{
		this.check();
		if (StringUtils.isNullOrEmpty(key)) {
			throw new MinIoApiException("[" + this.storageType + "]删除文件失败：文件key为空");
		}
		try {
			this.minIoApi.removeObject(bucketName, key);
            return true;
		} catch (Exception e) {
			throw new MinIoApiException("[" + this.storageType + "]删除文件发生异常：" + e.getMessage());
		}
	}

	@Override
	public void check() throws MinIoApiException {
		if (null == minIoApi) {
			throw new MinIoApiException("[" + this.storageType + "]尚未配置MinIO，文件上传功能暂时不可用！");
		}
	}
}
