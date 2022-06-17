package io.icw.txhander;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.icw.base.RPCUtil;
import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.core.constant.BaseConstant;
import io.icw.core.constant.CommonCodeConstanst;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.model.ObjectUtils;
import io.icw.core.model.StringUtils;
import io.icw.core.rpc.cmd.BaseCmd;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.CmdAnnotation;
import io.icw.core.rpc.model.Parameter;
import io.icw.core.rpc.model.message.Response;

@Component
public final class TransactionDispatcher extends BaseCmd {

    @FunctionalInterface
    public interface Handler {
        boolean call(int chainId, Transaction tx, BlockHeader blockHeader);
    }

    @Autowired
    PowProcessor powProcessor;

    private TransactionProcessor getProcessor(int txType) {
        if (txType == powProcessor.getType()) {
            return powProcessor;
        }
        throw new RuntimeException("不支持的txType：" + txType);
    }

    @CmdAnnotation(cmd = BaseConstant.TX_VALIDATOR, version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response txValidator(Map params) {
        List<String> finalInvalidTxs = new ArrayList<>();
        Map<String, List<String>> resultMap = new HashMap<>(2);
        handle(params,(chainId,tx,blockHeader)-> {
            if(!getProcessor(tx.getType()).validate(chainId, tx, blockHeader)){
                finalInvalidTxs.add(tx.getHash().toHex());
            }
            return true;
        });
        resultMap.put("list", finalInvalidTxs);
        return success(resultMap);
    }

    @CmdAnnotation(cmd = BaseConstant.TX_COMMIT, version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response txCommit(Map params) {
        return handle(params,(chainId,tx,blockHeader)-> getProcessor(tx.getType()).commit(chainId,tx,blockHeader));
    }

    @CmdAnnotation(cmd = BaseConstant.TX_ROLLBACK, version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response txRollback(Map params) {
        return handle(params,(chainId,tx,blockHeader)-> getProcessor(tx.getType()).rollback(chainId,tx,blockHeader));
    }

    private Response handle(Map params, Handler handler){
        ObjectUtils.canNotEmpty(params.get(Constants.CHAIN_ID), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("txList"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        String blockHeaderStr = (String) params.get("blockHeader");
        BlockHeader blockHeader = null;
        if (StringUtils.isNotBlank(blockHeaderStr)) {
            blockHeader = RPCUtil.getInstanceRpcStr(blockHeaderStr, BlockHeader.class);
        }
        int chainId = Integer.parseInt(params.get(Constants.CHAIN_ID).toString());
        List<String> txList = (List<String>) params.get("txList");
        for (String txStr : txList) {
//            Log.info(txStr);
            Transaction tx = RPCUtil.getInstanceRpcStr(txStr, Transaction.class);
            handler.call(chainId,tx,blockHeader);
        }
        Map<String, Boolean> resultMap = new HashMap<>(2);
        resultMap.put("value", true);
        return success(resultMap);
    }

}
