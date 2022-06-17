package io.icw.api.task;

import io.icw.api.constant.DBTableConstant;
import io.icw.api.db.mongo.MongoLastDayRewardStatServiceImpl;
import io.icw.api.model.po.BlockHeaderInfo;
import io.icw.api.model.po.LastDayRewardStatInfo;
import io.icw.api.ApiContext;
import io.icw.api.db.AccountService;
import io.icw.api.db.BlockService;
import io.icw.api.db.LastDayRewardStatService;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.log.Log;
import io.icw.core.model.DateUtils;

public class LastDayRewardStatTask implements Runnable {

    private int chainId;

    private LastDayRewardStatService lastDayRewardStatService;

    private BlockService blockService;

    private AccountService accountService;

    public LastDayRewardStatTask(int chainId) {
        this.chainId = chainId;
        lastDayRewardStatService = SpringLiteContext.getBean(MongoLastDayRewardStatServiceImpl.class);
        blockService = SpringLiteContext.getBean(BlockService.class);
        accountService = SpringLiteContext.getBean(AccountService.class);
    }

    @Override
    public void run() {
        ApiContext.locker.lock();
        try {
            LastDayRewardStatInfo statInfo = lastDayRewardStatService.getInfo(chainId);
            if (statInfo == null) {
                statInfo = new LastDayRewardStatInfo();
                statInfo.setLastDayRewardKey(DBTableConstant.LastDayRewardKey);
                lastDayRewardStatService.save(chainId, statInfo);
            }
            BlockHeaderInfo headerInfo = blockService.getBestBlockHeader(chainId);
            if (headerInfo == null) {
                return;
            }
            //判断当前块时间和上次统计块时间日期相差了一天
            if (headerInfo.getCreateTime() - statInfo.getLastStatTime() < DateUtils.DATE_TIME / 1000) {
                return;
            }
            //已超过一天，将所有账户的昨日收益转变为今日收益，今日收益清空后，重新获取
            accountService.updateAllAccountLastReward(chainId);

            statInfo.setLastStatHeight(headerInfo.getHeight());
            statInfo.setLastStatTime(headerInfo.getCreateTime());
            lastDayRewardStatService.update(chainId, statInfo);
        } catch (Exception e) {
            Log.error("------统计昨日收益异常-----", e);
        } finally {
            ApiContext.locker.unlock();
        }
    }
}
