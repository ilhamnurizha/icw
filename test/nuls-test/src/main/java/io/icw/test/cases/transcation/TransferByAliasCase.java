package io.icw.test.cases.transcation;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.account.AccountService;
import io.icw.base.api.provider.account.facade.AccountInfo;
import io.icw.base.api.provider.account.facade.GetAccountByAddressReq;
import io.icw.base.api.provider.transaction.facade.TransferReq;
import io.icw.test.cases.Constants;
import io.icw.test.cases.TestFailException;
import io.icw.test.cases.account.CreateAccountCase;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.model.StringUtils;

import static io.icw.test.cases.Constants.REMARK;
import static io.icw.test.cases.Constants.TRANSFER_AMOUNT;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 15:00
 * @Description: 功能描述
 */
@Component
public class TransferByAliasCase extends BaseTranscationCase<String,String> {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Autowired
    CreateAccountCase createAccountCase;

    @Override
    public String title() {
        return "通过别名转账到指定地址";
    }

    @Override
    public String doTest(String address, int depth) throws TestFailException {
        String toAddress = createAccountCase.check(null,depth);
        AccountInfo accountInfo = accountService.getAccountByAddress(new GetAccountByAddressReq(address)).getData();
        check(StringUtils.isNotBlank(accountInfo.getAlias()),"别名转账异常，转出账户别名为空");
        TransferReq.TransferReqBuilder builder =
                new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                        .addForm(accountInfo.getAlias(), Constants.PASSWORD, TRANSFER_AMOUNT)
                        .addTo(toAddress, TRANSFER_AMOUNT);
        builder.setRemark(REMARK);
        Result<String> result = transferService.transfer(builder.build(new TransferReq()));
        checkResultStatus(result);
        return result.getData();
    }
}
