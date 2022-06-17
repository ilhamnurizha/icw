package io.icw.chain.service.tx.v1;

import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.CommonAdvice;
import io.icw.chain.info.CmRuntimeInfo;
import io.icw.chain.rpc.call.RpcService;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;

import java.util.List;
@Component
public class ChainAssetRollbackAdvice implements CommonAdvice {
    @Autowired
    RpcService rpcService;
    @Override
    public void begin(int chainId, List<Transaction> txList, BlockHeader blockHeader) {

    }
    @Override
    public void end(int chainId, List<Transaction> txList, BlockHeader blockHeader) {
        rpcService.crossChainRegisterChange(CmRuntimeInfo.getMainIntChainId());
    }
}
