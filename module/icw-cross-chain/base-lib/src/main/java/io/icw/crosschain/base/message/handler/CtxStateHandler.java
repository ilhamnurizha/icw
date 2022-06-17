package io.icw.crosschain.base.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.protocol.MessageProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.crosschain.base.constant.CommandConstant;
import io.icw.crosschain.base.message.CtxStateMessage;
import io.icw.crosschain.base.service.ProtocolService;

/**
 * CtxStateMessage处理类
 * CtxStateMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("CtxStateHandlerV1")
public class CtxStateHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.CTX_STATE_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        CtxStateMessage realMessage = RPCUtil.getInstanceRpcStr(message, CtxStateMessage.class);
        if (message == null) {
            return;
        }
        protocolService.receiveCtxState(chainId, nodeId, realMessage);
    }
}
