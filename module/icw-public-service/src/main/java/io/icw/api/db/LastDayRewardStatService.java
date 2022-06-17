package io.icw.api.db;

import io.icw.api.model.po.LastDayRewardStatInfo;

public interface LastDayRewardStatService {

    LastDayRewardStatInfo getInfo(int chainId);

    void save(int chainId, LastDayRewardStatInfo statInfo);

    void update(int chainId, LastDayRewardStatInfo statInfo);
}
