package io.icw.transaction.utils;

import io.icw.core.rockdb.service.RocksDBService;
import io.icw.core.exception.NulsRuntimeException;

import io.icw.transaction.constant.TxErrorCode;

/**
 * @author: Charlie
 * @date: 2018/11/13
 */
public class DBUtil {

    public static void createTable(String name){
         if(!RocksDBService.existTable(name)) {
            try {
                RocksDBService.createTable(name);
            } catch (Exception e) {
                LoggerUtil.LOG.error(e);
                throw new NulsRuntimeException(TxErrorCode.DB_TABLE_CREATE_ERROR);
            }
        }
    }
}
