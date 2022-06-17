package io.icw.test.cases.transcation;

import io.icw.test.cases.TestCase;
import io.icw.test.cases.TestCaseChain;
import io.icw.test.cases.TestCaseIntf;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 20:07
 * @Description: 功能描述
 */
@TestCase("transaction")
@Component
public class TransactionCase extends TestCaseChain {
    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                TransferCase.class,
                AliasTransferCase.class
        };
    }

    @Override
    public String title() {
        return "交易模块";
    }
}
