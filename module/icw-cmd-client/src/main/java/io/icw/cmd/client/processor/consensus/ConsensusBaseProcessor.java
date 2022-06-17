package io.icw.cmd.client.processor.consensus;

import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.consensus.ConsensusProvider;
import io.icw.base.api.provider.consensus.facade.AgentInfo;
import io.icw.cmd.client.CommandHelper;
import io.icw.cmd.client.config.Config;
import io.icw.cmd.client.processor.CommandGroup;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.parse.MapUtils;
import io.icw.core.rpc.util.NulsDateUtils;

import java.math.BigInteger;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 17:07
 * @Description: 功能描述
 */
public abstract class ConsensusBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    ConsensusProvider consensusProvider = ServiceManager.get(ConsensusProvider.class);

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Consensus;
    }

    public Map<String, Object> agentToMap(AgentInfo info) {
        Map<String, Object> map = MapUtils.beanToMap(info);
        map.put("deposit", config.toBigUnit(new BigInteger(info.getDeposit())));
        map.put("totalDeposit", config.toBigUnit(new BigInteger(info.getTotalDeposit())));
        map.put("time", NulsDateUtils.timeStamp2DateStr(info.getTime() * 1000));
        map.put("status", CommandHelper.consensusExplain((Integer) map.get("status")));
        return map;
    }
}
