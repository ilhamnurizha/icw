package io.icw.crosschain.nuls.rpc.callback;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.icw.crosschain.nuls.servive.BlockService;
import io.icw.crosschain.nuls.utils.LoggerUtil;
import io.icw.core.rpc.invoke.BaseInvoke;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.parse.JSONUtils;

import java.util.HashMap;

/**
 * 接收最新区块回调信息
 * @author tag
 * @date 2019/4/25
 * */
public class NewBlockHeightInvoke extends BaseInvoke {

    private BlockService blockService = SpringLiteContext.getBean(BlockService.class);
    @Override
    public void callBack(Response response) {
        try {
            LoggerUtil.commonLog.info("收到区块高度更新消息:{}", JSONUtils.obj2json(response));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        HashMap result = ((HashMap) response.getResponseData());
        blockService.newBlockHeight(result);
    }
}
