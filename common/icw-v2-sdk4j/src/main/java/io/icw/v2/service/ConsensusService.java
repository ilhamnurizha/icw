package io.icw.v2.service;

import io.icw.v2.SDKContext;
import io.icw.v2.model.dto.*;
import io.icw.core.basic.Result;
import io.icw.core.constant.ErrorCode;
import io.icw.core.exception.NulsException;
import io.icw.v2.error.AccountErrorCode;
import io.icw.v2.util.*;

import java.util.HashMap;
import java.util.Map;

import static io.icw.v2.util.ValidateUtil.validateChainId;

public class ConsensusService {

    private ConsensusService() {

    }

    private static ConsensusService instance = new ConsensusService();

    public static ConsensusService getInstance() {
        return instance;
    }

    public Result createAgent(CreateAgentForm form) {
        validateChainId();

        try {
            CommonValidator.validateCreateAgentForm(form);
            Map<String, Object> map = new HashMap<>();
            map.put("agentAddress", form.getAgentAddress());
            map.put("packingAddress", form.getPackingAddress());
            map.put("rewardAddress", form.getRewardAddress());
            map.put("commissionRate", form.getCommissionRate());
            map.put("deposit", form.getDeposit());
            map.put("password", form.getPassword());
            RestFulResult restFulResult = RestFulUtil.post("api/consensus/agent", map);
            Result result;
            if (restFulResult.isSuccess()) {
                result = io.icw.core.basic.Result.getSuccess(restFulResult.getData());
            } else {
                ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
                result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
            }
            return result;
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        }
    }

    public Result stopAgent(StopAgentForm form) {
        validateChainId();

        try {
            CommonValidator.validateStopAgentForm(form);

            Map<String, Object> map = new HashMap<>();
            map.put("address", form.getAgentAddress());
            map.put("password", form.getPassword());

            RestFulResult restFulResult = RestFulUtil.post("api/consensus/agent/stop", map);
            Result result;
            if (restFulResult.isSuccess()) {
                result = io.icw.core.basic.Result.getSuccess(restFulResult.getData());
            } else {
                ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
                result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
            }
            return result;
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        }
    }

    public Result depositToAgent(DepositForm form) {
        validateChainId();

        try {
            CommonValidator.validateDepositForm(form);
            Map<String, Object> map = new HashMap<>();
            map.put("address", form.getAddress());
            map.put("agentHash", form.getAgentHash());
            map.put("deposit", form.getDeposit());
            map.put("password", form.getPassword());

            RestFulResult restFulResult = RestFulUtil.post("api/consensus/deposit", map);
            Result result;
            if (restFulResult.isSuccess()) {
                result = io.icw.core.basic.Result.getSuccess(restFulResult.getData());
            } else {
                ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
                result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
            }
            return result;
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        }
    }

    public Result withdraw(WithdrawForm form) {
        validateChainId();

        try {
            CommonValidator.validateWithDrawForm(form);
            Map<String, Object> map = new HashMap<>();
            map.put("address", form.getAddress());
            map.put("txHash", form.getTxHash());
            map.put("password", form.getPassword());

            RestFulResult restFulResult = RestFulUtil.post("api/consensus/withdraw", map);
            Result result;
            if (restFulResult.isSuccess()) {
                result = io.icw.core.basic.Result.getSuccess(restFulResult.getData());
            } else {
                ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
                result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
            }
            return result;
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        }
    }

    public Result getDepositList(String agentHash) {
        if (!ValidateUtil.validHash(agentHash)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        RestFulResult restFulResult = RestFulUtil.getList("api/consensus/list/deposit/" + agentHash, null);
        Result result;
        if (restFulResult.isSuccess()) {
            result = io.icw.core.basic.Result.getSuccess(restFulResult.getData());
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }

    public Result getConsensusNodes(int pageNumber, int pageSize, int type) {
        int chainId = SDKContext.main_chain_id;
        RpcResult rpcResult =
                JsonRpcUtil.request("getConsensusNodes", ListUtil.of(chainId, pageNumber, pageSize, type));
        RpcResultError rpcResultError = rpcResult.getError();
        if (rpcResultError != null) {
            return Result.getFailed(ErrorCode.init(rpcResultError.getCode())).setMsg(rpcResultError.getMessage());
        }
        return Result.getSuccess(rpcResult.getResult());
    }
}
