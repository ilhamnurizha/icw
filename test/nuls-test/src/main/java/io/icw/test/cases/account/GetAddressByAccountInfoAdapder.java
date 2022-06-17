package io.icw.test.cases.account;

import io.icw.base.api.provider.account.facade.AccountInfo;
import io.icw.test.cases.BaseAdapter;
import io.icw.test.cases.CaseType;
import io.icw.test.cases.TestFailException;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-22 09:46
 * @Description: 功能描述
 */
@Component
public class GetAddressByAccountInfoAdapder extends BaseAdapter<String, AccountInfo> {

    @Override
    public String title() {
        return "从account中提取address";
    }

    @Override
    public String doTest(AccountInfo param, int depth) throws TestFailException {
        return param.getAddress();
    }

    @Override
    public CaseType caseType() {
        return CaseType.Adapter;
    }

}
