package io.icw.api.db.mongo;

import com.mongodb.client.model.*;
import io.icw.api.model.po.AccountToken721Info;
import io.icw.api.model.po.Nrc721TokenIdInfo;
import io.icw.api.model.po.PageInfo;
import io.icw.api.model.po.Token721Transfer;
import io.icw.api.db.Token721Service;
import io.icw.api.utils.DocumentTransferTool;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.model.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

import static io.icw.api.constant.DBTableConstant.*;

@Component
public class MongoToken721ServiceImpl implements Token721Service {

    @Autowired
    private MongoDBService mongoDBService;

    public AccountToken721Info getAccountTokenInfo(int chainId, String key) {
        Bson query = Filters.eq("_id", key);

        Document document = mongoDBService.findOne(ACCOUNT_TOKEN721_TABLE + chainId, query);
        if (document == null) {
            return null;
        }
        AccountToken721Info tokenInfo = DocumentTransferTool.toInfo(document, "key", AccountToken721Info.class);
        return tokenInfo;
    }

    public void saveAccountTokens(int chainId, Map<String, AccountToken721Info> accountTokenInfos) {
        if (accountTokenInfos.isEmpty()) {
            return;
        }
        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (AccountToken721Info tokenInfo : accountTokenInfos.values()) {
            Document document = DocumentTransferTool.toDocument(tokenInfo, "key");
            if (tokenInfo.isNew()) {
                modelList.add(new InsertOneModel(document));
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", tokenInfo.getKey()), document));
            }
        }
        BulkWriteOptions options = new BulkWriteOptions();
        options.ordered(false);
        mongoDBService.bulkWrite(ACCOUNT_TOKEN721_TABLE + chainId, modelList, options);
    }

