package io.icw.crosschain.base;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.modulebootstrap.Module;
import io.icw.core.rpc.modulebootstrap.RpcModule;
import io.icw.crosschain.base.constant.CrossChainConstant;

import java.util.*;

/**
 * 跨链模块启动类
 * Cross Chain Module Startup and Initialization Management
 * @author tag
 * 2019/4/10
 */
public abstract class BaseCrossChainBootStrap extends RpcModule {
    private Set<String> rpcPaths = new HashSet<>(){{add(CrossChainConstant.RPC_PATH);}};

    /**
     * 新增需要加入RPC的CMD所在目录
     * Add the directory where the CMD needs to be added to RPC
     * */
    protected void registerRpcPath(String rpcPath){
        rpcPaths.add(rpcPath);
    }


    @Override
    public void init() {
        super.init();
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.CC.name,ROLE);
    }

    /**
     * 指定RpcCmd的包名
     * 可以不实现此方法，若不实现将使用spring init扫描的包
     * @return
     */
    @Override
    public Set<String> getRpcCmdPackage(){
        return rpcPaths;
    }

    protected Set<String> getRpcPaths() {
        return rpcPaths;
    }

    public void setRpcPaths(Set<String> rpcPaths) {
        this.rpcPaths = rpcPaths;
    }
}
