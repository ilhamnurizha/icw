package io.icw.cmd.client.processor.account;

import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.account.AccountService;
import io.icw.cmd.client.config.Config;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.cmd.client.processor.CommandGroup;
import io.icw.core.core.annotation.Autowired;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 17:04
 * @Description:
 */
public abstract class AccountBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Account;
    }

}
