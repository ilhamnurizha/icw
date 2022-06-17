/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.icw.block.utils;

import io.icw.block.manager.ContextManager;
import io.icw.block.model.ChainParameters;
import io.icw.block.storage.ParametersStorageService;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;

import java.util.List;

import static io.icw.block.BlockBootstrap.blockConfig;

/**
 * 配置加载器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 下午1:37
 */
@Component
public class ConfigLoader {

    @Autowired
    private static ParametersStorageService service;

    /**
     * 加载配置文件
     *
     * @throws Exception
     */
    public static void load() {
        List<ChainParameters> list = service.getList();
        if (list == null || list.isEmpty()) {
            loadDefault();
        } else {
            for (ChainParameters chainParameters : list) {
                ContextManager.init(chainParameters);
//                int chainId = chainParameters.getChainId();
//                String protocolConfigJson = service.getProtocolConfigJson(chainId);
//                ProtocolLoader.load(chainId, protocolConfigJson);
            }
        }
    }

    /**
     * 加载默认配置文件
     *
     * @throws Exception
     */
    private static void loadDefault() {
//        String json = IoUtils.read(PROTOCOL_CONFIG_FILE);
        int chainId = blockConfig.getChainId();
        ContextManager.init(blockConfig);
        service.save(blockConfig, chainId);
//        service.saveProtocolConfigJson(json, chainId);
    }

}
