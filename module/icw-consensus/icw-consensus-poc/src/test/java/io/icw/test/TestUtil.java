package io.icw.test;

import io.icw.core.model.ObjectUtils;
import io.icw.core.rockdb.constant.DBErrorCode;
import io.icw.core.rockdb.service.RocksDBService;
import io.icw.poc.constant.ConsensusConfig;
import io.icw.poc.constant.ConsensusConstant;
import io.icw.core.log.Log;

public class TestUtil {
    public static void initTable(int chainId){
        try {
            /*
            创建共识节点表
            Create consensus node tables
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_AGENT+chainId);

            /*
            创建共识信息表
            Create consensus information tables
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT+chainId);

            /*
            创建红黄牌信息表
            Creating Red and Yellow Card Information Table
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_PUNISH+chainId);
        }catch (Exception e){
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.info(e.getMessage());
            }
        }
    }
    public static void main(String []args){
        byte [] objs=ObjectUtils.objectToBytes(new ConsensusConfig());
    }
}
