package io.icw.test.cases.transcation.batch;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.account.AccountService;
import io.icw.base.api.provider.account.facade.CreateAccountReq;
import io.icw.base.api.provider.account.facade.GetAccountPrivateKeyByAddressReq;
import io.icw.base.api.provider.account.facade.ImportAccountByPrivateKeyReq;
import io.icw.base.data.NulsHash;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.test.Config;
import io.icw.test.cases.*;
import io.icw.test.cases.transcation.batch.fasttx.FastTransfer;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static io.icw.test.cases.account.BaseAccountCase.PASSWORD;
import static io.icw.test.cases.transcation.batch.BatchCreateAccountCase.TRANSFER_AMOUNT;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 17:54
 * @Description: 功能描述
 */
@Component
@TestCase("debugBatchTransfer")
public class DebugBatchTestCase extends TestCaseChain {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Autowired
    Config config;


    @Autowired
    FastTransfer fastTransfer;

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                BatchCreateAccountCase.class,
                SleepAdapter.$15SEC.class,
//                BatchCreateTransferCase.class,
                SleepAdapter.MAX.class
        };
    }

    @Override
    public String title() {
        return "本地调试批量创建交易";
    }

    @Override
    public Object initParam() {
        Result<String> result = accountService.importAccountByPrivateKey(new ImportAccountByPrivateKeyReq(PASSWORD, config.getTestSeedAccount(),true));
        Result<String> account = accountService.createAccount(new CreateAccountReq(1,PASSWORD));
        try {
            Result<NulsHash> result1 = fastTransfer.transfer(result.getData(),account.getList().get(0), TRANSFER_AMOUNT.multiply(BigInteger.valueOf(5000)),config.getTestSeedAccount(),null);
        } catch (TestFailException e) {
            e.printStackTrace();
        }
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BatchParam param = new BatchParam();
        param.count = 1000L;
        Result<String> priKey = accountService.getAccountPrivateKey(new GetAccountPrivateKeyByAddressReq(PASSWORD,account.getList().get(0)));
        param.formAddressPriKey = priKey.getData();
        return param;
    }
}
