package io.icw.poc.tx.v3;

import io.icw.base.basic.AddressTool;
import io.icw.base.data.BlockHeader;
import io.icw.base.data.CoinData;
import io.icw.base.data.NulsHash;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.TransactionProcessor;
import io.icw.base.signture.TransactionSignature;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.poc.model.bo.Chain;
import io.icw.poc.model.bo.tx.txdata.CancelDeposit;
import io.icw.poc.model.po.AgentPo;
import io.icw.poc.model.po.DepositPo;
import io.icw.poc.utils.manager.ChainManager;
import io.icw.poc.utils.manager.DepositManager;
import io.icw.poc.utils.validator.TxValidator;
import io.icw.poc.constant.ConsensusErrorCode;
import io.icw.poc.storage.AgentStorageService;
import io.icw.poc.storage.DepositStorageService;
import io.icw.poc.utils.LoggerUtil;

import java.io.IOException;
import java.util.*;

/**
 * 脱出共识交易处理器
 * @author tag
 * @date 2019/6/1
 */
@Component("WithdrawProcessorV3")
public class WithdrawProcessor implements TransactionProcessor {
    @Autowired
    private DepositManager depositManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;
    @Autowired
    private DepositStorageService depositStorageService;
    @Autowired
    private AgentStorageService agentStorageService;
    @Override
    public int getType() {
        return TxType.CANCEL_DEPOSIT;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        Map<String, Object> result = new HashMap<>(2);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            result.put("txList", txs);
            result.put("errorCode", ConsensusErrorCode.CHAIN_NOT_EXIST.getCode());
            return result;
        }
        List<Transaction> invalidTxList = new ArrayList<>();
        String errorCode = null;
        Set<NulsHash> hashSet = new HashSet<>();
        Set<NulsHash> invalidHashSet = txValidator.getInvalidAgentHash(txMap.get(TxType.RED_PUNISH),txMap.get(TxType.CONTRACT_STOP_AGENT),txMap.get(TxType.STOP_AGENT),chain);
        List<Transaction>contractWithdrawTxList = txMap.get(TxType.CONTRACT_CANCEL_DEPOSIT);
        if(contractWithdrawTxList != null && contractWithdrawTxList.size() > 0){
            for (Transaction contractWithdrawTx:contractWithdrawTxList) {
                try {
                    CancelDeposit cancelDeposit = new CancelDeposit();
                    cancelDeposit.parse(contractWithdrawTx.getTxData(), 0);
                    hashSet.add(cancelDeposit.getJoinTxHash());
                }catch (Exception e){
                    chain.getLogger().error(e);
                }
            }
        }
        for (Transaction withdrawTx:txs) {
            try {
                if(!txValidator.validateTx(chain, withdrawTx)){
                    invalidTxList.add(withdrawTx);
                    chain.getLogger().error("Intelligent contract withdrawal delegation transaction verification failed");
                    continue;
                }
                CancelDeposit cancelDeposit = new CancelDeposit();
                cancelDeposit.parse(withdrawTx.getTxData(), 0);
                DepositPo depositPo = depositStorageService.get(cancelDeposit.getJoinTxHash(), chainId);
                AgentPo agentPo = agentStorageService.get(depositPo.getAgentHash(), chainId);
                if (null == agentPo) {
                    invalidTxList.add(withdrawTx);
                    errorCode = ConsensusErrorCode.AGENT_NOT_EXIST.getCode();
                    continue;
                }
                //验证签名是否正确
                if (!verifyV3(chain, withdrawTx, depositPo.getAddress())) {
                    invalidTxList.add(withdrawTx);
                    continue;
                }

                if (invalidHashSet.contains(agentPo.getHash())) {
                    invalidTxList.add(withdrawTx);
                    errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                    continue;
                }
                /*
                 * 重复退出节点
                 * */
                if (!hashSet.add(cancelDeposit.getJoinTxHash())) {
                    invalidTxList.add(withdrawTx);
                    chain.getLogger().info("Repeated transactions");
                    errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                }
            }catch (NulsException e){
                invalidTxList.add(withdrawTx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
            }catch (IOException io){
                invalidTxList.add(withdrawTx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(io);
                errorCode = ConsensusErrorCode.SERIALIZE_ERROR.getCode();
            }
        }
        result.put("txList", invalidTxList);
        result.put("errorCode", errorCode);
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> commitSuccessList = new ArrayList<>();
        boolean commitResult = true;
        for (Transaction tx:txs) {
            try {
                if(depositManager.cancelDepositCommit(tx,blockHeader,chain)){
                    commitSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to withdraw transaction submission");
                chain.getLogger().error(e);
                commitResult = false;
            }
        }
        //回滚已提交成功的交易
        if(!commitResult){
            for (Transaction rollbackTx:commitSuccessList) {
                try {
                    depositManager.cancelDepositRollBack(rollbackTx, chain, blockHeader);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to withdraw transaction rollback");
                    chain.getLogger().error(e);
                }
            }
        }
        return commitResult;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> rollbackSuccessList = new ArrayList<>();
        boolean rollbackResult = true;
        for (Transaction tx:txs) {
            try {
                if(depositManager.cancelDepositRollBack(tx,chain,blockHeader)){
                    rollbackSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to withdraw transaction rollback");
                chain.getLogger().error(e);
                rollbackResult = false;
            }
        }
        //保存已回滚成功的交易
        if(!rollbackResult){
            for (Transaction commitTx:rollbackSuccessList) {
                try {
                    depositManager.cancelDepositCommit(commitTx, blockHeader, chain);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to withdraw transaction submission");
                    chain.getLogger().error(e);
                }
            }
        }
        return rollbackResult;
    }

    /**
     * 版本三新增的验证
     *
     * @param chain   链信息
     * @param tx      委托交易
     * @param creator 委托账户地址
     */
    private boolean verifyV3(Chain chain, Transaction tx, byte[] creator) throws NulsException {
        //验证签名是否为委托者签名
        TransactionSignature transactionSignature = new TransactionSignature();
        if (tx.getTransactionSignature() == null) {
            chain.getLogger().error("Unsigned Commission transaction");
            throw new NulsException(ConsensusErrorCode.TX_CREATOR_NOT_SIGNED);
        }
        transactionSignature.parse(tx.getTransactionSignature(), 0);
        if (transactionSignature.getP2PHKSignatures() == null || transactionSignature.getP2PHKSignatures().isEmpty()) {
            chain.getLogger().error("Unsigned Commission transaction");
            throw new NulsException(ConsensusErrorCode.TX_CREATOR_NOT_SIGNED);
        }
        if (!Arrays.equals(creator, AddressTool.getAddress(transactionSignature.getP2PHKSignatures().get(0).getPublicKey(), chain.getConfig().getChainId()))) {
            chain.getLogger().error("The signature of the entrusted transaction is not the signature of the entrusting party");
            throw new NulsException(ConsensusErrorCode.TX_CREATOR_NOT_SIGNED);
        }
        //验证from中地址与to中地址是否相同且为委托者
        CoinData coinData = tx.getCoinDataInstance();
        if (!Arrays.equals(creator, coinData.getFrom().get(0).getAddress()) || !Arrays.equals(creator, coinData.getTo().get(0).getAddress())) {
            chain.getLogger().error("From address or to address in coinData is not the principal address");
            throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
        }
        return true;
    }
}
