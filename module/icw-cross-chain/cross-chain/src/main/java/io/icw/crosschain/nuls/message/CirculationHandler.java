package io.icw.crosschain.nuls.message;

import io.icw.base.RPCUtil;
import io.icw.base.protocol.MessageProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.crosschain.base.constant.CommandConstant;
import io.icw.crosschain.base.message.CirculationMessage;
import io.icw.crosschain.nuls.servive.MainNetService;

/**
 * CirculationMessage处理类
 * CirculationMessage Processing Class
 *
 * @author tag
 * 2019/5/20
 */

@Component("CirculationHandlerV1")
public class CirculationHandler implements MessageProcessor {
    @Autowired
    private MainNetService mainNetService;

    @Override
    public String getCmd() {
        return CommandConstant.CIRCULATION_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String message) {
        CirculationMessage realMessage = RPCUtil.getInstanceRpcStr(message, CirculationMessage.class);
        if (message == null) {
            return;
        }
        mainNetService.receiveCirculation(chainId, nodeId, realMessage);
    }
}
