package io.icw.poc.tx;
import io.icw.core.basic.VersionChangeInvoker;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.log.Log;
import io.icw.core.rpc.util.NulsDateUtils;
import io.icw.poc.constant.ConsensusErrorCode;
import io.icw.poc.model.bo.Chain;
import io.icw.poc.utils.manager.ChainManager;

/**
 * @author: tag
 * @date: 2019/09/12
 */
public class ProtocolUpgradeInvoker implements VersionChangeInvoker {

    @Override
    public void process(int chainId) {
        ChainManager chainManager = SpringLiteContext.getBean(ChainManager.class);
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
        }
        try {
            long time = NulsDateUtils.getCurrentTimeSeconds();
            chain.getLogger().info("协议升级完成,升级时间：{}",time);
        }catch (Exception e){
            chain.getLogger().error("协议升级失败");
            chain.getLogger().error(e);
        }
    }
}
