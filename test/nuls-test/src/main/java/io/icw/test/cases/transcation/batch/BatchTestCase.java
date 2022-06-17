package io.icw.test.cases.transcation.batch;

import io.icw.test.Config;
import io.icw.test.cases.TestCase;
import io.icw.test.cases.TestCaseChain;
import io.icw.test.cases.TestCaseIntf;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 17:54
 * @Description: 功能描述
 */
@Component
@TestCase("batchTransfer")
public class BatchTestCase extends TestCaseChain {

    @Autowired
    Config config;

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                BatchReadyNodeAccountCase.class
        };
    }

    @Override
    public String title() {
        return "本地调试批量创建交易";
    }

    @Override
    public Object initParam() {
        int batchTxTotal = (int) config.getBatchTxTotal().longValue();
        return batchTxTotal;
    }
}
