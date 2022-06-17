package io.icw.contract.tx;

import io.icw.base.protocol.ProtocolGroupManager;
import io.icw.contract.config.ContractContext;
import io.icw.contract.manager.ChainManager;
import io.icw.contract.model.bo.Chain;
import io.icw.contract.model.bo.ContractTokenAssetsInfo;
import io.icw.contract.rpc.call.LedgerCall;
import io.icw.contract.rpc.call.TransactionCall;
import io.icw.contract.util.Log;
import io.icw.contract.vm.VMFactory;
import io.icw.core.basic.VersionChangeInvoker;
import io.icw.core.constant.TxType;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.exception.NulsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-12-06
 */
public class SmartContractVersionChangeInvoker implements VersionChangeInvoker {

    private static SmartContractVersionChangeInvoker invoker = new SmartContractVersionChangeInvoker();

    private SmartContractVersionChangeInvoker() {}

    public static SmartContractVersionChangeInvoker instance() {
        return invoker;
    }

    private boolean isloadV8 = false;

    /**
     *
     * 协议升级后，向账本模块请求nrc20-token资产列表，缓存到模块内存中。
     *
     * @param currentChainId
     */
    @Override
    public void process(int currentChainId) {
        ChainManager.chainHandle(currentChainId);
        Short currentVersion = ProtocolGroupManager.getCurrentVersion(currentChainId);
        Log.info("触发协议升级，chainId: [{}], 版本为: [{}]", currentChainId, currentVersion);
        ChainManager chainManager = SpringLiteContext.getBean(ChainManager.class);
        if (currentVersion >= ContractContext.UPDATE_VERSION_CONTRACT_ASSET) {
            this.loadV8(chainManager.getChainMap().get(currentChainId), currentVersion);
        }
        // 向交易模块设置智能合约生成交易类型
        setContractGenerateTxTypes(currentChainId, currentVersion);
        // 缓存token注册资产的资产ID和token合约地址
        Map<Integer, Chain> chainMap = chainManager.getChainMap();
        for (Chain chain : chainMap.values()) {
            int chainId = chain.getChainId();
            Short version = ProtocolGroupManager.getCurrentVersion(chainId);
            if(version < ContractContext.UPDATE_VERSION_V250) {
                continue;
            }
            Log.info("协议升级成功，向账本模块获取token资产列表，chainId: [{}], 版本为: [{}]", chainId, version);
            List<Map> regTokenList;
            try {
                regTokenList = LedgerCall.getRegTokenList(chainId);
                if(regTokenList != null && !regTokenList.isEmpty()) {
                    Map<String, ContractTokenAssetsInfo> tokenAssetsInfoMap = chain.getTokenAssetsInfoMap();
                    Map<String, String> tokenAssetsContractAddressInfoMap = chain.getTokenAssetsContractAddressInfoMap();
                    regTokenList.stream().forEach(map -> {
                        int assetId = Integer.parseInt(map.get("assetId").toString());
                        String tokenContractAddress = map.get("assetOwnerAddress").toString();
                        tokenAssetsInfoMap.put(tokenContractAddress, new ContractTokenAssetsInfo(chainId, assetId));
                        tokenAssetsContractAddressInfoMap.put(chainId + "-" + assetId, tokenContractAddress);
                    });
                }
            } catch (NulsException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setContractGenerateTxTypes(int currentChainId, Short currentVersion) {
        List<Integer> list = List.of(
                TxType.CONTRACT_TRANSFER,
                TxType.CONTRACT_CREATE_AGENT,
                TxType.CONTRACT_DEPOSIT,
                TxType.CONTRACT_CANCEL_DEPOSIT,
                TxType.CONTRACT_STOP_AGENT);
        List<Integer> resultList = new ArrayList<>();
        resultList.addAll(list);
        if(currentVersion >= ContractContext.UPDATE_VERSION_V250) {
            resultList.add(TxType.CONTRACT_TOKEN_CROSS_TRANSFER);
        }
        try {
            TransactionCall.setContractGenerateTxTypes(currentChainId, resultList);
        } catch (NulsException e) {
            Log.warn("获取智能合约生成交易类型异常", e);
        }
    }

    private void loadV8(Chain chain, int currentVersion) {
        if (isloadV8) {
            return;
        }
        chain.clearOldBatchInfo();
        Log.info("版本[{}]协议升级成功，重新初始化智能合约VM", currentVersion);
        VMFactory.reInitVM_v8();
        isloadV8 = true;
    }
}
