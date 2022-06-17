package io.icw;

import java.io.File;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.modulebootstrap.Module;
import io.icw.core.rpc.modulebootstrap.RpcModuleState;
import io.icw.core.thread.ThreadUtils;
import io.icw.core.thread.commom.NulsThreadFactory;
import io.icw.rpc.TransactionTools;
import io.icw.thread.PowProcessTask;
import io.icw.thread.process.PowProcess;

@Component
public class MyModule {

    @Autowired
    Config config;

    @Autowired
    TransactionTools transactionTools;

    public RpcModuleState startModule(String moduleName){
        //初始化数据存储文件夹
        File file = new File(config.getDataPath());
        if(!file.exists()){
            file.mkdir();
        }
        //注册交易
        transactionTools.registerTx(moduleName,Constant.TX_TYPE_POW);
        
        int chainId = config.getChainId();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadUtils.createScheduledThreadPool(2,new NulsThreadFactory("pow"+chainId));

        int stop = config.getStop();
        System.out.println("stop: " + stop);
        System.out.println("start PowProcess----------------------");
        if (stop != 1) {
        	System.out.println("start PowProcess----------------------");
        	PowProcess powProcess = new PowProcess();
        	scheduledThreadPoolExecutor.scheduleAtFixedRate(new PowProcessTask(chainId,powProcess),1000L,5000L,TimeUnit.MILLISECONDS);
        }
        
        return RpcModuleState.Running;
    }

    public Module[] declareDependent() {
        return new Module[]{
                Module.build(ModuleE.AC),
                Module.build(ModuleE.LG),
                Module.build(ModuleE.TX),
                Module.build(ModuleE.CS),
                Module.build(ModuleE.BL),
                Module.build(ModuleE.NW)
        };
    }

}
