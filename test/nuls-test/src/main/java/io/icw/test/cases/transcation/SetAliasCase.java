package io.icw.test.cases.transcation;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.account.AccountService;
import io.icw.base.api.provider.account.facade.SetAccountAliasReq;
import io.icw.test.cases.BaseTestCase;
import io.icw.test.cases.Constants;
import io.icw.test.cases.TestFailException;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 14:38
 * @Description: 功能描述
 */
@Component
public class SetAliasCase extends BaseTestCase<String,String> {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Override
    public String title() {
        return "设置别名";
    }

    @Override
    public String doTest(String param, int depth) throws TestFailException {
        Result<String> result = accountService.setAccountAlias(new SetAccountAliasReq(Constants.PASSWORD,param,Constants.getAlias(param)));
        checkResultStatus(result);
        return result.getData();
    }
}
