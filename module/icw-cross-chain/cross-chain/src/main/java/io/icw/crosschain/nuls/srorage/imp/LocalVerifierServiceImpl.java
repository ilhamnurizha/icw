package io.icw.crosschain.nuls.srorage.imp;

import io.icw.core.core.annotation.Component;
import io.icw.core.log.Log;
import io.icw.core.model.ByteUtils;
import io.icw.core.rockdb.service.RocksDBService;
import io.icw.crosschain.nuls.constant.NulsCrossChainConstant;
import io.icw.crosschain.nuls.model.po.LocalVerifierPO;
import io.icw.crosschain.nuls.srorage.LocalVerifierService;

@Component
public class LocalVerifierServiceImpl implements LocalVerifierService {
    @Override
    public boolean save(LocalVerifierPO po, int chainID) {
        try {
            if(po == null){
                return false;
            }
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_LOCAL_VERIFIER, ByteUtils.intToBytes(chainID), po.serialize());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public boolean backup(int chainID, long blockHeight) {
        LocalVerifierPO localVerifierPO = get(chainID);
        try {
            if(localVerifierPO == null){
                return false;
            }
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_OLD_LOCAL_VERIFIER + chainID, ByteUtils.longToBytes(blockHeight), localVerifierPO.serialize());
        }catch (Exception e){
            Log.error(e);
        }
        return true;
    }

    @Override
    public boolean rollback(int chainID, long blockHeight) {
        byte[] value = RocksDBService.get(NulsCrossChainConstant.DB_NAME_OLD_LOCAL_VERIFIER + chainID,ByteUtils.longToBytes(blockHeight));
        if(value == null){
            Log.error("没有找到备份数据" );
            return true;
        }
        try {
            RocksDBService.put(NulsCrossChainConstant.DB_NAME_LOCAL_VERIFIER, ByteUtils.intToBytes(chainID), value);
            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }

    }

    @Override
    public LocalVerifierPO get(int chainID) {
        byte[] value = RocksDBService.get(NulsCrossChainConstant.DB_NAME_LOCAL_VERIFIER,ByteUtils.intToBytes(chainID));
        if(value == null){
            Log.info("The chain verifier has not been initialized" );
            return null;
        }
        LocalVerifierPO po = new LocalVerifierPO();
        try {
            po.parse(value,0);
        }catch (Exception e){
            Log.error(e);
        }
        return po;
    }

    @Override
    public boolean delete(int chainID) {
        try {
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_LOCAL_VERIFIER,ByteUtils.intToBytes(chainID));
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }
}
