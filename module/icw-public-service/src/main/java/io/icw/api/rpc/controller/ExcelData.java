package io.icw.api.rpc.controller;

import com.alibaba.excel.annotation.ExcelProperty;

import java.util.Date;

public class ExcelData {
    @ExcelProperty("哈希值")
    private String hash;
    @ExcelProperty("交易时间")
    private Date date;
    @ExcelProperty("交易金额")
    private String values;
    @ExcelProperty("手续费")
    private String fee;
    @ExcelProperty("余额")
    private String balance;
    @ExcelProperty("交易类型")
    private String type;
    @ExcelProperty("转账类型")
    private String transferType;

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
