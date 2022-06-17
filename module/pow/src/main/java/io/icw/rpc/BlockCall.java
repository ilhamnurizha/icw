/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.icw.rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.icw.base.RPCUtil;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.data.Block;
import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.core.constant.TxStatusEnum;
import io.icw.core.crypto.HexUtil;
import io.icw.core.exception.NulsException;
import io.icw.core.exception.NulsRuntimeException;
import io.icw.core.log.Log;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.ModuleE;

public class BlockCall implements CallRpc {

    public static long getLatestHeight(int chainId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            Map resultMap = (Map) CallHelper.request(ModuleE.BL.abbr, "latestHeight", params);
            return Long.parseLong(resultMap.get("value").toString());
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public static BlockHeader getLatestBlockHeader(int chainId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            Map resultMap = (Map) CallHelper.request(ModuleE.BL.abbr, "latestBlockHeader", params);
            BlockHeader header = new BlockHeader();
            header.parse(RPCUtil.decode((String) resultMap.get("value")), 0);
            return header;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public static BlockHeader getBlockHeader(int chainId, long height) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("height", height);
            Map resultMap = (Map) CallHelper.request(ModuleE.BL.abbr, "getBlockHeaderByHeight", params);
            BlockHeader header = new BlockHeader();
            header.parse(RPCUtil.decode((String) resultMap.get("value")), 0);
            return header;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public static BlockHeader getBlockHeader(int chainId, String hash) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("hash", hash);
            Map resultMap = (Map) CallHelper.request(ModuleE.BL.abbr, "getBlockHeaderByHash", params);
            BlockHeader header = new BlockHeader();
            header.parse(RPCUtil.decode((String) resultMap.get("value")), 0);
            return header;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
    
    public Block getBlockByHeight(int chainId, long height) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("chainId", chainId);
        param.put("height", height);
        try {
            Block block = callRpc(ModuleE.BL.name, "getBlockByHeight", param, (Function<Map, Block>) res -> {
                if (res == null || res.isEmpty()) {
                    return null;
                }
                Block _block = new Block();
                try {
                    _block.parse(new NulsByteBuffer(HexUtil.decode((String) res.get("value"))));
                    for(Transaction tx : _block.getTxs()) {
                        tx.setStatus(TxStatusEnum.CONFIRMED);
                    }
                } catch (NulsException e) {
                    Log.error(e);
                    return null;
                }
                return _block;
            });
            return block;
        } catch (NulsRuntimeException e) {
            return null;
        }
    }
}
