package io.icw.api.db.mongo;

import static io.icw.api.constant.DBTableConstant.AGENT_TABLE;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.alibaba.excel.util.StringUtils;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.WriteModel;

import io.icw.api.ApiContext;
import io.icw.api.cache.ApiCache;
import io.icw.api.db.AgentService;
import io.icw.api.db.AliasService;
import io.icw.api.manager.CacheManager;
import io.icw.api.model.po.AgentInfo;
import io.icw.api.model.po.AliasInfo;
import io.icw.api.model.po.PageInfo;
import io.icw.api.model.po.PocRoundItem;
import io.icw.api.utils.DocumentTransferTool;
import io.icw.api.utils.LoggerUtil;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;

@Component
public class MongoAgentServiceImpl implements AgentService {

    @Autowired
    private MongoDBService mongoDBService;
    @Autowired
    private MongoAliasServiceImpl mongoAliasServiceImpl;

    @Autowired
    private AliasService aliasService;
    
    public void initCache() {
        for (ApiCache apiCache : CacheManager.getApiCaches().values()) {
            List<Document> documentList = mongoDBService.query(AGENT_TABLE + apiCache.getChainInfo().getChainId());
            for (Document document : documentList) {
                AgentInfo agentInfo = DocumentTransferTool.toInfo(document, "txHash", AgentInfo.class);
                apiCache.addAgentInfo(agentInfo);
            }
        }
    }

    public AgentInfo getAgentByHash(int chainID, String agentHash) {
        AgentInfo agentInfo = CacheManager.getCache(chainID).getAgentInfo(agentHash);
        if (agentInfo == null) {
            Document document = mongoDBService.findOne(AGENT_TABLE + chainID, Filters.eq("_id", agentHash));
            agentInfo = DocumentTransferTool.toInfo(document, "txHash", AgentInfo.class);
            CacheManager.getCache(chainID).addAgentInfo(agentInfo);
        }
        return agentInfo.copy();
    }

    @Override
    public PageInfo<AgentInfo> getAgentByHashList(int chainID, int pageNumber, int pageSize, List<String> hashList) {
        PageInfo<AgentInfo> page = new PageInfo<>(pageNumber, pageSize);
        page.setTotalCount(hashList.size());
        int start = (pageNumber - 1) * pageSize;
        if (hashList.size() < start) {
            return page;
        }
        int end = pageNumber * pageSize;
        if (end > hashList.size()) {
            end = hashList.size();
        }
        hashList = hashList.subList(start, end);
        List<AgentInfo> agentInfoList = new ArrayList<>();
        for (String agentHash : hashList) {
            agentInfoList.add(getAgentByHash(chainID, agentHash));
        }
        page.setList(agentInfoList);
        return page;
    }

    public AgentInfo getAgentByPackingAddress(int chainID, String packingAddress) {
        Collection<AgentInfo> agentInfos = CacheManager.getCache(chainID).getAgentMap().values();
        AgentInfo agentInfo = null;
        for (AgentInfo agent : agentInfos) {
            if (!agent.getPackingAddress().equals(packingAddress)) {
                continue;
            }
            if (null == agentInfo || agent.getCreateTime() > agentInfo.getCreateTime()) {
                agentInfo = agent;
            }
        }
        if (agentInfo == null) {
            return null;
        }
        return agentInfo.copy();
    }

    public AgentInfo getAgentByAgentAddress(int chainID, String agentAddress) {
        Collection<AgentInfo> agentInfos = CacheManager.getCache(chainID).getAgentMap().values();
        AgentInfo agentInfo = null;
        for (AgentInfo agent : agentInfos) {
            if (!agentAddress.equals(agent.getAgentAddress())) {
                continue;
            }
            if (null == agentInfo || agent.getCreateTime() > agentInfo.getCreateTime()) {
                agentInfo = agent;
            }
        }
        if (agentInfo == null) {
            return null;
        }
        return agentInfo.copy();
    }

