package io.icw.chain.storage;

import io.icw.core.exception.NulsException;

public interface InitDB {
    /**
     * 初始化表
     */
    void initTableName() throws NulsException;
}