    public PageInfo<AccountToken721Info> getAccountTokens(int chainId, String address, int pageNumber, int pageSize) {
        Bson query = Filters.eq("address", address);
        Bson sort = Sorts.descending("tokenCount");
        List<Document> docsList = this.mongoDBService.pageQuery(ACCOUNT_TOKEN721_TABLE + chainId, query, sort, pageNumber, pageSize);
        List<AccountToken721Info> accountTokenList = new ArrayList<>();
        long totalCount = mongoDBService.getCount(ACCOUNT_TOKEN721_TABLE + chainId, query);

        for (Document document : docsList) {
            AccountToken721Info tokenInfo = DocumentTransferTool.toInfo(document, "key", AccountToken721Info.class);
            accountTokenList.add(tokenInfo);
            document = mongoDBService.findOne(CONTRACT_TABLE + chainId, Filters.eq("_id", tokenInfo.getContractAddress()));
            tokenInfo.setStatus(document.getInteger("status"));
        }

        PageInfo<AccountToken721Info> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, accountTokenList);
        return pageInfo;
    }

    public PageInfo<AccountToken721Info> getContractTokens(int chainId, String contractAddress, int pageNumber, int pageSize) {
        Bson query = Filters.eq("contractAddress", contractAddress);
        Bson sort = Sorts.descending("tokenCount");
        List<Document> docsList = this.mongoDBService.pageQuery(ACCOUNT_TOKEN721_TABLE + chainId, query, sort, pageNumber, pageSize);
        List<AccountToken721Info> accountTokenList = new ArrayList<>();
        long totalCount = mongoDBService.getCount(ACCOUNT_TOKEN721_TABLE + chainId, query);
        for (Document document : docsList) {
            accountTokenList.add(DocumentTransferTool.toInfo(document, "key", AccountToken721Info.class));
        }
        PageInfo<AccountToken721Info> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, accountTokenList);
        return pageInfo;
    }


    public void saveTokenTransfers(int chainId, List<Token721Transfer> tokenTransfers) {
        if (tokenTransfers.isEmpty()) {
            return;
        }
        List<Document> documentList = new ArrayList<>();
        for (Token721Transfer tokenTransfer : tokenTransfers) {
            Document document = DocumentTransferTool.toDocument(tokenTransfer);
            documentList.add(document);
        }
        InsertManyOptions options = new InsertManyOptions();
        options.ordered(false);
        mongoDBService.insertMany(TOKEN721_TRANSFER_TABLE + chainId, documentList, options);
    }

    public void rollbackTokenTransfers(int chainId, List<String> tokenTxHashs, long height) {
        if (tokenTxHashs.isEmpty()) {
            return;
        }
        mongoDBService.delete(TOKEN721_TRANSFER_TABLE + chainId, Filters.eq("height", height));
    }

    public PageInfo<Token721Transfer> getTokenTransfers(int chainId, String address, String contractAddress, int pageIndex, int pageSize) {
        Bson filter = null;
        if (StringUtils.isNotBlank(address) && StringUtils.isNotBlank(contractAddress)) {
            Bson addressFilter = Filters.or(Filters.eq("fromAddress", address), Filters.eq("toAddress", address));
            filter = Filters.and(Filters.eq("contractAddress", contractAddress), addressFilter);
        } else if (StringUtils.isNotBlank(contractAddress)) {
            filter = Filters.eq("contractAddress", contractAddress);
        } else if (StringUtils.isNotBlank(address)) {
            filter = Filters.or(Filters.eq("fromAddress", address), Filters.eq("toAddress", address));
        }
        Bson sort = Sorts.descending("time");
        List<Document> docsList = this.mongoDBService.pageQuery(TOKEN721_TRANSFER_TABLE + chainId, filter, sort, pageIndex, pageSize);
        List<Token721Transfer> tokenTransfers = new ArrayList<>();
        long totalCount = mongoDBService.getCount(TOKEN721_TRANSFER_TABLE + chainId, filter);
        for (Document document : docsList) {
            tokenTransfers.add(DocumentTransferTool.toInfo(document, Token721Transfer.class));
        }

        PageInfo<Token721Transfer> pageInfo = new PageInfo<>(pageIndex, pageSize, totalCount, tokenTransfers);
        return pageInfo;
    }

    @Override
    public void saveTokenIds(int chainId, List<Nrc721TokenIdInfo> tokenIdInfos) {
        if (tokenIdInfos.isEmpty()) {
            return;
        }
        Set<String> insertKeys = new HashSet<>();
        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (Nrc721TokenIdInfo tokenIdInfo : tokenIdInfos) {
            if (tokenIdInfo.getTime() != null) {
                // 造币
                boolean notExist = insertKeys.add(tokenIdInfo.getKey());
                if (!notExist) {
                    // 已存在，跳过
                    continue;
                }
                Bson query = Filters.eq("_id", tokenIdInfo.getKey());
                Document currentDocument = mongoDBService.findOne(TOKEN721_IDS_TABLE + chainId, query);
                if (currentDocument == null) {
                    Document document = DocumentTransferTool.toDocument(tokenIdInfo, "key");
                    modelList.add(new InsertOneModel(document));
                }
            } else if (tokenIdInfo.getOwner() != null) {
                // 转账
                Bson query = Filters.eq("_id", tokenIdInfo.getKey());
                Document currentDocument = mongoDBService.findOne(TOKEN721_IDS_TABLE + chainId, query);
                currentDocument.put("owner", tokenIdInfo.getOwner());
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", tokenIdInfo.getKey()), currentDocument));
            } else {
                // 销毁
                modelList.add(new DeleteOneModel<>(Filters.eq("_id", tokenIdInfo.getKey())));
            }
        }
        if (modelList.isEmpty()) {
            return;
        }
        BulkWriteOptions options = new BulkWriteOptions();
        options.ordered(false);
        mongoDBService.bulkWrite(TOKEN721_IDS_TABLE + chainId, modelList, options);
    }

    @Override
    public void rollbackTokenIds(int chainId, List<Nrc721TokenIdInfo> tokenIdInfos) {
        if (tokenIdInfos.isEmpty()) {
            return;
        }
        Set<String> insertKeys = new HashSet<>();
        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (Nrc721TokenIdInfo tokenIdInfo : tokenIdInfos) {
            if (tokenIdInfo.getTime() != null) {
                // 销毁回滚
                boolean notExist = insertKeys.add(tokenIdInfo.getKey());
                if (!notExist) {
                    // 已存在，跳过
                    continue;
                }
                Bson query = Filters.eq("_id", tokenIdInfo.getKey());
                Document currentDocument = mongoDBService.findOne(TOKEN721_IDS_TABLE + chainId, query);
                if (currentDocument == null) {
                    Document document = DocumentTransferTool.toDocument(tokenIdInfo, "key");
                    modelList.add(new InsertOneModel(document));
                }
            } else if (tokenIdInfo.getOwner() != null) {
                // 转账回滚token的拥有者
                Bson query = Filters.eq("_id", tokenIdInfo.getKey());
                Document currentDocument = mongoDBService.findOne(TOKEN721_IDS_TABLE + chainId, query);
                currentDocument.put("owner", tokenIdInfo.getOwner());
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", tokenIdInfo.getKey()), currentDocument));
            } else {
                // 造币回滚
                modelList.add(new DeleteOneModel<>(Filters.eq("_id", tokenIdInfo.getKey())));
            }
        }
        if (!modelList.isEmpty()) {
            BulkWriteOptions options = new BulkWriteOptions();
            options.ordered(false);
            mongoDBService.bulkWrite(TOKEN721_IDS_TABLE + chainId, modelList, options);
        }
    }

    @Override
    public Nrc721TokenIdInfo getContractTokenId(int chainId, String contractAddress, String tokenId) {
        Bson query = Filters.eq("_id", contractAddress + tokenId);
        Document document = mongoDBService.findOne(TOKEN721_IDS_TABLE + chainId, query);
        if (document == null) {
            return null;
        }
        Nrc721TokenIdInfo tokenIdInfo = DocumentTransferTool.toInfo(document, "key", Nrc721TokenIdInfo.class);
        return tokenIdInfo;
    }

    @Override
    public PageInfo<Nrc721TokenIdInfo> getContractTokenIds(int chainId, String contractAddress, int pageNumber, int pageSize) {
        Bson query = Filters.eq("contractAddress", contractAddress);
        Bson sort = Sorts.descending("time");
        List<Document> docsList = this.mongoDBService.pageQuery(TOKEN721_IDS_TABLE + chainId, query, sort, pageNumber, pageSize);
        List<Nrc721TokenIdInfo> tokenIdList = new ArrayList<>();
        long totalCount = mongoDBService.getCount(TOKEN721_IDS_TABLE + chainId, query);
        for (Document document : docsList) {
            tokenIdList.add(DocumentTransferTool.toInfo(document, "key", Nrc721TokenIdInfo.class));
        }
        PageInfo<Nrc721TokenIdInfo> pageInfo = new PageInfo<>(pageNumber, pageSize, totalCount, tokenIdList);
        return pageInfo;
    }
}
