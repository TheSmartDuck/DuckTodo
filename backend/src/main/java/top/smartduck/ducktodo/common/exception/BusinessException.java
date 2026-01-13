package top.smartduck.ducktodo.common.exception;

import top.smartduck.ducktodo.common.enums.ResultCode;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getDesc());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
