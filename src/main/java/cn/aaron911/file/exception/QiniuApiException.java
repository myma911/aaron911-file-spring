package cn.aaron911.file.exception;


public class QiniuApiException extends GlobalFileException {
	private static final long serialVersionUID = 1L;

	public QiniuApiException() {
        super();
    }

    public QiniuApiException(String message) {
        super(message);
    }

    public QiniuApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public QiniuApiException(Throwable cause) {
        super(cause);
    }
}
