package io.icw.transaction.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.data.NulsHash;
import io.icw.base.protocol.MessageProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.transaction.constant.TxErrorCode;
import io.icw.transaction.manager.ChainManager;
import io.icw.transaction.message.GetTxMessage;
import io.icw.transaction.model.bo.Chain;
import io.icw.transaction.model.po.TransactionConfirmedPO;
import io.icw.transaction.rpc.call.NetworkCall;
import io.icw.transaction.service.TxService;

import static io.icw.transaction.constant.TxCmd.NW_ASK_TX;
import static io.icw.transaction.utils.LoggerUtil.LOG;

/**
 * 接收处理网络中其他节点发送的交易hash来索取完整交易的消息
 */
@Component("GetTxMessageHandlerV1")
public class GetTxMessageHandler implements MessageProcessor {

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxService txService;

    @Override
    public String getCmd() {
        return NW_ASK_TX;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        Chain chain = null;
        try {
            //解析获取完整交易消息
            GetTxMessage message = RPCUtil.getInstanceRpcStr(msgStr, GetTxMessage.class);
            if (message == null) {
                return;
            }
            chain = chainManager.getChain(chainId);
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            NulsHash txHash = message.getTxHash();
            TransactionConfirmedPO tx = txService.getTransaction(chain, txHash);
            if (tx == null) {
                chain.getLogger().debug("recieve [askTx] message from node-{}, chainId:{}, hash:{}", nodeId, chainId, txHash.toHex());
                throw new NulsException(TxErrorCode.TX_NOT_EXIST);
            }
            NetworkCall.sendTxToNode(chain, nodeId, tx.getTx());
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
