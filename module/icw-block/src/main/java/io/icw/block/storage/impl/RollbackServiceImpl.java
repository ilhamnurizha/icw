package io.icw.block.storage.impl;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.block.constant.Constant;
import io.icw.block.model.RollbackInfoPo;
import io.icw.block.storage.RollbackStorageService;
import io.icw.core.core.annotation.Component;
import io.icw.core.model.ByteUtils;
import io.icw.core.rockdb.service.RocksDBService;
import static io.icw.block.utils.LoggerUtil.COMMON_LOG;

@Component
public class RollbackServiceImpl implements RollbackStorageService {
    public boolean save(RollbackInfoPo po, int chainId) {
        byte[] bytes;
        try {
            bytes = po.serialize();
            return RocksDBService.put(Constant.ROLLBACK_HEIGHT, ByteUtils.intToBytes(chainId), bytes);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return false;
        }
    }

    public RollbackInfoPo get(int chainId) {
        try {
            RollbackInfoPo po = new RollbackInfoPo();
            byte[] bytes = RocksDBService.get(Constant.ROLLBACK_HEIGHT, ByteUtils.intToBytes(chainId));
            if(bytes == null){
                return null;
            }
            po.parse(new NulsByteBuffer(bytes));
            return po;
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return null;
        }
    }
}
