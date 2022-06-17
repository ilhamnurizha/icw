package io.icw.chain.service.tx.v1;

import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.CommonAdvice;
import io.icw.chain.info.CmRuntimeInfo;
import io.icw.chain.model.po.BlockHeight;
import io.icw.chain.rpc.call.RpcService;
import io.icw.chain.service.CacheDataService;
import io.icw.chain.util.LoggerUtil;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;

import java.util.List;

@Component
public class ChainAssetCommitAdvice implements CommonAdvice {
    @Autowired
    private CacheDataService cacheDataService;
    @Autowired
    private RpcService rpcService;
    @Override
    public void begin(int chainId, List<Transaction> txList, BlockHeader blockHeader) {
        try {
            long commitHeight = blockHeader.getHeight();
            /*begin bak datas*/
            BlockHeight dbHeight = cacheDataService.getBlockHeight(chainId);
            cacheDataService.bakBlockTxs(chainId, commitHeight, txList, false);
            /*end bak datas*/
            /*begin bak height*/
            cacheDataService.beginBakBlockHeight(chainId, commitHeight);
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void end(int chainId, List<Transaction> txList, BlockHeader blockHeader) {
        try {
            long commitHeight = blockHeader.getHeight();
            /*begin bak height*/
            cacheDataService.endBakBlockHeight(chainId, commitHeight);
            /*end bak height*/
            rpcService.crossChainRegisterChange(CmRuntimeInfo.getMainIntChainId());
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            throw new RuntimeException(e);
        }

    }
}
