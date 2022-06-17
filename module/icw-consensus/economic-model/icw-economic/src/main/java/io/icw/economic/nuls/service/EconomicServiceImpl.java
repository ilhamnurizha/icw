package io.icw.economic.nuls.service;

import static io.icw.core.constant.CommonCodeConstanst.PARAMETER_ERROR;
import static io.icw.core.constant.CommonCodeConstanst.SUCCESS;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.icw.base.data.CoinTo;
import io.icw.core.basic.Result;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.economic.base.service.EconomicService;
import io.icw.economic.nuls.constant.NulsEconomicConstant;
import io.icw.economic.nuls.constant.ParamConstant;
import io.icw.economic.nuls.model.bo.AgentInfo;
import io.icw.economic.nuls.model.bo.ConsensusConfigInfo;
import io.icw.economic.nuls.model.bo.RoundInfo;
import io.icw.economic.nuls.util.manager.EconomicManager;

/**
 * 经济模型接口实现类
 * @author tag
 * @date 2019/7/22
 */
@Component
public class EconomicServiceImpl implements EconomicService {

    @Override
    public Result registerConfig(Map<String, Object> params) {
        if (params == null || params.get(ParamConstant.CONSENUS_CONFIG) == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        ConsensusConfigInfo consensusConfig = (ConsensusConfigInfo) params.get(ParamConstant.CONSENUS_CONFIG);
        EconomicManager.configMap.put(consensusConfig.getChainId(), consensusConfig);
        return Result.getSuccess(SUCCESS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result calcReward(Map<String, Object> params) {
        if (params == null || params.get(ParamConstant.CHAIN_ID) == null
                || params.get(ParamConstant.ROUND_INFO) == null || params.get(ParamConstant.AGENT_INFO) == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (int)params.get(ParamConstant.CHAIN_ID);
        if (chainId <= ParamConstant.CHAIN_ID_MIN) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        ConsensusConfigInfo consensusConfigInfo = EconomicManager.configMap.get(chainId);
        RoundInfo roundInfo = (RoundInfo)params.get(ParamConstant.ROUND_INFO);
        AgentInfo agentInfo = (AgentInfo)params.get(ParamConstant.AGENT_INFO);
        Map<String, BigInteger> awardAssetMap = new HashMap<>(NulsEconomicConstant.VALUE_0F_4);
        if(params.get(ParamConstant.AWARD_ASSERT_MAP) != null){
            awardAssetMap = (Map<String, BigInteger> )params.get(ParamConstant.AWARD_ASSERT_MAP);
        }
        try {
            Map<String, Object> result = new HashMap<>(2);
            List<CoinTo> coinToList = EconomicManager.getRewardCoin(agentInfo, roundInfo, consensusConfigInfo, 0, awardAssetMap);
            result.put("coinToList", coinToList);
            return Result.getSuccess(SUCCESS).setData(result);
        }catch (NulsException e){
            return Result.getFailed(e.getErrorCode());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Result calcReward2(Map<String, Object> params) {
        if (params == null || params.get(ParamConstant.CHAIN_ID) == null
                || params.get(ParamConstant.ROUND_INFO) == null || params.get(ParamConstant.AGENT_INFO) == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (int)params.get(ParamConstant.CHAIN_ID);
        if (chainId <= ParamConstant.CHAIN_ID_MIN) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        ConsensusConfigInfo consensusConfigInfo = EconomicManager.configMap.get(chainId);
        RoundInfo roundInfo = (RoundInfo)params.get(ParamConstant.ROUND_INFO);
        List<AgentInfo> agentInfos = (List<AgentInfo>)params.get(ParamConstant.AGENT_INFO);
        Map<String, BigInteger> awardAssetMap = new HashMap<>(NulsEconomicConstant.VALUE_0F_4);
        try {
            Map<String, Object> result = new HashMap<>(2);
            List<CoinTo> coinToList = EconomicManager.getRewardCoin2(agentInfos, roundInfo, consensusConfigInfo, 0, awardAssetMap);
            result.put("coinToList", coinToList);
            return Result.getSuccess(SUCCESS).setData(result);
        }catch (NulsException e){
            return Result.getFailed(e.getErrorCode());
        }
    }
}
