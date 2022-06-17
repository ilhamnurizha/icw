/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.icw.api.rpc.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.icw.api.constant.config.ApiConfig;
import io.icw.api.model.po.mini.MiniAccountInfo;
import io.icw.api.rpc.controller.runner.TxTypeEnum;
import io.icw.api.ApiContext;
import io.icw.api.analysis.WalletRpcHandler;
import io.icw.api.cache.ApiCache;
import io.icw.api.db.*;
import io.icw.api.manager.CacheManager;
import io.icw.api.model.po.*;
import io.icw.api.model.rpc.*;
import io.icw.api.utils.LoggerUtil;
import io.icw.api.utils.VerifyUtils;
import io.icw.base.basic.AddressTool;
import io.icw.core.basic.Result;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Controller;
import io.icw.core.core.annotation.RpcMethod;
import io.icw.core.model.StringUtils;
import io.icw.core.parse.MapUtils;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Niels
 */
@Controller
public class AccountController {
    @Autowired
    private ApiConfig apiConfig;
    @Autowired
    private AccountService accountService;
    @Autowired
    private BlockService blockHeaderService;
    @Autowired
    private ChainService chainService;
    @Autowired
    private AccountLedgerService accountLedgerService;
    @Autowired
    private AliasService aliasService;

    @Autowired
    TokenService tokenService;

