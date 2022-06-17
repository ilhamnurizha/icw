package io.icw.crosschain.base.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.protocol.MessageProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.crosschain.base.constant.CommandConstant;
import io.icw.crosschain.base.message.GetOtherCtxMessage;
import io.icw.crosschain.base.service.ProtocolService;

/**
 * GetOtherCtxMessage处理类
 * GetOtherCtxMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("GetOtherCtxHandlerV1")
public class GetOtherCtxHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.GET_OTHER_CTX_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        GetOtherCtxMessage realMessage = RPCUtil.getInstanceRpcStr(message, GetOtherCtxMessage.class);
        if (message == null) {
            return;
        }
        protocolService.getOtherCtx(chainId, nodeId, realMessage);
    }
}
