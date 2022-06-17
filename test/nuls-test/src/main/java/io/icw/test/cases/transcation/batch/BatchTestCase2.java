package io.icw.test.cases.transcation.batch;

import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.test.Config;
import io.icw.test.cases.TestCaseChain;
import io.icw.test.cases.TestCaseIntf;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 17:54
 * @Description: 功能描述
 */
@Component
public class BatchTestCase2 extends TestCaseChain {

    @Autowired
    Config config;

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                BatchReadyNodeAccountCase2.class
        };
    }

    @Override
    public String title() {
        return "本地调试批量创建交易";
    }

    @Override
    public Object initParam() {
        return 10000;
    }
}
