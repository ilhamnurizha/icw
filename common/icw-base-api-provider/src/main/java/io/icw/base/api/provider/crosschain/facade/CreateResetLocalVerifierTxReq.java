package io.icw.base.api.provider.crosschain.facade;

import io.icw.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 16:56
 * @Description: 功能描述
 */
public class CreateResetLocalVerifierTxReq extends BaseReq {

    String address;

    String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public CreateResetLocalVerifierTxReq(String address, String password) {
        this.address = address;
        this.password = password;
    }
}
