package io.icw.cmd.client.processor.crosschain;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.block.BlockService;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.icw.base.api.provider.crosschain.facade.RehandleCtxReq;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandResult;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 13:47
 * @Description: 功能描述
 */
@Component
public class RehandleCrossTxProcessor extends CrossChainBaseProcessor {

    BlockService blockService = ServiceManager.get(BlockService.class);

    @Override
    public String getCommand() {
        return "rehandlectx";
    }

    @Override
    public String getHelp() {
        return new CommandBuilder()
                .newLine(getCommandDescription())
                .newLine("\t<txHash>  tx hash - require")
                .toString();
    }

    @Override
    public String getCommandDescription() {
        return getCommand() + " <txHash> ";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String txHash = args[1];
        long blockHeight = blockService.getBlockHeaderByLastHeight(new GetBlockHeaderByLastHeightReq()).getData().getHeight();
        Result<String> result = crossChainProvider.rehandleCtx(new RehandleCtxReq(txHash,blockHeight));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }
}