    @RpcMethod("getAccountList")
    public RpcResult getAccountList(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, pageNumber, pageSize;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        RpcResult result = new RpcResult();
        PageInfo<AccountInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = accountService.pageQuery(chainId, pageNumber, pageSize);
        } else {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
        }
        result.setResult(pageInfo);
        return result;

    }

    @RpcMethod("getAccountTxs")
    public RpcResult getAccountTxs(List<Object> params) {
        VerifyUtils.verifyParams(params, 7);
        int chainId, assetChainId, assetId, pageNumber, pageSize, type;
        String address;
        long startHeight, endHeight;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            type = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[type] is inValid");
        }
        try {
            startHeight = Long.parseLong("" + params.get(5));
        } catch (Exception e) {
            return RpcResult.paramError("[startHeight] is invalid");
        }
        try {
            endHeight = Long.parseLong("" + params.get(6));
        } catch (Exception e) {
            return RpcResult.paramError("[endHeight] is invalid");
        }
        try {
            assetChainId = (int) params.get(7);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is invalid");
        }
        try {
            assetId = (int) params.get(8);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is invalid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        RpcResult result = new RpcResult();
        try {
            PageInfo<TxRelationInfo> pageInfo;
            if (CacheManager.isChainExist(chainId)) {
                pageInfo = accountService.getAccountTxs(chainId, address, pageNumber, pageSize, type, startHeight, endHeight, assetChainId, assetId);
                result.setResult(new PageInfo<>(pageNumber, pageSize, pageInfo.getTotalCount(), pageInfo.getList().stream().map(d -> {
                    Map res = MapUtils.beanToMap(d);
                    AssetInfo assetInfo = CacheManager.getAssetInfoMap().get(d.getChainId() + "-" + d.getAssetId());
                    if (assetInfo != null) {
                        res.put("symbol", assetInfo.getSymbol());
                        res.put("decimals", assetInfo.getDecimals());
                    }
                    return res;
                }).collect(Collectors.toList())));
            } else {
                result.setResult(new PageInfo<>(pageNumber, pageSize));
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
        return result;
    }

    @RpcMethod("getAcctTxs")
    public RpcResult getAcctTxs(List<Object> params) {
        VerifyUtils.verifyParams(params, 7);
        int chainId, assetChainId, assetId, pageNumber, pageSize, type;
        String address;
        long startTime, endTime;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            type = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[type] is inValid");
        }
        try {
            assetChainId = (int) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is inValid");
        }
        try {
            assetId = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is inValid");
        }
        try {
            startTime = Long.parseLong("" + params.get(5));
        } catch (Exception e) {
            return RpcResult.paramError("[startTime] is invalid");
        }
        try {
            endTime = Long.parseLong("" + params.get(6));
        } catch (Exception e) {
            return RpcResult.paramError("[endTime] is invalid");
        }

        try {
            pageNumber = (int) params.get(7);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(8);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        RpcResult result = new RpcResult();
        try {
            PageInfo<TxRelationInfo> pageInfo;
            if (CacheManager.isChainExist(chainId)) {
                pageInfo = accountService.getAcctTxs(chainId, assetChainId, assetId, address, type, startTime, endTime, pageNumber, pageSize);
                result.setResult(new PageInfo<>(pageNumber, pageSize, pageInfo.getTotalCount(), pageInfo.getList().stream().map(d -> {
                    Map res = MapUtils.beanToMap(d);
                    AssetInfo assetInfo = CacheManager.getAssetInfoMap().get(d.getChainId() + "-" + d.getAssetId());
                    if (assetInfo != null) {
                        res.put("symbol", assetInfo.getSymbol());
                        res.put("decimals", assetInfo.getDecimals());
                    }
                    return res;
                }).collect(Collectors.toList())));
            } else {
                result.setResult(new PageInfo<>(pageNumber, pageSize));
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
        return result;
    }

    @RpcMethod("getAcctTxs_1")
    public RpcResult getAcctTxs_1(List<Object> params) {
        VerifyUtils.verifyParams(params, 7);
        int chainId, assetChainId, assetId, pageNumber, pageSize, type;
        String address;
        long startTime, endTime;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            type = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[type] is inValid");
        }
        try {
            assetChainId = (int) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is inValid");
        }
        try {
            assetId = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is inValid");
        }
        try {
            startTime = Long.parseLong("" + params.get(5));
        } catch (Exception e) {
            return RpcResult.paramError("[startTime] is invalid");
        }
        try {
            endTime = Long.parseLong("" + params.get(6));
        } catch (Exception e) {
            return RpcResult.paramError("[endTime] is invalid");
        }

        try {
            pageNumber = (int) params.get(7);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(8);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        RpcResult result = new RpcResult();
        try {
            PageInfo<TxRelationInfo> pageInfo;
            if (CacheManager.isChainExist(chainId)) {
                pageInfo = accountService.getAccountTxsByTime(chainId, address, pageNumber, pageSize, type, startTime, endTime, assetChainId, assetId);
                result.setResult(new PageInfo<>(pageNumber, pageSize, pageInfo.getTotalCount(), pageInfo.getList().stream().map(d -> {
                    Map res = MapUtils.beanToMap(d);
                    AssetInfo assetInfo = CacheManager.getAssetInfoMap().get(d.getChainId() + "-" + d.getAssetId());
                    var decimals = new BigDecimal("1");
                    var scale = 1;
                    if (assetInfo != null) {
                        res.put("symbol", assetInfo.getSymbol());
                        res.put("decimals", assetInfo.getDecimals());
                        decimals = BigDecimal.valueOf(Math.pow(10, assetInfo.getDecimals()));
                        scale = assetInfo.getDecimals();
                    }
                    res.put("values",
                            String.valueOf(new BigDecimal(d.getValues().multiply(BigInteger.valueOf(d.getTransferType()))).divide(decimals, scale, RoundingMode.DOWN)));
                    res.put("balance", String.valueOf(new BigDecimal(d.getBalance()).divide(decimals, scale, RoundingMode.DOWN)));
                    FeeInfo feeInfo = d.getFee();
                    FeeInfoStr feeInfoStr = new FeeInfoStr();
                    feeInfoStr.setChainId(feeInfo.getChainId());
                    feeInfoStr.setAssetId(feeInfo.getAssetId());
                    feeInfoStr.setSymbol(feeInfo.getSymbol());
                    feeInfoStr.setValue(String.valueOf(new BigDecimal(feeInfo.getValue()).divide(decimals, scale, RoundingMode.DOWN)));
                    res.put("feeInfo", feeInfoStr);
                    res.put("type", TxTypeEnum.getCnName(d.getType()));
                    if (d.getType() == 2) {
                        res.put("transferType", d.getTransferType()==1?"充值":"提现");
                    } else {
                        res.put("transferType", String.valueOf(d.getTransferType()));
                    }
                    return res;
                }).collect(Collectors.toList())));
            } else {
                result.setResult(new PageInfo<>(pageNumber, pageSize));
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
        return result;
    }

    @RpcMethod("getAcctTxsToExcel")
    public RpcResult getAcctTxsToExcel(List<Object> params) {
        VerifyUtils.verifyParams(params, 7);
        int chainId, assetChainId, assetId, pageNumber, pageSize, type;
        String address;
        long startTime, endTime;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            type = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[type] is inValid");
        }
        try {
            assetChainId = (int) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is inValid");
        }
        try {
            assetId = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is inValid");
        }
        try {
            startTime = Long.parseLong("" + params.get(5));
        } catch (Exception e) {
            return RpcResult.paramError("[startTime] is invalid");
        }
        try {
            endTime = Long.parseLong("" + params.get(6));
        } catch (Exception e) {
            return RpcResult.paramError("[endTime] is invalid");
        }

        try {
            pageNumber = (int) params.get(7);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(8);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 500) {
            pageSize = 500;
        }

        RpcResult result = new RpcResult();
        ExcelWriter excelWriter = null;
        try {
            PageInfo<TxRelationInfo> pageInfo;
            if (CacheManager.isChainExist(chainId)) {
                pageInfo = accountService.getAccountTxsByTime(chainId, address, 1, pageSize, type, startTime, endTime, assetChainId, assetId);
                // 创建临时文件
                File temp = File.createTempFile("txRelationInfo", ".xlsx");
                excelWriter = EasyExcel.write(temp, ExcelData.class).build();
                WriteSheet writeSheet = EasyExcel.writerSheet("交易数据").build();
                // 写入第一页
                writeToExcelBase64(pageInfo.getList(), writeSheet, excelWriter);
                // 写入后续页码
                long count = (pageInfo.getTotalCount()-1)/pageInfo.getPageSize()+1;
                for(long i=2; i<=count; i++) {
                    pageInfo = accountService.getAccountTxsByTime(chainId, address, (int)i, pageSize, type, startTime, endTime, assetChainId, assetId);
                    writeToExcelBase64(pageInfo.getList(), writeSheet, excelWriter);
                }
                // 关闭流
                excelWriter.finish();
                // 文件转base64
                FileInputStream inputFile = new FileInputStream(temp);
                byte[] buffer = new byte[(int) temp.length()];
                inputFile.read(buffer);
                inputFile.close();
                // 删除临时文件
                temp.delete();
                String s = new String(Base64.getEncoder().encode(buffer), StandardCharsets.UTF_8);
                result.setResult(s);
            } else {
                result.setResult("");
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
        return result;
    }

    private void writeToExcelBase64(List<TxRelationInfo> list, WriteSheet writeSheet, ExcelWriter excelWriter) {
        List<ExcelData> excelDataList = new ArrayList<>();
        list.forEach(l-> {
            AssetInfo assetInfo = CacheManager.getAssetInfoMap().get(l.getChainId() + "-" + l.getAssetId());
            var decimals = new BigDecimal("1");
            var scale = 1;
            if (assetInfo != null) {
                decimals = BigDecimal.valueOf(Math.pow(10, assetInfo.getDecimals()));
                scale = assetInfo.getDecimals();
            }
            ExcelData data = new ExcelData();
            data.setHash(l.getTxHash());
            data.setDate(new Date(l.getCreateTime() * 1000));
            data.setValues(String.valueOf(new BigDecimal(l.getValues().multiply(BigInteger.valueOf(l.getTransferType()))).divide(decimals, scale, RoundingMode.DOWN)));
            data.setFee(String.valueOf(new BigDecimal(l.getFee().getValue()).divide(decimals, scale, RoundingMode.DOWN)));
            data.setBalance(String.valueOf(new BigDecimal(l.getBalance()).divide(decimals, scale, RoundingMode.DOWN)));
            data.setType(TxTypeEnum.getCnName(l.getType()));
            if (l.getType() == 2) {
                data.setTransferType(l.getTransferType()==1?"充值":"提现");
            } else {
                data.setTransferType(String.valueOf(l.getTransferType()));
            }
            excelDataList.add(data);
        });
        excelWriter.write(excelDataList, writeSheet);
    }

    @RpcMethod("getAccount")
    public RpcResult getAccount(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

        RpcResult result = new RpcResult();
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.dataNotFound();
        }
        AccountInfo accountInfo = accountService.getAccountInfo(chainId, address);
        if (accountInfo == null) {
            accountInfo = new AccountInfo(address);
        } else {
            AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
            BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, address, defaultAsset.getChainId(), defaultAsset.getAssetId());
            accountInfo.setBalance(balanceInfo.getBalance());
            // accountInfo.setConsensusLock(balanceInfo.getConsensusLock());
            accountInfo.setTimeLock(balanceInfo.getTimeLock());
        }
        accountInfo.setSymbol(ApiContext.defaultSymbol);
        return result.setResult(accountInfo);
    }

    @RpcMethod("getAccountByAlias")
    public RpcResult getAccountByAlias(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String alias;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            alias = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[alias] is inValid");
        }
        RpcResult result = new RpcResult();
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.dataNotFound();
        }
        AliasInfo aliasInfo = aliasService.getByAlias(chainId, alias);
        if (aliasInfo == null) {
            return RpcResult.dataNotFound();
        }
        AccountInfo accountInfo = accountService.getAccountInfo(chainId, aliasInfo.getAddress());
        if (accountInfo == null) {
            return RpcResult.dataNotFound();
        } else {
            AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
            BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, aliasInfo.getAddress(), defaultAsset.getChainId(), defaultAsset.getAssetId());
            accountInfo.setBalance(balanceInfo.getBalance());
//            accountInfo.setConsensusLock(balanceInfo.getConsensusLock());
            accountInfo.setTimeLock(balanceInfo.getTimeLock());
        }
        accountInfo.setSymbol(ApiContext.defaultSymbol);
        return result.setResult(accountInfo);

    }

    @RpcMethod("getCoinRanking")
    public RpcResult getCoinRanking(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, pageNumber, pageSize;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        PageInfo<MiniAccountInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = accountService.getCoinRanking(pageNumber, pageSize, chainId);
        } else {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
        }
        return new RpcResult().setResult(pageInfo);
    }


    @RpcMethod("getAssetRanking")
    public RpcResult getAssetRanking(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId, assetChainId, assetId, pageNumber, pageSize;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            assetChainId = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is inValid");
        }
        try {
            assetId = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is inValid");
        }

        try {
            pageNumber = (int) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        PageInfo<MiniAccountInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            if (chainId == apiConfig.getChainId() && assetChainId == apiConfig.getChainId() && assetId == apiConfig.getAssetId()
                    && pageNumber == 1 && pageSize == 15) {
                pageInfo = ApiContext.miniAccountPageInfo;
            } else {
                pageInfo = accountLedgerService.getAssetRanking(chainId, assetChainId, assetId, pageNumber, pageSize);
            }
        } else {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
        }
        return new RpcResult().setResult(pageInfo);
    }


    @RpcMethod("getAccountFreezes")
    public RpcResult getAccountFreezes(List<Object> params) {
        VerifyUtils.verifyParams(params, 6);
        int chainId, assetChainId, assetId, pageNumber, pageSize;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            assetChainId = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is inValid");
        }
        try {
            assetId = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is inValid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            pageNumber = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(5);
        } catch (Exception e) {
            return RpcResult.paramError("[sortType] is inValid");
        }

        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        PageInfo<FreezeInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            Result<PageInfo<FreezeInfo>> result = WalletRpcHandler.getFreezeList(chainId, assetChainId, assetId, address, pageNumber, pageSize);
            if (result.isFailed()) {
                return RpcResult.failed(result);
            }
            pageInfo = result.getData();
            return RpcResult.success(pageInfo);
        } else {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
            return RpcResult.success(pageInfo);
        }
    }

    @RpcMethod("getAccountBalance")
    public RpcResult getAccountBalance(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, assetChainId, assetId;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            assetChainId = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is inValid");
        }
        try {
            assetId = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is inValid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.dataNotFound();
        }
        if (assetId <= 0) {
            AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
            assetId = defaultAsset.getAssetId();
        }
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, address, assetChainId, assetId);
        if (assetChainId == ApiContext.defaultChainId && assetId == ApiContext.defaultAssetId) {
            AccountInfo accountInfo = accountService.getAccountInfo(chainId, address);
            if (accountInfo != null) {
                balanceInfo.setConsensusLock(accountInfo.getConsensusLock());
            }
        }

        return RpcResult.success(balanceInfo);

    }

    @RpcMethod("getAccountsBalance")
    public RpcResult getAccountsBalance(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, assetChainId, assetId;
        String address;

        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            assetChainId = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is inValid");
        }
        try {
            assetId = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is inValid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.dataNotFound();
        }
        if (assetId <= 0) {
            AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
            assetId = defaultAsset.getAssetId();
        }

        String[] addressList = address.split(",");
        Map<String, BalanceInfo> balanceInfoList = new HashMap<>();
        for (int i = 0; i < addressList.length; i++) {
            address = addressList[i];
            BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, address, assetChainId, assetId);
            AccountInfo accountInfo = accountService.getAccountInfo(chainId, address);
            if (accountInfo != null) {
                balanceInfo.setConsensusLock(accountInfo.getConsensusLock());
            }
            balanceInfoList.put(address, balanceInfo);
        }
        return RpcResult.success(balanceInfoList);
    }


    @RpcMethod("isAliasUsable")
    public RpcResult isAliasUsable(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String alias;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            alias = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[alias] is inValid");
        }
        if (StringUtils.isBlank(alias)) {
            return RpcResult.paramError("[alias] is inValid");
        }

        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.dataNotFound();
        }

        Result result = WalletRpcHandler.isAliasUsable(chainId, alias);
        return RpcResult.success(result.getData());
    }

    @RpcMethod("getAccountLedgerList")
    public RpcResult getAccountLedgerList(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.dataNotFound();
        }
        List<AccountLedgerInfo> list = accountLedgerService.getAccountLedgerInfoList(chainId, address);
        for (AccountLedgerInfo ledgerInfo : list) {
            BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, address, ledgerInfo.getChainId(), ledgerInfo.getAssetId());
            ledgerInfo.setBalance(balanceInfo.getBalance());
            ledgerInfo.setTimeLock(balanceInfo.getTimeLock());
            ledgerInfo.setConsensusLock(balanceInfo.getConsensusLock());
            AssetInfo assetInfo = CacheManager.getAssetInfoMap().get(ledgerInfo.getAssetKey());
            if (assetInfo != null) {
                ledgerInfo.setSymbol(assetInfo.getSymbol());
                ledgerInfo.setDecimals(assetInfo.getDecimals());
            }
        }
        return RpcResult.success(list);
    }


    @RpcMethod("getAccountCrossLedgerList")
    public RpcResult getAccountCrossLedgerList(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.dataNotFound();
        }
        List<AccountLedgerInfo> list = accountLedgerService.getAccountCrossLedgerInfoList(chainId, address);
        for (AccountLedgerInfo ledgerInfo : list) {
            BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, address, ledgerInfo.getChainId(), ledgerInfo.getAssetId());
            ledgerInfo.setBalance(balanceInfo.getBalance());
            ledgerInfo.setTimeLock(balanceInfo.getTimeLock());
            ledgerInfo.setConsensusLock(balanceInfo.getConsensusLock());
            AssetInfo assetInfo = CacheManager.getAssetInfoMap().get(ledgerInfo.getAssetKey());
            if (assetInfo != null) {
                ledgerInfo.setSymbol(assetInfo.getSymbol());
                ledgerInfo.setDecimals(assetInfo.getDecimals());
            }
        }
        return RpcResult.success(list);

    }

    @RpcMethod("getAllAddressPrefix")
    public RpcResult getAllAddressPrefix(List<Object> params) {
        Result<List> result = WalletRpcHandler.getAllAddressPrefix();
        return RpcResult.success(result.getData());
    }

    @RpcMethod("getNRC20Snapshot")
    public RpcResult getNRC20Snapshot(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        PageInfo<AccountTokenInfo> pageInfo = tokenService.getContractTokens(chainId, address, 1, Integer.MAX_VALUE);
        return RpcResult.success(pageInfo.getList().stream().map(d -> Map.of("address", d.getAddress(), "balance", d.getBalance())).collect(Collectors.toList()));
    }


}
