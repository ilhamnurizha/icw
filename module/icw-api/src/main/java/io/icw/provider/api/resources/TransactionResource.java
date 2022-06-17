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

import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.block.BlockService;
import io.icw.base.api.provider.block.facade.BlockHeaderData;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.icw.provider.model.dto.TransactionDto;
import io.icw.provider.api.config.Config;
import io.icw.base.api.provider.Result;
import io.icw.core.constant.CommonCodeConstanst;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.rpc.model.Parameter;
import io.icw.core.rpc.model.Parameters;
import io.icw.core.rpc.model.ResponseData;
import io.icw.core.rpc.model.TypeDescriptor;
import io.icw.provider.model.ErrorData;
import io.icw.provider.model.RpcClientResult;
import io.icw.provider.rpctools.TransactionTools;
import io.icw.provider.utils.ResultUtil;
import io.icw.v2.model.annotation.Api;
import io.icw.v2.model.annotation.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author: PierreLuo
 * @date: 2019-06-30
 */
@Path("/api/tx")
@Component
@Api
public class TransactionResource {

    @Autowired
    Config config;
    @Autowired
    TransactionTools transactionTools;

    BlockService blockService = ServiceManager.get(BlockService.class);

    @GET
    @Path("/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "根据hash获取交易", order = 301)
    @Parameters({
            @Parameter(parameterName = "hash", requestType = @TypeDescriptor(value = String.class), parameterDes = "交易hash")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = TransactionDto.class))
    public RpcClientResult getTx(@PathParam("hash") String hash) {
        if (hash == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "hash is empty"));
        }
        Result<TransactionDto> result = transactionTools.getTx(config.getChainId(), hash);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            TransactionDto txDto = (TransactionDto) clientResult.getData();
            if (txDto.getBlockHeight() >= 0) {
                GetBlockHeaderByHeightReq req = new GetBlockHeaderByHeightReq(txDto.getBlockHeight());
                req.setChainId(config.getChainId());
                Result<BlockHeaderData> blockResult = blockService.getBlockHeaderByHeight(req);
                if (blockResult.isSuccess()) {
                    txDto.setBlockHash(blockResult.getData().getHash());
                }
            }
        }
        return clientResult;
    }
}
