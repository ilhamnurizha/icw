package io.icw.sdk.protocol.service.impl;

import io.icw.sdk.accountledger.model.Input;
import io.icw.sdk.accountledger.model.Output;
import io.icw.sdk.accountledger.model.Transaction;
import io.icw.sdk.core.contast.KernelErrorCode;
import io.icw.sdk.core.exception.NulsException;
import io.icw.sdk.core.model.Result;
import io.icw.sdk.core.utils.Log;
import io.icw.sdk.core.utils.NulsByteBuffer;
import io.icw.sdk.core.utils.RestFulUtils;
import io.icw.sdk.core.utils.StringUtils;
import io.icw.sdk.protocol.model.Block;
import io.icw.sdk.protocol.model.BlockHeader;
import io.icw.sdk.protocol.service.BlockService;

import java.util.*;

/**
 * @author: Charlie
 */
public class BlockServiceImpl implements BlockService {

    private static BlockService instance = new BlockServiceImpl();

    private BlockServiceImpl() {

    }

    public static BlockService getInstance() {
        return instance;
    }

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public Result getNewestBlockHeight() {
        Result result = restFul.get("/block/newest/height", null);
        if (result.isFailed()) {
            return result;
        }
        return result;
    }

    @Override
    public Result getNewestBlockHash() {
        Result result = restFul.get("/block/newest/hash", null);
        if (result.isFailed()) {
            return result;
        }
        return result;
    }

    @Override
    public Result getNewestBlockHeader() {
        Result result = restFul.get("/block/newest", null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map) result.getData();
        BlockHeader blockHeaderDto = new BlockHeader(map);
        return result.setData(blockHeaderDto);
    }

    @Override
    public Result getBlockHeader(int height) {
        if (height < 0) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = restFul.get("/block/header/height/" + height, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map) result.getData();
        BlockHeader blockHeaderDto = new BlockHeader(map);
        return result.setData(blockHeaderDto);
    }

    @Override
    public Result getBlockHeader(String hash) {
        if (StringUtils.isBlank(hash)) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = restFul.get("/block/header/hash/" + hash, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map) result.getData();
        BlockHeader blockHeaderDto = new BlockHeader(map);
        return result.setData(blockHeaderDto);
    }

    @Override
    public Result getBlock(int height) {
        if (height < 0) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = restFul.get("/block/height/" + height, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map) result.getData();
        return result.setData(assembleBlockDto(map));
    }

    @Override
    public Result getBlock(String hash) {
        if (StringUtils.isBlank(hash)) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = restFul.get("/block/hash/" + hash, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map) result.getData();
        return result.setData(assembleBlockDto(map));
    }

    @Override
    public Result getBlockWithBytes(String hash) {
        Map<String, Object> param = new HashMap<>();
        param.put("hash", hash);
        Result result = restFul.get("/block/bytes", param);
        if (result.isFailed()) {
            return result;
        }
        Map<String, String> resultMap = (Map<String, String>) result.getData();
        String blockHex = resultMap.get("value");
        byte[] data = Base64.getDecoder().decode(blockHex);

        io.icw.sdk.core.model.Block block = new io.icw.sdk.core.model.Block();
        try {
            block.parseWithVersion(new NulsByteBuffer(data));
            block.getHeader().setSize(block.size());
            result.setData(block);
        } catch (NulsException e) {
            Log.error(e);
            result = new Result(false, e.getErrorCode());
        }
        return result;
    }

    @Override
    public Result getBlockWithBytes(long height) {
        Map<String, Object> param = new HashMap<>();
        param.put("height", height);
        Result result = restFul.get("/block/bytes/height", param);
        if (result.isFailed()) {
            return result;
        }
        Map<String, String> resultMap = (Map<String, String>) result.getData();
        String blockHex = resultMap.get("value");
        byte[] data = Base64.getDecoder().decode(blockHex);
        io.icw.sdk.core.model.Block block = new io.icw.sdk.core.model.Block();
        try {
            block.parseWithVersion(new NulsByteBuffer(data));
            block.getHeader().setSize(block.size());
            result.setData(block);
        } catch (NulsException e) {
            Log.error(e);
            result = new Result(false, e.getErrorCode());
        }
        return result;
    }


    private Block assembleBlockDto(Map<String, Object> map) {
        List<Map<String, Object>> txMapList = (List<Map<String, Object>>) map.get("txList");
        List<Transaction> txList = new ArrayList<>();
        for (Map<String, Object> txMap : txMapList) {
            String txHash = (String) map.get("hash");
            //重新组装input
            List<Map<String, Object>> inputMaps = (List<Map<String, Object>>) txMap.get("inputs");
            List<Input> inputs = new ArrayList<>();
            for (Map<String, Object> inputMap : inputMaps) {
                Input inputDto = new Input(inputMap);
                inputs.add(inputDto);
            }
            txMap.put("inputs", inputs);
            //重新组装output
            List<Map<String, Object>> outputMaps = (List<Map<String, Object>>) txMap.get("outputs");
            List<Output> outputs = new ArrayList<>();
            for (int i = 0; i < outputMaps.size(); i++) {
                Output outputDto = new Output(outputMaps.get(i));
                outputDto.setTxHash(txHash);
                outputDto.setIndex(i);
                outputs.add(outputDto);
            }

            txMap.put("outputs", outputs);
            Transaction transactionDto = new Transaction(txMap);
            txList.add(transactionDto);
        }
        map.put("txList", txList);
        return new Block(map);
    }


}
