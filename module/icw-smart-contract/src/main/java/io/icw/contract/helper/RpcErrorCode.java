package io.icw.contract.helper;

/**
 * @author Niels
 */
public enum RpcErrorCode {
    // 参数不对
    PARAMS_ERROR("1000", "Parameters is wrong!"),

    // 合约未验证
    CONTRACT_NOT_VALIDATION_ERROR("100", "Contract code not certified!"),

    // 合约已验证
    CONTRACT_VALIDATION_ERROR("101", "The contract code has been certified!"),

    // 合约验证失败
    CONTRACT_VALIDATION_FAILED("102", "Contract verification failed."),

    //数据未找到
    DATA_NOT_EXISTS("404", "Data not found!"),

    //交易解析错误
    TX_PARSE_ERROR("999", "Transaction parse error!"),

    //脚本执行错误
    TX_SHELL_ERROR("755", "Shell execute error!"),

    //系统未知错误
    SYS_UNKNOWN_EXCEPTION("10002", "System unknown error!");

    private String code;

    private String message;

    RpcErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }}
