package io.icw.test.cases.ledger;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.ledger.LedgerProvider;
import io.icw.base.api.provider.ledger.facade.AccountBalanceInfo;
import io.icw.base.api.provider.ledger.facade.GetBalanceReq;
import io.icw.test.Config;
import io.icw.test.cases.BaseTestCase;
import io.icw.test.cases.TestFailException;
import io.icw.test.cases.account.ImportAccountByPriKeyCase;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 11:25
 * @Description: 功能描述
 */
@Component
public class GetAccountBalanceByPriKeyCase extends BaseTestCase<AccountBalanceInfo,String> {

    LedgerProvider ledgerProvider = ServiceManager.get(LedgerProvider.class);

    @Autowired
    Config config;

    @Autowired
    ImportAccountByPriKeyCase importAccountByPriKeyCase;


    @Override
    public String title() {
        return "通过私钥查询余额";
    }

    @Override
    public AccountBalanceInfo doTest(String priKey, int depth) throws TestFailException {
        String address = importAccountByPriKeyCase.check(priKey,depth);
        Result<AccountBalanceInfo> result = ledgerProvider.getBalance(new GetBalanceReq(config.getAssetsId(),config.getChainId(),address));
        checkResultStatus(result);
        return result.getData();
    }
}
