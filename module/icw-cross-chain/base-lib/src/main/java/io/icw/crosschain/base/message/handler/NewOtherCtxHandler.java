package io.icw.crosschain.base.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.protocol.MessageProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.crosschain.base.constant.CommandConstant;
import io.icw.crosschain.base.message.NewOtherCtxMessage;
import io.icw.crosschain.base.service.ProtocolService;

/**
 * NewOtherCtxMessage处理类
 * NewOtherCtxMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("NewOtherCtxHandlerV1")
public class NewOtherCtxHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.NEW_OTHER_CTX_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        NewOtherCtxMessage realMessage = RPCUtil.getInstanceRpcStr(message, NewOtherCtxMessage.class);
        if (message == null) {
            return;
        }
        protocolService.receiveOtherCtx(chainId, nodeId, realMessage);
    }
}
