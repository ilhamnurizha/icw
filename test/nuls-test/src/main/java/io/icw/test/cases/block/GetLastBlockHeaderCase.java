package io.icw.test.cases.block;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.block.BlockService;
import io.icw.base.api.provider.block.facade.BlockHeaderData;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.icw.test.cases.TestFailException;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 13:40
 * @Description: 功能描述
 */
@Component
public class GetLastBlockHeaderCase extends BaseBlockCase<BlockHeaderData,Void> {

    BlockService blockService = ServiceManager.get(BlockService.class);

    @Override
    public String title() {
        return "获取最新区块头";
    }

    @Override
    public BlockHeaderData doTest(Void param, int depth) throws TestFailException {
        Result<BlockHeaderData> result = blockService.getBlockHeaderByLastHeight(new GetBlockHeaderByLastHeightReq());
        checkResultStatus(result);
        return result.getData();
    }

}
