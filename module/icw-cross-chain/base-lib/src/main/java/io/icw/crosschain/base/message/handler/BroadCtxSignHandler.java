package io.icw.crosschain.base.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.protocol.MessageProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.crosschain.base.constant.CommandConstant;
import io.icw.crosschain.base.message.BroadCtxSignMessage;
import io.icw.crosschain.base.service.ProtocolService;

/**
 * BroadCtxSignMessage处理类
 * BroadCtxSignMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("BroadCtxSignHandlerV1")
public class BroadCtxSignHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.BROAD_CTX_SIGN_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        BroadCtxSignMessage realMessage = RPCUtil.getInstanceRpcStr(message, BroadCtxSignMessage.class);
        if (message == null) {
            return;
        }
        protocolService.receiveCtxSign(chainId, nodeId, realMessage);
    }
}
