package com.eagleeye.model.vo;

/**
 * 操作结果响应VO
 */
public class OperationResult {

    private boolean success;
    private String message;

    public OperationResult() {
    }

    public OperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static OperationResult success(String message) {
        return new OperationResult(true, message);
    }

    public static OperationResult error(String message) {
        return new OperationResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
