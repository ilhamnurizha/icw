package io.icw.chain.storage.impl;

import io.icw.chain.storage.ChainCirculateStorage;
import io.icw.chain.storage.InitDB;
import io.icw.core.basic.InitializingBean;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.core.model.ByteUtils;
import io.icw.core.rockdb.service.RocksDBService;

import java.math.BigInteger;


@Component
public class ChainCirculateStorageImpl extends BaseStorage implements ChainCirculateStorage, InitDB, InitializingBean {

    private final String TBL = "chain_circulate";

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {

    }


    @Override
    public void initTableName() throws NulsException {
        super.initTableName(TBL);
    }

    @Override
    public BigInteger load(String key) throws Exception {
        byte[] bytes = RocksDBService.get(TBL, key.getBytes());
        if (bytes == null) {
            return null;
        }
        BigInteger amount = ByteUtils.bytesToBigInteger(bytes);
        return amount;
    }

    @Override
    public void save(String key, BigInteger amount) throws Exception {
        RocksDBService.put(TBL, key.getBytes(), amount.toByteArray());
    }
}
