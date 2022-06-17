package io.icw.api.db.mongo;

import com.mongodb.client.model.Filters;
import io.icw.api.model.po.LastDayRewardStatInfo;
import io.icw.api.constant.DBTableConstant;
import io.icw.api.db.LastDayRewardStatService;
import io.icw.api.utils.DocumentTransferTool;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import org.bson.Document;
import org.bson.conversions.Bson;

@Component
public class MongoLastDayRewardStatServiceImpl implements LastDayRewardStatService {

    @Autowired
    private MongoDBService mongoDBService;

    @Override
    public LastDayRewardStatInfo getInfo(int chainId) {

        Document document = mongoDBService.findOne(DBTableConstant.LAST_DAY_REWARD_TABLE + chainId, Filters.eq("_id", DBTableConstant.LastDayRewardKey));
        if (document == null) {
            return null;
        }
        LastDayRewardStatInfo statInfo = DocumentTransferTool.toInfo(document, "lastDayRewardKey", LastDayRewardStatInfo.class);
        return statInfo;
    }

    @Override
    public void save(int chainId, LastDayRewardStatInfo statInfo) {
        Document document = DocumentTransferTool.toDocument(statInfo, "lastDayRewardKey");
        mongoDBService.insertOne(DBTableConstant.LAST_DAY_REWARD_TABLE + chainId, document);
    }

    @Override
    public void update(int chainId, LastDayRewardStatInfo statInfo) {
        Bson filter = Filters.eq("_id", statInfo.getLastDayRewardKey());
        Document document = DocumentTransferTool.toDocument(statInfo, "lastDayRewardKey");
        mongoDBService.updateOne(DBTableConstant.LAST_DAY_REWARD_TABLE + chainId, filter, document);
    }
}
