package io.icw;

import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Service;
import io.icw.core.core.annotation.Value;
import io.icw.core.rpc.info.HostInfo;
import io.icw.core.rpc.modulebootstrap.Module;
import io.icw.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.icw.core.rpc.modulebootstrap.RpcModule;
import io.icw.core.rpc.modulebootstrap.RpcModuleState;

@Service
public abstract class NulsModuleBootstrap extends RpcModule {

    @Value("APP_NAME")
    private String moduleName;

    @Autowired
    MyModule myModule;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
//            args = new String[]{"ws://" + "192.168.110.125" + ":7771"};
        }
        
//        int chainId = 1;
//        String prefix = AddressTool.getPrefix(chainId);
//        System.out.println(prefix);
        
        NulsRpcModuleBootstrap.run("io.icw",args);
    }

    @Override
    public Module[] declareDependent() {
        return myModule.declareDependent();
    }

    @Override
    public Module moduleInfo() {
        return new Module(moduleName,ROLE);
    }

    @Override
    public boolean doStart() {
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
