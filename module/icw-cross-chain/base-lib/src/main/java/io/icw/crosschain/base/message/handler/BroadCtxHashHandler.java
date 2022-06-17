package io.icw.crosschain.base.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.protocol.MessageProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.crosschain.base.constant.CommandConstant;
import io.icw.crosschain.base.message.BroadCtxHashMessage;
import io.icw.crosschain.base.service.ProtocolService;

/**
 * BroadCtxHashMessage处理类
 * BroadCtxHashMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("BroadCtxHashHandlerV1")
public class BroadCtxHashHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.BROAD_CTX_HASH_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        BroadCtxHashMessage realMessage = RPCUtil.getInstanceRpcStr(message, BroadCtxHashMessage.class);
        if (message == null) {
            return;
        }
        protocolService.receiveCtxHash(chainId, nodeId, realMessage);
    }
}
