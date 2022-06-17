/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.icw.cmd.client.processor.account;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.account.facade.GetMultiSignAccountByAddressReq;
import io.icw.base.api.provider.ledger.LedgerProvider;
import io.icw.base.api.provider.ledger.facade.AccountBalanceInfo;
import io.icw.base.api.provider.ledger.facade.GetBalanceReq;
import io.icw.base.data.MultiSigAccount;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandResult;
import io.icw.cmd.client.config.Config;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.Log;
import io.icw.core.parse.JSONUtils;
import io.icw.core.parse.MapUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: zhoulijun
 * 获取多签账户信息
 */
@Component
public class GetMultiSignAccountProcessor extends AccountBaseProcessor implements CommandProcessor {

    LedgerProvider ledgerProvider = ServiceManager.get(LedgerProvider.class);

    @Autowired
    Config config;

    @Override
    public String getCommand() {
        return "getmultisignaccount";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> the multi sign account address - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getmultisignaccount <multi sign address> --get account information";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        checkAddress(config.getChainId(), args[1]);
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        Result<MultiSigAccount> info = accountService.getMultiSignAccount(new GetMultiSignAccountByAddressReq(address));
        if (info.isFailed()) {
            return CommandResult.getFailed(info);
        }
        Result<AccountBalanceInfo> balance = ledgerProvider.getBalance(new GetBalanceReq(config.getAssetsId(),config.getChainId(),address));
        if (balance.isFailed()) {
            return CommandResult.getFailed(balance);
        }
        Map<String,Object> res = new HashMap<>(7);
        Map<String,Object> balanceMap = new HashMap<>(3);
        balanceMap.put("available",config.toBigUnit(balance.getData().getAvailable()));
        balanceMap.put("freeze",config.toBigUnit(balance.getData().getFreeze()));
        balanceMap.put("total",config.toBigUnit(balance.getData().getTotal()));
        res.putAll(MapUtils.beanToMap(info.getData()));
        res.put("balance",balanceMap);
        try {
            return CommandResult.getSuccess(JSONUtils.obj2PrettyJson(res));
        } catch (JsonProcessingException e) {
            Log.error("",e);
            return null;
        }
    }
}
