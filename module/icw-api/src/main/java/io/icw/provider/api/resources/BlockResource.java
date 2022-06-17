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
package io.icw.provider.api.resources;

import io.icw.provider.model.dto.block.BlockDto;
import io.icw.provider.model.dto.block.BlockHeaderDto;
import io.icw.provider.api.config.Config;
import io.icw.provider.api.config.Context;
import io.icw.provider.api.manager.BeanCopierManager;
import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.block.BlockService;
import io.icw.base.api.provider.block.facade.BlockHeaderData;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.icw.base.data.Block;
import io.icw.core.constant.CommonCodeConstanst;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.core.rpc.model.Parameter;
import io.icw.core.rpc.model.Parameters;
import io.icw.core.rpc.model.ResponseData;
import io.icw.core.rpc.model.TypeDescriptor;
import io.icw.provider.model.ErrorData;
import io.icw.provider.model.RpcClientResult;
import io.icw.provider.rpctools.BlockTools;
import io.icw.provider.utils.Log;
import io.icw.provider.utils.ResultUtil;
import io.icw.v2.model.annotation.Api;
import io.icw.v2.model.annotation.ApiOperation;
import io.icw.v2.util.ValidateUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author: PierreLuo
 * @date: 2019-06-27
 */
@Path("/api/block")
@Component
@Api
public class BlockResource {

    BlockService blockService = ServiceManager.get(BlockService.class);
    @Autowired
    private Config config;
    @Autowired
    BlockTools blockTools;

    @GET
    @Path("/header/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "根据区块高度查询区块头", order = 201)
    @Parameters({
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = Long.class), parameterDes = "区块高度")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcClientResult getBlockHeaderByHeight(@PathParam("height") Long height) {
        if (height == null || height < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "height is invalid"));
        }
        GetBlockHeaderByHeightReq req = new GetBlockHeaderByHeightReq(height);
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByHeight(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess() && clientResult.getData() != null) {
            BlockHeaderDto dto = new BlockHeaderDto();
            Object data = clientResult.getData();
            BeanCopierManager.beanCopier(data, dto);
            clientResult.setData(dto);
        }
        return clientResult;
    }

    @GET
    @Path("/header/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "根据区块hash查询区块头", order = 202)
    @Parameters({
            @Parameter(parameterName = "hash", parameterDes = "区块hash")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcClientResult getBlockHeaderByHash(@PathParam("hash") String hash) {
        if (hash == null || !ValidateUtil.validHash(hash)) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "hash is invalid"));
        }
        GetBlockHeaderByHashReq req = new GetBlockHeaderByHashReq(hash);
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByHash(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess() && clientResult.getData() != null) {
            BlockHeaderDto dto = new BlockHeaderDto();
            Object data = clientResult.getData();
            BeanCopierManager.beanCopier(data, dto);
            clientResult.setData(dto);
        }
        return clientResult;
    }

    @GET
    @Path("/header/newest")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "查询最新区块头信息", order = 203)
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcClientResult getBestBlockHeader() {
        GetBlockHeaderByLastHeightReq req = new GetBlockHeaderByLastHeightReq();
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByLastHeight(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            BlockHeaderDto dto = new BlockHeaderDto();
            Object data = clientResult.getData();
            BeanCopierManager.beanCopier(data, dto);
            clientResult.setData(dto);
        }
        return clientResult;
    }

    @GET
    @Path("/newest")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "查询最新区块", order = 204, detailDesc = "包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用")
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcClientResult getBestBlock() {
        Result<Block> result = blockTools.getBestBlock(Context.getChainId());
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            try {
                clientResult.setData(new BlockDto((Block) clientResult.getData()));
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionRpcClientResult(e);
            }
        }
        return clientResult;
    }

    @GET
    @Path("/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "根据区块高度查询区块", order = 205, detailDesc = "包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用")
    @Parameters({
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = Long.class), parameterDes = "区块高度")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcClientResult getBlockByHeight(@PathParam("height") Long height) {
        if (height == null || height < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "height is invalid"));
        }
        Result<Block> result = blockTools.getBlockByHeight(config.getChainId(), height);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            try {
                if (clientResult.getData() != null) {
                    clientResult.setData(new BlockDto((Block) clientResult.getData()));
                }
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionRpcClientResult(e);
            }
        }
        return clientResult;
    }

    @GET
    @Path("/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "根据区块hash查询区块", order = 206, detailDesc = "包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用")
    @Parameters({
            @Parameter(parameterName = "hash", parameterDes = "区块hash")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcClientResult getBlockByHash(@PathParam("hash") String hash) {
        if (hash == null || !ValidateUtil.validHash(hash)) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "hash is invalid"));
        }
        Result<Block> result = blockTools.getBlockByHash(config.getChainId(), hash);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            try {
                if (clientResult.getData() != null) {
                    clientResult.setData(new BlockDto((Block) clientResult.getData()));
                }
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionRpcClientResult(e);
            }
        }
        return clientResult;
    }

    @GET
    @Path("/serialization/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "根据区块高度查询区块序列化字符串", order = 207, detailDesc = "包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用")
    @Parameters({
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = Long.class), parameterDes = "区块高度")
    })
    @ResponseData(name = "返回值", description = "返回区块序列化后的HEX字符串", responseType = @TypeDescriptor(value = String.class))
    public RpcClientResult getBlockSerializationByHeight(@PathParam("height") Long height) {
        if (height == null || height < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "height is invalid"));
        }
        Result<String> result = blockTools.getBlockSerializationByHeight(config.getChainId(), height);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }

    @GET
    @Path("/serialization/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "根据区块hash查询区块序列化字符串", order = 208, detailDesc = "包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用")
    @Parameters({
            @Parameter(parameterName = "hash", parameterDes = "区块hash")
    })
    @ResponseData(name = "返回值", description = "返回区块序列化后的HEX字符串", responseType = @TypeDescriptor(value = String.class))
    public RpcClientResult getBlockSerializationByHash(@PathParam("hash") String hash) {
        if (hash == null || !ValidateUtil.validHash(hash)) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "hash is invalid"));
        }
        Result<String> result = blockTools.getBlockSerializationByHash(config.getChainId(), hash);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }
}
