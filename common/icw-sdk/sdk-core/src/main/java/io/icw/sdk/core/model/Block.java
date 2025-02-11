/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.icw.sdk.core.model;

import io.icw.sdk.core.contast.KernelErrorCode;
import io.icw.sdk.core.exception.NulsException;
import io.icw.sdk.core.exception.NulsRuntimeException;
import io.icw.sdk.core.model.transaction.Transaction;
import io.icw.sdk.core.utils.NulsByteBuffer;
import io.icw.sdk.core.utils.NulsOutputStreamBuffer;
import io.icw.sdk.core.utils.TransactionTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author win10
 */
public class Block extends BaseNulsData implements Cloneable {

    private BlockHeader header;
    private List<Transaction> txs;

    @Override
    public int size() {
        int size = header.size();
        for (Transaction tx : txs) {
            size += tx.size();
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        header.serializeToStream(stream);
        for (Transaction tx : txs) {
            stream.write(tx.serialize());
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        header = new BlockHeader();
        header.parse(byteBuffer);
        try {
            txs = TransactionTool.getInstances(byteBuffer, header.getTxCount());
        } catch (Exception e) {
            throw new NulsRuntimeException(KernelErrorCode.DESERIALIZE_ERROR);
        }
        for (Transaction tx : txs) {
            tx.setBlockHeight(header.getHeight());
        }
    }

    public void parseWithVersion(NulsByteBuffer byteBuffer) throws NulsException {
        header = new BlockHeader();
        header.parse(byteBuffer);

        try {
            BlockExtendsData extendsData = new BlockExtendsData(header.getExtend());
            if (extendsData.getMainVersion() == null) {
                txs = TransactionTool.getInstancesWithVersion(byteBuffer, header.getTxCount(), 1);
            } else {
                txs = TransactionTool.getInstancesWithVersion(byteBuffer, header.getTxCount(), extendsData.getMainVersion());
            }

        } catch (Exception e) {
            throw new NulsRuntimeException(KernelErrorCode.DESERIALIZE_ERROR);
        }
        for (Transaction tx : txs) {
            tx.setBlockHeight(header.getHeight());
        }
    }

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }

    public BlockHeader getHeader() {
        return header;
    }

    public void setHeader(BlockHeader header) {
        this.header = header;
    }

    //    /**
//     * 从交易列表中循环取出所有的交易hash，顺序和交易列表保持一致
//     * Loop through the list of trades to remove all of the trading hash, in the same order as the list of transactions.
//     */
    public List<NulsDigestData> getTxHashList() {
        List<NulsDigestData> list = new ArrayList<>();
        for (Transaction tx : txs) {
            if (null == tx) {
                continue;
            }
            list.add(tx.getHash());
        }
        return list;
    }

}
