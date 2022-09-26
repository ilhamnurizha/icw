package io.icw.api.db.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.Sorts;
import io.icw.api.model.po.PageInfo;
import io.icw.api.model.po.PunishLogInfo;
import io.icw.api.model.po.TxDataInfo;
import io.icw.api.db.PunishService;
import io.icw.api.utils.DocumentTransferTool;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.model.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static io.icw.api.constant.DBTableConstant.PUNISH_TABLE;

@Component
public class MongoPunishServiceImpl implements PunishService {

    @Autowired
    private MongoDBService mongoDBService;

    public void savePunishList(int chainId, List<PunishLogInfo> punishLogList) {
        if (punishLogList.isEmpty()) {
            return;
        }

        List<Document> documentList = new ArrayList<>();
        for (PunishLogInfo punishLog : punishLogList) {
            documentList.add(DocumentTransferTool.toDocument(punishLog));
        }
        InsertManyOptions options = new InsertManyOptions();
        options.ordered(false);
        mongoDBService.insertMany(PUNISH_TABLE + chainId, documentList, options);
    }

    public List<TxDataInfo> getYellowPunishLog(int chainId, String txHash) {
        List<Document> documentList = mongoDBService.query(PUNISH_TABLE + chainId, Filters.eq("txHash", txHash));
        List<TxDataInfo> punishLogs = new ArrayList<>();
        for (Document document : documentList) {
            PunishLogInfo punishLog = DocumentTransferTool.toInfo(document, PunishLogInfo.class);
            punishLogs.add(punishLog);
        }
        return punishLogs;
    }


    public PunishLogInfo getRedPunishLog(int chainId, String txHash) {
        Document document = mongoDBService.findOne(PUNISH_TABLE + chainId, Filters.eq("txHash", txHash));
        if (document == null) {
            return null;
        }
        PunishLogInfo punishLog = DocumentTransferTool.toInfo(document, PunishLogInfo.class);
        return punishLog;
    }

    public long getYellowCount(int chainId, String agentAddress) {
        Bson filter = and(eq("address", agentAddress), eq("type", 1));
        long count = mongoDBService.getCount(PUNISH_TABLE + chainId, filter);
        return count;
    }

    public PageInfo<PunishLogInfo> getPunishLogList(int chainId, int type, String address, int pageIndex, int pageSize) {
        Bson filter = null;

        if (type == 0 && !StringUtils.isBlank(address)) {
            filter = Filters.eq("address", address);
        } else if (type > 0 && StringUtils.isBlank(address)) {
            filter = Filters.eq("type", type);
        } else if (type > 0 && !StringUtils.isBlank(address)) {
            filter = Filters.and(eq("type", type), eq("address", address));
        }

        long totalCount = mongoDBService.getCount(PUNISH_TABLE + chainId, filter);
        List<Document> documentList = mongoDBService.pageQuery(PUNISH_TABLE + chainId, filter, Sorts.descending("time"), pageIndex, pageSize);
        List<PunishLogInfo> punishLogList = new ArrayList<>();
        for (Document document : documentList) {
            punishLogList.add(DocumentTransferTool.toInfo(document, PunishLogInfo.class));
        }
        PageInfo<PunishLogInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, totalCount, punishLogList);
        return pageInfo;
    }


    public void rollbackPunishLog(int chainID, List<String> txHashs, long height) {
        if (txHashs.isEmpty()) {
            return;
        }
        mongoDBService.delete(PUNISH_TABLE + chainID, Filters.eq("blockHeight", height));
    }
}
