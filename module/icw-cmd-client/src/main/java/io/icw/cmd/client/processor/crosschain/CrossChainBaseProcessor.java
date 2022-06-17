package io.icw.cmd.client.processor.crosschain;

import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.crosschain.ChainManageProvider;
import io.icw.base.api.provider.crosschain.CrossChainProvider;
import io.icw.cmd.client.config.Config;
import io.icw.cmd.client.processor.CommandGroup;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.core.core.annotation.Autowired;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 14:54
 * @Description: 功能描述
 */
public abstract class CrossChainBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    CrossChainProvider crossChainProvider = ServiceManager.get(CrossChainProvider.class);

    ChainManageProvider chainManageProvider = ServiceManager.get(ChainManageProvider.class);

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Cross_Chain;
    }


}
