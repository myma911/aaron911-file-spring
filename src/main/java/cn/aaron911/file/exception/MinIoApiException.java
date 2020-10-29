package cn.aaron911.file.exception;


public class MinIoApiException extends GlobalFileException {
	private static final long serialVersionUID = 1L;

	public MinIoApiException() {
        super();
    }

    public MinIoApiException(String message) {
        super(message);
    }

    public MinIoApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public MinIoApiException(Throwable cause) {
        super(cause);
    }
}
