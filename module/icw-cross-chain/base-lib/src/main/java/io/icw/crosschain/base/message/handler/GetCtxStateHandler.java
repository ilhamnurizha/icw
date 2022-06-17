package io.icw.crosschain.base.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.protocol.MessageProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.crosschain.base.constant.CommandConstant;
import io.icw.crosschain.base.message.GetCtxStateMessage;
import io.icw.crosschain.base.service.ProtocolService;

/**
 * GetCtxStateMessage处理类
 * GetCtxStateMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("GetCtxStateHandlerV1")
public class GetCtxStateHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.GET_CTX_STATE_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        GetCtxStateMessage realMessage = RPCUtil.getInstanceRpcStr(message, GetCtxStateMessage.class);
        if (message == null) {
            return;
        }
        protocolService.getCtxState(chainId, nodeId, realMessage);
    }
}
