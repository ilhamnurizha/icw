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

package io.icw.block.storage;

import io.icw.base.data.Block;
import io.icw.base.data.po.BlockHeaderPo;
import io.icw.block.constant.StatusEnum;
import io.icw.block.manager.ContextManager;
import io.icw.block.test.BlockGenerator;
import io.icw.block.utils.BlockUtil;
import io.icw.block.utils.ConfigLoader;
import io.icw.core.rockdb.service.RocksDBService;
import io.icw.core.core.ioc.SpringLiteContext;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BlockStorageServiceImplTest {

    private static BlockHeaderPo header;
    private static final int CHAIN_ID = 2;
    private static BlockStorageService service;

    @BeforeClass
    public static void beforeClass() throws Exception {
        SpringLiteContext.init("io.icw.block", "io.icw.rpc.modulebootstrap", "io.icw.rpc.cmd");
        RocksDBService.init("../../../../data/block");
        ConfigLoader.load();
        ContextManager.getContext(CHAIN_ID).setStatus(StatusEnum.RUNNING);
        service = SpringLiteContext.getBean(BlockStorageService.class);
        RocksDBService.init("../../../../data/block");
        Block block = BlockGenerator.generate(null);
        header = BlockUtil.toBlockHeaderPo(block);
    }

    @Test
    public void save() {
        service.save(CHAIN_ID, header);
        assertNotNull(service.query(CHAIN_ID, header.getHeight()));
    }

    @Test
    public void remove() {
        service.save(CHAIN_ID, header);
        service.remove(CHAIN_ID, header.getHeight());
        assertNull(service.query(CHAIN_ID, header.getHeight()));
    }

}