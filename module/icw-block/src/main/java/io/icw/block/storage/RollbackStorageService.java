package io.icw.block.storage;

import io.icw.block.model.RollbackInfoPo;

public interface RollbackStorageService {
    public boolean save(RollbackInfoPo po, int chainId);

    public RollbackInfoPo get(int chainId);
}