    @Override
    public AgentInfo getAliveAgentByAgentAddress(int chainID, String agentAddress) {
        Collection<AgentInfo> agentInfos = CacheManager.getCache(chainID).getAgentMap().values();
        AgentInfo agentInfo = null;
        for (AgentInfo agent : agentInfos) {
            if (!agentAddress.equals(agent.getAgentAddress())) {
                continue;
            }
            if (agent.getStatus() == 2) {
                continue;
            }
            if (null == agentInfo || agent.getCreateTime() > agentInfo.getCreateTime()) {
                agentInfo = agent;
            }
        }
        if (agentInfo == null) {
            return null;
        }
        return agentInfo.copy();
    }

    public void saveAgentList(int chainID, List<AgentInfo> agentInfoList) {
        if (agentInfoList.isEmpty()) {
            return;
        }
        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (AgentInfo agentInfo : agentInfoList) {
            Document document = DocumentTransferTool.toDocument(agentInfo, "txHash");

            if (agentInfo.isNew()) {
                modelList.add(new InsertOneModel(document));
                agentInfo.setNew(false);
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", agentInfo.getTxHash()), document));
            }
        }
        BulkWriteOptions options = new BulkWriteOptions();
        options.ordered(false);
        mongoDBService.bulkWrite(AGENT_TABLE + chainID, modelList, options);
        ApiCache cache = CacheManager.getCache(chainID);
        for (AgentInfo agentInfo : agentInfoList) {
            cache.addAgentInfo(agentInfo);
        }
    }

    public void rollbackAgentList(int chainId, List<AgentInfo> agentInfoList) {
        initCache();
        if (agentInfoList.isEmpty()) {
            return;
        }
        ApiCache apiCache = CacheManager.getCache(chainId);
        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (AgentInfo agentInfo : agentInfoList) {
            if (agentInfo.isNew()) {
                modelList.add(new DeleteOneModel(Filters.eq("_id", agentInfo.getTxHash())));
                apiCache.getAgentMap().remove(agentInfo.getTxHash());
            } else {
                Document document = DocumentTransferTool.toDocument(agentInfo, "txHash");
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", agentInfo.getTxHash()), document));
                apiCache.addAgentInfo(agentInfo);
            }
        }
        BulkWriteOptions options = new BulkWriteOptions();
        options.ordered(false);
        mongoDBService.bulkWrite(AGENT_TABLE + chainId, modelList, options);
    }

    public List<AgentInfo> getAgentList(int chainId, long startHeight) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        Collection<AgentInfo> agentInfos = apiCache.getAgentMap().values();
        List<AgentInfo> resultList = new ArrayList<>();
        for (AgentInfo agent : agentInfos) {
            if (agent.getDeleteHash() != null && agent.getDeleteHeight() <= startHeight) {
                continue;
            }
            if (agent.getBlockHeight() > startHeight) {
                continue;
            }
            resultList.add(agent);
        }

//        Bson bson = Filters.and(Filters.lte("blockHeight", startHeight), Filters.or(Filters.eq("deleteHeight", 0), Filters.gt("deleteHeight", startHeight)));
//
//        List<Document> list = this.mongoDBService.query(MongoTableName.AGENT_INFO, bson);

//        for (Document document : list) {
//            AgentInfo agentInfo = DocumentTransferTool.toInfo(document, "txHash", AgentInfo.class);
//            AliasInfo alias = mongoAliasServiceImpl.getAliasByAddress(agentInfo.getAgentAddress());
//            if (alias != null) {
//                agentInfo.setAgentAlias(alias.getAlias());
//            }
//            resultList.add(agentInfo);
//        }

