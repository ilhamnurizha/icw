package io.icw.cmd.client;

import io.icw.base.api.provider.Provider;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.basic.AddressTool;
import io.icw.core.core.config.ConfigurationLoader;
import io.icw.core.log.Log;
import io.icw.core.rpc.info.HostInfo;
import io.icw.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.icw.core.rpc.util.AddressPrefixDatas;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 17:07
 * @Description: 功能描述
 */
public class CmdClientBootstrap {
    public static void main(String[] args) {
        NulsRpcModuleBootstrap.printLogo("/cli-logo");
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        configurationLoader.load();
        Provider.ProviderType providerType = Provider.ProviderType.valueOf(configurationLoader.getValue("providerType"));
        int defaultChainId = Integer.parseInt(configurationLoader.getValue("chainId"));
        ServiceManager.init(defaultChainId,providerType);
        try {
            NulsRpcModuleBootstrap.run("io.icw.cmd.client",args);
            //增加地址工具类初始化
            AddressTool.init(new AddressPrefixDatas());

        }catch (Exception e){
            Log.error("module start fail {}",e.getMessage());
        }
    }
}
