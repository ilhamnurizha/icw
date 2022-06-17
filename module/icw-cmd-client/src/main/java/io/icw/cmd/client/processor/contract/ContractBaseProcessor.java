package io.icw.cmd.client.processor.contract;

import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.contract.ContractProvider;
import io.icw.cmd.client.CommandHelper;
import io.icw.cmd.client.config.Config;
import io.icw.cmd.client.processor.CommandGroup;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.core.core.annotation.Autowired;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 14:54
 * @Description: 功能描述
 */
public abstract class ContractBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    ContractProvider contractProvider = ServiceManager.get(ContractProvider.class);

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Smart_Contract;
    }

    public Object[] getContractCallArgsJson() {
        Object[] argsObj;
        // 再次交互输入构造参数
        String argsJson = CommandHelper.getArgsJson();
        argsObj = CommandHelper.parseArgsJson(argsJson);
        return argsObj;
    }


}