        return resultList;
    }

    public PageInfo<AgentInfo> getAgentList(int chainId, int type, int pageNumber, int pageSize, String keyword, int order) {
        Bson filter = null;
        Bson deleteFilter = Filters.eq("deleteHeight", 0);
        if (type == 1) {
            List list = new ArrayList<>(ApiContext.DEVELOPER_NODE_ADDRESS);
            list.addAll(ApiContext.AMBASSADOR_NODE_ADDRESS);
            filter = Filters.and(Filters.nin("agentAddress", list.toArray()), deleteFilter);
        } else if (type == 2) {
            filter = Filters.and(Filters.in("agentAddress", ApiContext.DEVELOPER_NODE_ADDRESS.toArray()), deleteFilter);
        } else if (type == 3) {
            filter = Filters.and(Filters.in("agentAddress", ApiContext.AMBASSADOR_NODE_ADDRESS.toArray()), deleteFilter);
        } else {
            filter = deleteFilter;
        }
        
        if (StringUtils.isNotBlank(keyword)) {
        	AliasInfo aliasInfo = aliasService.getByAlias(chainId, keyword);
        	if (aliasInfo != null) {
        		keyword = ".*" + keyword +".*";
	        	filter = Filters.and(deleteFilter,
	        				Filters.or(
	        				Filters.regex("agentId", keyword), 
	        				Filters.regex("agentAlias", keyword),
	        				Filters.regex("agentId", keyword.toLowerCase()), 
	        				Filters.regex("agentAlias", keyword.toLowerCase()),
	        				Filters.regex("agentAddress", aliasInfo.getAddress()), 
	        				Filters.regex("agentAddress", keyword), 
	        				Filters.regex("packingAddress", keyword),
	        				Filters.regex("rewardAddress", keyword)
	        			));
        	} else {
	        	keyword = ".*" + keyword +".*";
	        	filter = Filters.and(deleteFilter,
	        				Filters.or(
	        				Filters.regex("agentId", keyword), 
	        				Filters.regex("agentAlias", keyword),
	        				Filters.regex("agentId", keyword.toLowerCase()), 
	        				Filters.regex("agentAlias", keyword.toLowerCase()),
	        				Filters.regex("agentAddress", keyword), 
	        				Filters.regex("packingAddress", keyword),
	        				Filters.regex("rewardAddress", keyword)
	        			));
        	}
        }
        Long start = System.currentTimeMillis();
        
        long totalCount = this.mongoDBService.getCount(AGENT_TABLE + chainId, filter);
        LoggerUtil.commonLog.info(String.valueOf(System.currentTimeMillis() - start));
        
        Bson sorts = Sorts.descending("createTime");
        
        List<Document> docsList = null;
        List<PocRoundItem> itemList = null;
        if (order == 0 || order == 1) {
        	ApiCache apiCache = CacheManager.getCache(chainId);
        	itemList = apiCache.getCurrentRound().getItemList();
        	List<String> packingAddress = new ArrayList<String>();
        	for (PocRoundItem item : itemList) {
        		packingAddress.add(item.getPackingAddress());
        	}
        	if (itemList.size() > 15) {
        		docsList = this.mongoDBService.pageQuery(AGENT_TABLE + chainId, filter, sorts, pageNumber, pageSize);
        	} else {
	        	if (!packingAddress.isEmpty()) {
	        		if (pageNumber == 1) {
	        			Bson filterIn = Filters.and(filter, Filters.in("packingAddress", packingAddress));
	        			docsList = this.mongoDBService.pageQuery(AGENT_TABLE + chainId, filterIn, sorts, pageNumber, pageSize);
	        			Bson filterNin = Filters.and(filter, Filters.nin("packingAddress", packingAddress));
	        			List<Document> docsList2 = this.mongoDBService.pageQuery(AGENT_TABLE + chainId, filterNin, sorts, pageNumber, pageSize - itemList.size());
	        			docsList.addAll(docsList2);
	        		} else {
	        			Bson filterNin = Filters.and(filter, Filters.nin("packingAddress", packingAddress));
	        			docsList = this.mongoDBService.pageQuery(AGENT_TABLE + chainId, filterNin, sorts, pageNumber, pageSize, itemList.size());
	        		}
	        	}
        	}
        } else if (order == 2) {
        	sorts = Sorts.descending("commissionRate");
        	docsList = this.mongoDBService.pageQuery(AGENT_TABLE + chainId, filter, sorts, pageNumber, pageSize);
        } else if (order == 3) {
        	sorts = Sorts.descending("totalDeposit", "createTime");
        	docsList = this.mongoDBService.pageQuery(AGENT_TABLE + chainId, filter, sorts, pageNumber, pageSize);
        } else if (order == 4) {
        	sorts = Sorts.descending("deposit", "createTime");
        	docsList = this.mongoDBService.pageQuery(AGENT_TABLE + chainId, filter, sorts, pageNumber, pageSize);
        }
        
        LoggerUtil.commonLog.info(String.valueOf(System.currentTimeMillis() - start));
        
        List<AgentInfo> agentInfoList = new ArrayList<>();
        for (Document document : docsList) {
            AgentInfo agentInfo = DocumentTransferTool.toInfo(document, "txHash", AgentInfo.class);
            AliasInfo alias = mongoAliasServiceImpl.getAliasByAddress(chainId, agentInfo.getAgentAddress());
            if (alias != null) {
                agentInfo.setAgentAlias(alias.getAlias());
            }
            agentInfoList.add(agentInfo);
            if (agentInfo.getType() == 0 && null != agentInfo.getAgentAddress()) {
                if (ApiContext.DEVELOPER_NODE_ADDRESS.contains(agentInfo.getAgentAddress())) {
                    agentInfo.setType(2);
                } else if (ApiContext.AMBASSADOR_NODE_ADDRESS.contains(agentInfo.getAgentAddress())) {
                    agentInfo.setType(3);
                } else {
                    agentInfo.setType(1);
                }
            }
        }
        LoggerUtil.commonLog.info(String.valueOf(System.currentTimeMillis() - start));
        PageInfo<AgentInfo> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, agentInfoList);
        return pageInfo;
    }

    @Override
    public PageInfo<AgentInfo> getAgentList(int chainId, int pageNumber, int pageSize) {
        long totalCount = this.mongoDBService.getEstimateCount(AGENT_TABLE + chainId);
        List<Document> docsList = this.mongoDBService.pageQuery(AGENT_TABLE + chainId, Sorts.descending("createTime"), pageNumber, pageSize);
        List<AgentInfo> agentInfoList = new ArrayList<>();
        for (Document document : docsList) {
            AgentInfo agentInfo = DocumentTransferTool.toInfo(document, "txHash", AgentInfo.class);
            AliasInfo alias = mongoAliasServiceImpl.getAliasByAddress(chainId, agentInfo.getAgentAddress());
            if (alias != null) {
                agentInfo.setAgentAlias(alias.getAlias());
            }
            agentInfoList.add(agentInfo);
            if (agentInfo.getType() == 0 && null != agentInfo.getAgentAddress()) {
                if (ApiContext.DEVELOPER_NODE_ADDRESS.contains(agentInfo.getAgentAddress())) {
                    agentInfo.setType(2);
                } else if (ApiContext.AMBASSADOR_NODE_ADDRESS.contains(agentInfo.getAgentAddress())) {
                    agentInfo.setType(3);
                } else {
                    agentInfo.setType(1);
                }
            }
        }
        PageInfo<AgentInfo> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, agentInfoList);
        return pageInfo;
    }

    public long agentsCount(int chainId, long startHeight) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        Collection<AgentInfo> agentInfos = apiCache.getAgentMap().values();
        long count = 0;
        for (AgentInfo agent : agentInfos) {
            if (agent.getDeleteHash() != null && agent.getDeleteHeight() <= startHeight) {
                continue;
            }
            if (agent.getBlockHeight() > startHeight) {
                continue;
            }
            count++;
        }
        return count;
//        Bson bson = Filters.and(Filters.lte("blockHeight", startHeight), Filters.or(Filters.eq("deleteHeight", 0), Filters.gt("deleteHeight", startHeight)));
//        return this.mongoDBService.getCount(MongoTableName.AGENT_INFO, bson);
    }

    public BigInteger getConsensusCoinTotal(int chainId) {
        BigInteger total = BigInteger.ZERO;

        ApiCache apiCache = CacheManager.getCache(chainId);
        for (AgentInfo agentInfo : apiCache.getAgentMap().values()) {
            if (agentInfo.getDeleteHash() == null) {
                total = total.add(agentInfo.getDeposit()).add(agentInfo.getTotalDeposit());
            }
        }
        return total;
    }
}
