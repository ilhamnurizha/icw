package io.icw.provider;

import java.util.Map;

import io.icw.base.api.provider.Provider;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.basic.AddressTool;
import io.icw.core.constant.BaseConstant;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Service;
import io.icw.core.core.config.ConfigurationLoader;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.exception.NulsException;
import io.icw.core.model.StringUtils;
import io.icw.core.parse.I18nUtils;
import io.icw.core.rpc.info.HostInfo;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.modulebootstrap.Module;
import io.icw.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.icw.core.rpc.modulebootstrap.RpcModule;
import io.icw.core.rpc.modulebootstrap.RpcModuleState;
import io.icw.core.rpc.util.AddressPrefixDatas;
import io.icw.provider.api.RpcServerManager;
import io.icw.provider.api.constant.SdkConstant;
import io.icw.v2.NulsSDKBootStrap;
import io.icw.v2.SDKContext;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-10 19:27
 * @Description: 模块启动类
 */
@Service
public class ApiBootstrap extends RpcModule {

    private String moduleName = "nuls-api";

    @Autowired
    MyModule myModule;
    @Autowired
    private AddressPrefixDatas addressPrefixDatas;

    public static void main(String[] args) {
        boolean isOffline = false;
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
            //args = new String[]{"ws://192.168.1.40:7771"};
        } else {
            String arg1 = args[0];
            if (StringUtils.isNotBlank(arg1)) {
                arg1 = arg1.trim().toLowerCase();
            }
            if ("offline".equals(arg1)) {
                isOffline = true;
            }
        }
        String basePackage = "io.icw";
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        configurationLoader.load();
        int defaultChainId = Integer.parseInt(configurationLoader.getValue("chainId"));
        Provider.ProviderType providerType = Provider.ProviderType.RPC;
        ServiceManager.init(defaultChainId, providerType);
        Map<String, ConfigurationLoader.ConfigItem> configItemMap = configurationLoader.getConfigData().get(SdkConstant.SDK_API);
        ConfigurationLoader.ConfigItem offline = configItemMap.get("offline");
        if (offline != null) {
            isOffline = Boolean.parseBoolean(offline.getValue());
        }
        if (!isOffline) {
            NulsRpcModuleBootstrap.run("io.icw", args);
        } else {
            SpringLiteContext.init(basePackage);
        }
        initRpcServer(configItemMap);

        NulsSDKBootStrap.init(defaultChainId, "");
        SDKContext.addressPrefix = BaseConstant.MAINNET_DEFAULT_ADDRESS_PREFIX;
        try {
            I18nUtils.setLanguage("en");
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }

    private static void initRpcServer(Map<String, ConfigurationLoader.ConfigItem> configItemMap) {
        String server_ip = "0.0.0.0";
        int server_port = 18004;
        if (configItemMap != null) {
            ConfigurationLoader.ConfigItem serverIp = configItemMap.get("serverIp");
            if (serverIp != null) {
                server_ip = serverIp.getValue();
            }
            ConfigurationLoader.ConfigItem serverPort = configItemMap.get("serverPort");
            if (serverPort != null) {
                server_port = Integer.parseInt(serverPort.getValue());
            }
        }
        RpcServerManager.getInstance().startServer(server_ip, server_port);
    }


    @Override
    public Module[] declareDependent() {
        return new Module[]{
                new Module(ModuleE.CS.abbr, ROLE),
                new Module(ModuleE.BL.abbr, ROLE),
                new Module(ModuleE.AC.abbr, ROLE),
                new Module(ModuleE.TX.abbr, ROLE),
                new Module(ModuleE.LG.abbr, ROLE),
                new Module(ModuleE.CC.abbr, ROLE),
                new Module(ModuleE.NW.abbr, ROLE)
        };
    }

    @Override
    public Module moduleInfo() {
        return new Module(moduleName, ROLE);
    }

    @Override
    public boolean doStart() {
        AddressTool.init(addressPrefixDatas);
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        return myModule.startModule(moduleName);
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Running;
    }

}
