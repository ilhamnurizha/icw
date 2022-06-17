package io.icw.api.task;

import io.icw.api.db.mongo.MongoAccountLedgerServiceImpl;
import io.icw.api.db.mongo.MongoAgentServiceImpl;
import io.icw.api.model.po.AgentInfo;
import io.icw.api.model.po.PageInfo;
import io.icw.api.model.po.mini.MiniAccountInfo;
import io.icw.api.utils.LoggerUtil;
import io.icw.api.ApiContext;

/**
 * 刷新首页缓存task
 */
public class RefreshCacheTask implements Runnable {

    private int chainId;

    private MongoAgentServiceImpl agentService;

    private MongoAccountLedgerServiceImpl accountLedgerService;

    public RefreshCacheTask(int chainId, MongoAgentServiceImpl agentService, MongoAccountLedgerServiceImpl accountLedgerService) {
        this.chainId = chainId;
        this.agentService = agentService;
        this.accountLedgerService = accountLedgerService;
    }

    @Override
    public void run() {
        try {
            PageInfo<AgentInfo> agentPageInfo = agentService.getAgentList(chainId, 0, 1, 200);
            ApiContext.agentPageInfo = agentPageInfo;

            PageInfo<MiniAccountInfo> miniAccountPageInfo = accountLedgerService.getAssetRanking(chainId, chainId, 1, 1, 15);
            ApiContext.miniAccountPageInfo = miniAccountPageInfo;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
    }
}
