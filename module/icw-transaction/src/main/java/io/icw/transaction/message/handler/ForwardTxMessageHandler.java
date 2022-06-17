package io.icw.transaction.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.data.NulsHash;
import io.icw.base.protocol.MessageProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.transaction.manager.ChainManager;
import io.icw.transaction.message.ForwardTxMessage;
import io.icw.transaction.message.GetTxMessage;
import io.icw.transaction.model.bo.Chain;
import io.icw.transaction.rpc.call.NetworkCall;
import io.icw.transaction.service.TxService;
import io.icw.transaction.utils.TxDuplicateRemoval;

import static io.icw.transaction.constant.TxCmd.NW_ASK_TX;
import static io.icw.transaction.constant.TxCmd.NW_NEW_HASH;
import static io.icw.transaction.utils.LoggerUtil.LOG;

/**
 * 接收处理网络中其他节点转发的交易hash的消息
 */
@Component("ForwardTxMessageHandlerV1")
public class ForwardTxMessageHandler implements MessageProcessor {

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxService txService;

    @Override
    public String getCmd() {
        return NW_NEW_HASH;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        Chain chain = null;
        try {
            chain = chainManager.getChain(chainId);
            //根据区块同步状态,决定是否开始处理交易hash
            if(!chain.getProcessTxStatus().get()){
                return;
            }
            //解析广播交易hash消息
            ForwardTxMessage message = RPCUtil.getInstanceRpcStr(msgStr, ForwardTxMessage.class);
            if (message == null) {
                return;
            }
            NulsHash hash = message.getTxHash();
//            chain.getLoggerMap().get(TxConstant.LOG_TX_MESSAGE).debug(
//                    "recieve [newHash] message from node-{}, chainId:{}, hash:{}", nodeId, chainId, hash.toHex());
            //只判断是否存在
            String hashHex = hash.toHex();
            if (TxDuplicateRemoval.exist(hashHex)) {
                TxDuplicateRemoval.putExcludeNode(hashHex, nodeId);
                return;
            }
            //去该节点查询完整交易
            GetTxMessage getTxMessage = new GetTxMessage();
            getTxMessage.setTxHash(hash);
            NetworkCall.sendToNode(chain, getTxMessage, nodeId, NW_ASK_TX);
        } catch (Exception e) {
            errorLogProcess(chain, e);
        }
    }

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }
}
