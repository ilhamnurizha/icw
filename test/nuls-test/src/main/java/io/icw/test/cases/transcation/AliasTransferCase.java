package io.icw.test.cases.transcation;

import io.icw.test.cases.SleepAdapter;
import io.icw.test.cases.TestCaseChain;
import io.icw.test.cases.TestCaseIntf;
import io.icw.test.cases.account.SyncAccountInfo;
import io.icw.test.cases.ledger.SyncAccountBalance;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 14:36
 * @Description:
 * 设置别名测试
 * 1.设置别名
 * 2.等待10秒
 * 3.检查设置别名交易是否已确认
 * 4.检查本地别名是否已设置成功
 * 5.检查网络节点账户信息是否一致
 */
@Component
public class AliasTransferCase extends TestCaseChain {

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                SetAliasCase.class,
                SleepAdapter.$30SEC.class,
                SyncTxInfoCase.class,
                GetTranscationFormAddressAdapter.class,
                CheckAliasCase.class,
                SyncAccountInfo.class,
                ReadyBalanceToAddressAdapter.class,
                TransferByAliasCase.class,
                SleepAdapter.$30SEC.class,
                SyncTxInfoCase.class,
                GetTranscationToAddressAdapter.class,
                SyncAccountBalance.class
        };
    }

    @Override
    public String title() {
        return "别名转账";
    }
}
