package io.icw.api.db;

import io.icw.api.model.po.AccountLedgerInfo;
import io.icw.api.model.po.PageInfo;
import io.icw.api.model.po.mini.MiniAccountInfo;

import java.util.List;
import java.util.Map;

public interface AccountLedgerService {

    void initCache();

    AccountLedgerInfo getAccountLedgerInfo(int chainId, String key);

    void saveLedgerList(int chainId, Map<String, AccountLedgerInfo> accountLedgerInfoMap);

    PageInfo<MiniAccountInfo> getAssetRanking(int chainId, int assetChainId, int assetId, int pageNumber, int pageSize);

    List<AccountLedgerInfo> getAccountLedgerInfoList(int chainId, String address);

    List<AccountLedgerInfo> getAccountCrossLedgerInfoList(int chainId, String address);

    List<AccountLedgerInfo> getAccountLedgerInfoList(int assetChainId, int assetId);

}
