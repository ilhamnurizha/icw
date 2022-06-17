package io.icw.cmd.client.processor.consensus;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.consensus.facade.AgentInfo;
import io.icw.base.api.provider.consensus.facade.GetAgentInfoReq;
import io.icw.base.data.NulsHash;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandResult;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-26 15:50
 * @Description: 功能描述
 */
@Component
public class GetAgentInfoProcessor extends ConsensusBaseProcessor {

    @Override
    public String getCommand() {
        return "getagent";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<agentHash>  the hash of an agent -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getagent <agentHash>  -- get an agent node information According to agent hash";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args, 1);
        checkArgs(NulsHash.validHash(args[1]), "agentHash format error");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String agentHash = args[1];
        Result<AgentInfo> result = consensusProvider.getAgentInfo(new GetAgentInfoReq(agentHash));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        AgentInfo info = result.getData();
        return CommandResult.getResult(new Result(agentToMap(info)));
    }


}

