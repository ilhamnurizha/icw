package io.icw.sdk.consensus.service;


import io.icw.sdk.accountledger.model.Input;
import io.icw.sdk.accountledger.model.Output;
import io.icw.sdk.consensus.model.DepositInfo;
import io.icw.sdk.consensus.model.AgentInfo;
import io.icw.sdk.core.model.Na;
import io.icw.sdk.core.model.Result;

import java.util.List;

public interface ConsensusService {

    Result createAgentTransaction(AgentInfo agent, List<Input> inputs, Na fee);

    Result createDepositTransaction(DepositInfo info, List<Input> inputs, Na fee);

    Result createCancelDepositTransaction(Output output);

    Result createStopAgentTransaction(Output output);

    Result getDeposits(String address, int pageNumber, int pageSize);

    Result getAgentDeposits(String agentHash, int pageNumber, int pageSize);

    Result createMSAgentTransaction(AgentInfo agentInfo, List<Input> inputs, Na fee);

    Result createStopMSAgentTransaction(Output output);

    Result createMSAccountDepositTransaction(DepositInfo info, List<Input> inputs, Na fee);

    Result createMSAccountCancelDepositTransaction(Output output);
}
