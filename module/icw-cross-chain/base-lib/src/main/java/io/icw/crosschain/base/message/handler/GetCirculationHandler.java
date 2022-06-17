package io.icw.crosschain.base.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.protocol.MessageProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.crosschain.base.constant.CommandConstant;
import io.icw.crosschain.base.message.GetCirculationMessage;
import io.icw.crosschain.base.service.ProtocolService;

/**
 * GetCirculationMessage处理类
 * GetCirculationMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("GetCirculationHandlerV1")
public class GetCirculationHandler implements MessageProcessor {
    @Autowired
    private ProtocolService protocolService;

    @Override
    public String getCmd() {
        return CommandConstant.GET_CIRCULLAT_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        GetCirculationMessage realMessage = RPCUtil.getInstanceRpcStr(message, GetCirculationMessage.class);
        if (message == null) {
            return;
        }
        protocolService.getCirculation(chainId, nodeId, realMessage);
    }
}
