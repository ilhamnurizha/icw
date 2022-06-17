package io.icw.base.api.provider.block;

import io.icw.base.api.provider.block.facade.BlockHeaderData;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.icw.base.api.provider.Result;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 09:30
 * @Description: block service
 */
public interface BlockService {

    Result<BlockHeaderData> getBlockHeaderByHash(GetBlockHeaderByHashReq req);

    Result<BlockHeaderData> getBlockHeaderByHeight(GetBlockHeaderByHeightReq req);

    Result<BlockHeaderData> getBlockHeaderByLastHeight(GetBlockHeaderByLastHeightReq req);


}
