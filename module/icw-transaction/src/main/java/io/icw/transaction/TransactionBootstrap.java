/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.icw.transaction;

import io.icw.base.basic.AddressTool;
import io.icw.base.protocol.ModuleHelper;
import io.icw.base.protocol.ProtocolGroupManager;
import io.icw.base.protocol.RegisterHelper;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.crypto.HexUtil;
import io.icw.core.rockdb.service.RocksDBService;
import io.icw.core.rpc.info.HostInfo;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.modulebootstrap.Module;
import io.icw.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.icw.core.rpc.modulebootstrap.RpcModule;
import io.icw.core.rpc.modulebootstrap.RpcModuleState;
import io.icw.core.rpc.util.AddressPrefixDatas;
import io.icw.core.rpc.util.NulsDateUtils;
import io.icw.transaction.constant.TxConfig;
import io.icw.transaction.constant.TxConstant;
import io.icw.transaction.constant.TxDBConstant;
import io.icw.transaction.manager.ChainManager;
import io.icw.transaction.model.bo.Chain;
import io.icw.transaction.utils.DBUtil;
import io.icw.transaction.utils.LoggerUtil;
import io.icw.transaction.utils.TxUtil;

import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author: Charlie
 * @date: 2019/3/4
 */
@Component
public class TransactionBootstrap extends RpcModule {

    @Autowired
    private TxConfig txConfig;
    @Autowired
    private AddressPrefixDatas addressPrefixDatas;
    @Autowired
    private ChainManager chainManager;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsRpcModuleBootstrap.run("io.icw", args);
    }

    @Override
    public void init() {
        try {
            //初始化地址工具
            AddressTool.init(addressPrefixDatas);
            //初始化系统参数
            initSys();
            //初始化数据库配置文件
            initDB();
            chainManager.initChain();
            TxUtil.blackHolePublicKey = HexUtil.decode(txConfig.getBlackHolePublicKey());
            ModuleHelper.init(this);
        } catch (Exception e) {
            LoggerUtil.LOG.error("Transaction init error!");
            LoggerUtil.LOG.error(e);
        }
    }

    @Override
    public boolean doStart() {
        try {
            chainManager.runChain();
            while (!isDependencieReady(ModuleE.NW.abbr)){
                LoggerUtil.LOG.debug("wait depend modules ready");
                Thread.sleep(2000L);
            }
            LoggerUtil.LOG.info("Transaction Ready...");
            return true;
        } catch (Exception e) {
            LoggerUtil.LOG.error("Transaction init error!");
            LoggerUtil.LOG.error(e);
            return false;
        }
    }


    @Override
    public void onDependenciesReady(Module module) {
        // add by pierre at 2019-12-04 增加与智能合约模块的连接标志
        LoggerUtil.LOG.info("module [{}] is connected, version [{}]", module.getName(), module.getVersion());
        if (ModuleE.SC.abbr.equals(module.getName())) {
            txConfig.setCollectedSmartContractModule(true);
        }
        // end code by pierre
        if (ModuleE.NW.abbr.equals(module.getName())) {
            RegisterHelper.registerMsg(ProtocolGroupManager.getOneProtocol());
        }
        if (ModuleE.PU.abbr.equals(module.getName())) {
            chainManager.getChainMap().keySet().forEach(RegisterHelper::registerProtocol);
        }
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        LoggerUtil.LOG.info("Transaction onDependenciesReady");
        NulsDateUtils.getInstance().start();
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module module) {
        // add by pierre at 2019-12-04 增加与智能合约模块的连接标志
        LoggerUtil.LOG.info("module [{}] has lost connection, version [{}]", module.getName(), module.getVersion());
        if (ModuleE.SC.abbr.equals(module.getName())) {
            txConfig.setCollectedSmartContractModule(false);
        }
        // end code by pierre
        if (ModuleE.BL.abbr.equals(module.getName())) {
            for(Chain chain : chainManager.getChainMap().values()) {
                chain.getProcessTxStatus().set(false);
            }
        }
        if (ModuleE.CS.abbr.equals(module.getName())) {
            for(Chain chain : chainManager.getChainMap().values()) {
                chain.getPackaging().set(false);
            }
        }
        return RpcModuleState.Ready;
    }

    @Override
    public Module[] declareDependent() {
        return new Module[]{
                Module.build(ModuleE.NW),
                Module.build(ModuleE.LG),
                Module.build(ModuleE.BL),
                Module.build(ModuleE.AC)
        };
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.TX.abbr, TxConstant.RPC_VERSION);
    }

    @Override
    public Set<String> getRpcCmdPackage() {
        return Set.of(TxConstant.TX_CMD_PATH);
    }

    /**
     * 初始化系统编码
     */
    private void initSys() {
        try {
            System.setProperty(TxConstant.SYS_ALLOW_NULL_ARRAY_ELEMENT, "true");
            System.setProperty(TxConstant.SYS_FILE_ENCODING, UTF_8.name());
        } catch (Exception e) {
            LoggerUtil.LOG.error(e);
        }
    }

    public void initDB() {
        try {
            //数据文件存储地址
            RocksDBService.init(txConfig.getTxDataRoot());
            //模块配置表
            DBUtil.createTable(TxDBConstant.DB_MODULE_CONGIF);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e);
        }
    }


}
