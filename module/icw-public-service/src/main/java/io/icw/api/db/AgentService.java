package io.icw.api.db;

import io.icw.api.model.po.AgentInfo;
import io.icw.api.model.po.PageInfo;

import java.math.BigInteger;
import java.util.List;

public interface AgentService {

     void initCache();

    AgentInfo getAgentByHash(int chainID, String agentHash);

    PageInfo<AgentInfo> getAgentByHashList(int chainID, int pageNumber, int pageSize, List<String> hashList);

    AgentInfo getAgentByPackingAddress(int chainID, String packingAddress);

    AgentInfo getAgentByAgentAddress(int chainID, String agentAddress);

    AgentInfo getAliveAgentByAgentAddress(int chainID, String agentAddress);

    void saveAgentList(int chainID, List<AgentInfo> agentInfoList);

    void rollbackAgentList(int chainId, List<AgentInfo> agentInfoList);

    List<AgentInfo> getAgentList(int chainId, long startHeight);
    
    PageInfo<AgentInfo> getAgentList(int chainId, int type, int pageNumber, int pageSize, String keyword, int order);

    PageInfo<AgentInfo> getAgentList(int chainId, int pageNumber, int pageSize);

    long agentsCount(int chainId, long startHeight);

    BigInteger getConsensusCoinTotal(int chainId);
}
