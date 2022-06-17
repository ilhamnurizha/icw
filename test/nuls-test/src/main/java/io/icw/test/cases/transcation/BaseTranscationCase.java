package io.icw.test.cases.transcation;

import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.transaction.TransferService;
import io.icw.test.Config;
import io.icw.test.cases.BaseTestCase;
import io.icw.core.core.annotation.Autowired;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 10:13
 * @Description: 功能描述
 */
public abstract class BaseTranscationCase<T,P> extends BaseTestCase<T,P> {


    protected TransferService transferService = ServiceManager.get(TransferService.class);

    @Autowired protected Config config;

}
