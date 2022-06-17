package io.icw.api.db;

import io.icw.api.model.po.PageInfo;
import io.icw.api.model.po.PunishLogInfo;
import io.icw.api.model.po.TxDataInfo;

import java.util.List;

public interface PunishService {

    void savePunishList(int chainId, List<PunishLogInfo> punishLogList);

    List<TxDataInfo> getYellowPunishLog(int chainId, String txHash);

    PunishLogInfo getRedPunishLog(int chainId, String txHash);

    long getYellowCount(int chainId, String agentAddress);

    PageInfo<PunishLogInfo> getPunishLogList(int chainId, int type, String address, int pageIndex, int pageSize);

    void rollbackPunishLog(int chainID,List<String> txHashs, long height);
}
