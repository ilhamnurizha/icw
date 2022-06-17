package io.icw.test.cases.account;

import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.account.AccountService;
import io.icw.test.Config;
import io.icw.test.cases.BaseTestCase;
import io.icw.test.cases.Constants;
import io.icw.core.core.annotation.Autowired;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:35
 * @Description: 功能描述
 */
public abstract class BaseAccountCase<T,P> extends BaseTestCase<T,P> {

    protected AccountService accountService = ServiceManager.get(AccountService.class);

    public static final String PASSWORD = Constants.PASSWORD;

    @Autowired
    protected Config config;

}
