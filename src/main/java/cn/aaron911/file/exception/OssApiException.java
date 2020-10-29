package cn.aaron911.file.exception;


public class OssApiException extends GlobalFileException {
	private static final long serialVersionUID = 1L;

	public OssApiException(String message) {
        super(message);
    }

    public OssApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
