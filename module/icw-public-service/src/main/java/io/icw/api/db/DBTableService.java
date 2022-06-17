package io.icw.api.db;

import io.icw.api.model.po.ChainConfigInfo;
import io.icw.api.model.po.ChainInfo;

/**
 *
 */
public interface DBTableService {

    void initCache();

    void addDefaultChainCache();

    void addChainCache(ChainInfo chainInfo, ChainConfigInfo configInfo);
}
