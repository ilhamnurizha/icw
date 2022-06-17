package io.icw.test.cases.block;

import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.block.BlockService;
import io.icw.test.cases.BaseTestCase;


public abstract class BaseBlockCase<T,P> extends BaseTestCase<T,P> {

    BlockService blockService = ServiceManager.get(BlockService.class);

}
