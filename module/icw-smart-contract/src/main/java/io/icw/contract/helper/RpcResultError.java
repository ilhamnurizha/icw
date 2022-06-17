package io.icw.contract.helper;

/**
 * @author Niels
 */
public class RpcResultError {

    private String code;

    private String message;

    private Object data;

    public RpcResultError() {

    }

    public RpcResultError(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public RpcResultError(RpcErrorCode rpcErrorCode) {
        this.code = rpcErrorCode.getCode();
        this.message = rpcErrorCode.getMessage();
    }

    public RpcResultError(RpcErrorCode rpcErrorCode, Object data) {
        this.code = rpcErrorCode.getCode();
        this.message = rpcErrorCode.getMessage();
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public RpcResultError setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public RpcResultError setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object getData() {
        return data;
    }

    public RpcResultError setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"code\":")
                .append(code);
        sb.append(",\"message\":")
                .append('\"').append(message).append('\"');
        sb.append(",\"entity\":")
                .append('\"').append(data).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
