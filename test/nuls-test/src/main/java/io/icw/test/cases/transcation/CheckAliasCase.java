package io.icw.test.cases.transcation;

import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.account.AccountService;
import io.icw.base.api.provider.account.facade.AccountInfo;
import io.icw.base.api.provider.account.facade.GetAccountByAddressReq;
import io.icw.test.cases.Constants;
import io.icw.test.cases.TestFailException;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 14:56
 * @Description: 功能描述
 *
 */
@Component
public class CheckAliasCase extends BaseTranscationCase<String,String> {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Override
    public String title() {
        return "别名是否设置成功";
    }

    @Override
    public String doTest(String address, int depth) throws TestFailException {
        AccountInfo accountInfo = accountService.getAccountByAddress(new GetAccountByAddressReq(address)).getData();
        if(!Constants.getAlias(address).equals(accountInfo.getAlias())){
            throw new TestFailException("账户别名不符合预期");
        }
        return address;
    }
}
