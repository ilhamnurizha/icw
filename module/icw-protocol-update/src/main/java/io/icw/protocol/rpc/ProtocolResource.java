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

package io.icw.protocol.rpc;

import com.google.common.collect.Maps;
import io.icw.base.RPCUtil;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.ProtocolVersion;
import io.icw.base.data.BlockExtendsData;
import io.icw.base.data.BlockHeader;
import io.icw.base.protocol.Protocol;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.crypto.HexUtil;
import io.icw.core.exception.NulsException;
import io.icw.core.log.logback.NulsLogger;
import io.icw.core.parse.JSONUtils;
import io.icw.core.rpc.cmd.BaseCmd;
import io.icw.core.rpc.info.Constants;
import io.icw.protocol.constant.CommandConstant;
import io.icw.protocol.manager.ContextManager;
import io.icw.protocol.model.ProtocolContext;
import io.icw.protocol.service.ProtocolService;
import io.icw.core.rpc.model.*;
import io.icw.core.rpc.model.message.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模块的对外接口类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午2:04
 */
@Component
public class ProtocolResource extends BaseCmd {
    @Autowired
    private ProtocolService service;

    /**
     * 获取当前主网版本信息
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = CommandConstant.GET_VERSION, version = 1.0, description = "get mainnet version")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象，包含三个属性", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "version", valueType = Short.class, description = "协议版本号"),
            @Key(name = "effectiveRatio", valueType = Byte.class, description = "每个统计区间内的最小生效比例"),
            @Key(name = "continuousIntervalCount", valueType = Short.class, description = "协议生效要满足的连续区间数")})
    )
    public Response getVersion(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        ProtocolContext context = ContextManager.getContext(chainId);
        ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
        ProtocolVersion localProtocolVersion = context.getLocalProtocolVersion();
        Map<String, ProtocolVersion> result = new HashMap<>();
        result.put("currentProtocolVersion", currentProtocolVersion);
        result.put("localProtocolVersion", localProtocolVersion);
        return success(result);
    }

    /**
     * 验证新收到区块的版本号是否正确
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = CommandConstant.CHECK_BLOCK_VERSION, version = 1.0, description = "check block version")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "extendsData", requestType = @TypeDescriptor(value = String.class), parameterDes = "BlockExtendsData序列化后的hex字符串")
    })
    @ResponseData(name = "返回值", description = "无返回值")
    public Response checkBlockVersion(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        String extendStr = map.get("extendsData").toString();
        BlockExtendsData extendsData = new BlockExtendsData(RPCUtil.decode(extendStr));

        ProtocolContext context = ContextManager.getContext(chainId);
        ProtocolVersion currentProtocol = context.getCurrentProtocolVersion();
        //收到的新区块和本地主网版本不一致，验证不通过
        if (currentProtocol.getVersion() != extendsData.getMainVersion()
        		&& currentProtocol.getVersion() != extendsData.getBlockVersion()) {
            NulsLogger logger = context.getLogger();
            logger.info("------block version error, mainVersion:" + currentProtocol.getVersion() + ",blockVersion:" + extendsData.getMainVersion());
            return failed("block version error");
        }
        return success();
    }

    /**
     * 保存区块
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = CommandConstant.SAVE_BLOCK, version = 1.0, description = "save block header")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "blockHeader", requestType = @TypeDescriptor(value = String.class), parameterDes = "区块头hex")
    })
    @ResponseData(name = "返回值", description = "无返回值")
    public Response save(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        String hex = map.get("blockHeader").toString();
        BlockHeader blockHeader = new BlockHeader();
        try {
            blockHeader.parse(new NulsByteBuffer(HexUtil.decode(hex)));
            if (service.save(chainId, blockHeader)) {
                return success();
            } else {
                return failed("protocol save failed!");
            }
        } catch (NulsException e) {
            return failed(e.getMessage());
        }
    }

    /**
     * 回滚区块
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = CommandConstant.ROLLBACK_BLOCK, version = 1.0, description = "rollback block header")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "blockHeader", requestType = @TypeDescriptor(value = String.class), parameterDes = "区块头hex")
    })
    @ResponseData(name = "返回值", description = "无返回值")
    public Response rollback(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        String hex = map.get("blockHeader").toString();
        BlockHeader blockHeader = new BlockHeader();
        try {
            blockHeader.parse(new NulsByteBuffer(HexUtil.decode(hex)));
            if (service.rollback(chainId, blockHeader)) {
                return success();
            } else {
                return failed("protocol rollback failed!");
            }
        } catch (NulsException e) {
            return failed(e.getMessage());
        }
    }

    /**
     * 接受各模块注册多版本配置
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = CommandConstant.REGISTER_PROTOCOL, version = 1.0, description = "register protocol")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "moduleCode", requestType = @TypeDescriptor(value = String.class), parameterDes = "模块标志"),
            @Parameter(parameterName = "list", requestType = @TypeDescriptor(value = List.class), parameterDes = "Protocol序列化后的hex字符串"),
    })
    @ResponseData(name = "返回值", description = "无返回值")
    public Response registerProtocol(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        ProtocolContext context = ContextManager.getContext(chainId);
        Map<Short, List<Map.Entry<String, Protocol>>> protocolMap = context.getProtocolMap();
        NulsLogger logger = context.getLogger();
        String moduleCode = map.get("moduleCode").toString();
        List list = (List) map.get("list");
        logger.info("--------------------registerProtocol---------------------------");
        logger.info("moduleCode-" + moduleCode);
//        JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for (Object o : list) {
            Map m = (Map) o;
            Protocol protocol = JSONUtils.map2pojo(m, Protocol.class);
            short version = protocol.getVersion();
            List<Map.Entry<String, Protocol>> protocolList = protocolMap.computeIfAbsent(version, k -> new ArrayList<>());
            protocolList.add(Maps.immutableEntry(moduleCode, protocol));
            logger.info("protocol-" + protocol);
        }
        return success();
    }

}
