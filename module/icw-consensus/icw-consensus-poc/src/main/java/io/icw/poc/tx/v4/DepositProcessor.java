package io.icw.poc.tx.v4;

import io.icw.base.basic.AddressTool;
import io.icw.base.data.*;
import io.icw.poc.model.bo.Chain;
import io.icw.poc.model.bo.tx.txdata.Deposit;
import io.icw.poc.model.po.DepositPo;
import io.icw.poc.rpc.call.CallMethodUtils;
import io.icw.poc.utils.manager.ChainManager;
import io.icw.poc.utils.manager.DepositManager;
import io.icw.poc.utils.validator.TxValidator;
import io.icw.base.protocol.TransactionProcessor;
import io.icw.base.signture.MultiSignTxSignature;
import io.icw.base.signture.TransactionSignature;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.poc.constant.ConsensusConstant;
import io.icw.poc.constant.ConsensusErrorCode;
import io.icw.poc.utils.LoggerUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 委托交易处理器
 *
 * @author tag
 * @date 2019/12/2
 */


@Component("DepositProcessorV4")
public class DepositProcessor implements TransactionProcessor {
    @Autowired
    private DepositManager depositManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;

    @Override
    public int getType() {
        return TxType.DEPOSIT;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        Map<String, Object> result = new HashMap<>(2);
        if (chain == null) {
            LoggerUtil.commonLog.error("Chains do not exist.");
            result.put("txList", txs);
            result.put("errorCode", ConsensusErrorCode.CHAIN_NOT_EXIST.getCode());
            return result;
        }
        List<Transaction> invalidTxList = new ArrayList<>();
        String errorCode = null;
        Set<NulsHash> invalidHashSet = txValidator.getInvalidAgentHash(txMap.get(TxType.RED_PUNISH), txMap.get(TxType.CONTRACT_STOP_AGENT), txMap.get(TxType.STOP_AGENT), chain);
        //各节点总委托金额
        Map<NulsHash, BigInteger> agentDepositTotalMap = new HashMap<>(16);
        try {
            Map<NulsHash, BigInteger> contractDepositMap = getContractDepositMap(chain, txMap.get(TxType.CONTRACT_DEPOSIT));
            if(contractDepositMap != null){
                agentDepositTotalMap = contractDepositMap;
            }
        }catch(NulsException e){
            chain.getLogger().error(e);
        }
        for (Transaction depositTx : txs) {
            try {
                Deposit deposit = new Deposit();
                deposit.parse(depositTx.getTxData(), 0);
                //验证签名及coinData地址
                if (!verifyV4(chain, depositTx, deposit.getAddress())) {
                    invalidTxList.add(depositTx);
                    continue;
                }
                if (!txValidator.validateTx(chain, depositTx)) {
                    invalidTxList.add(depositTx);
                    chain.getLogger().info("Delegated transaction verification failed");
                    continue;
                }
                if (invalidHashSet.contains(deposit.getAgentHash())) {
                    invalidTxList.add(depositTx);
                    chain.getLogger().info("Conflict between Intelligent Delegation Transaction and Red Card Transaction or Stop Node Transaction");
                    errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                }
                NulsHash agentHash = deposit.getAgentHash();
                BigInteger totalDeposit = BigInteger.ZERO;
                if (agentDepositTotalMap.containsKey(agentHash)) {
                    totalDeposit = agentDepositTotalMap.get(agentHash).add(deposit.getDeposit());
                    chain.getLogger().info(totalDeposit + ":::" + chain.getConfig().getCommissionMax());
                    if (totalDeposit.compareTo(chain.getConfig().getCommissionMax()) > 0) {
                    	Map networkInfo = CallMethodUtils.networkInfo(chainId);
                    	if (Long.valueOf(networkInfo.get("netBestHeight").toString()) - 100 > blockHeader.getHeight()) {
                    		agentDepositTotalMap.put(agentHash, totalDeposit);
                    	} else {
	                        chain.getLogger().info("Node delegation amount exceeds maximum delegation amount");
	                        throw new NulsException(ConsensusErrorCode.DEPOSIT_OVER_AMOUNT);
                    	}
                    } else {
                        agentDepositTotalMap.put(agentHash, totalDeposit);
                    }
                } else {
                    List<DepositPo> poList = txValidator.getDepositListByAgent(chain, deposit.getAgentHash());
                    for (DepositPo cd : poList) {
                        totalDeposit = totalDeposit.add(cd.getDeposit());
                    }
                    totalDeposit = totalDeposit.add(deposit.getDeposit());
                    agentDepositTotalMap.put(agentHash, totalDeposit);
                }
            } catch (NulsException e) {
                invalidTxList.add(depositTx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
            } catch (IOException io) {
                invalidTxList.add(depositTx);
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
        if (chain == null) {
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> commitSuccessList = new ArrayList<>();
        boolean commitResult = true;
        for (Transaction tx : txs) {
            try {
                if (depositManager.depositCommit(tx, blockHeader, chain)) {
                    commitSuccessList.add(tx);
                }
            } catch (NulsException e) {
                chain.getLogger().error("Failure to deposit transaction submission");
                chain.getLogger().error(e);
                commitResult = false;
            }
        }
        //回滚已提交成功的交易
        if (!commitResult) {
            for (Transaction rollbackTx : commitSuccessList) {
                try {
                    depositManager.depositRollBack(rollbackTx, chain);
                } catch (NulsException e) {
                    chain.getLogger().error("Failure to deposit transaction rollback");
                    chain.getLogger().error(e);
                }
            }
        }
        return commitResult;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> rollbackSuccessList = new ArrayList<>();
        boolean rollbackResult = true;
        for (Transaction tx : txs) {
            try {
                if (depositManager.depositRollBack(tx, chain)) {
                    rollbackSuccessList.add(tx);
                }
            } catch (NulsException e) {
                chain.getLogger().error("Failure to deposit transaction rollback");
                chain.getLogger().error(e);
                rollbackResult = false;
            }
        }
        //保存已回滚成功的交易
        if (!rollbackResult) {
            for (Transaction commitTx : rollbackSuccessList) {
                try {
                    depositManager.depositCommit(commitTx, blockHeader, chain);
                } catch (NulsException e) {
                    chain.getLogger().error("Failure to deposit transaction submission");
                    chain.getLogger().error(e);
                }
            }
        }
        return rollbackResult;
    }

    /**
     * 版本四新增的验证
     *
     * @param chain   链信息
     * @param tx      委托交易
     * @param creator 委托账户地址
     */
    private boolean verifyV4(Chain chain, Transaction tx, byte[] creator) throws NulsException {
        if (tx.getTransactionSignature() == null) {
            chain.getLogger().error("Unsigned Commission transaction");
            throw new NulsException(ConsensusErrorCode.TX_CREATOR_NOT_SIGNED);
        }

        byte[] signer;
        if(tx.isMultiSignTx()){
            MultiSignTxSignature signTxSignature = new MultiSignTxSignature();
            signTxSignature.parse(tx.getTransactionSignature(), 0);
            if (signTxSignature.getP2PHKSignatures() == null || signTxSignature.getP2PHKSignatures().isEmpty()
                    || signTxSignature.getPubKeyList() == null || signTxSignature.size() < signTxSignature.getM()) {
                throw new NulsException(ConsensusErrorCode.AGENT_CREATOR_NOT_SIGNED);
            }
            signer = AddressTool.getAddress(CallMethodUtils.createMultiSignAccount(chain.getConfig().getChainId(), signTxSignature));
        }else{
            TransactionSignature transactionSignature = new TransactionSignature();
            transactionSignature.parse(tx.getTransactionSignature(), 0);
            if (transactionSignature.getP2PHKSignatures() == null || transactionSignature.getP2PHKSignatures().isEmpty()) {
                throw new NulsException(ConsensusErrorCode.AGENT_CREATOR_NOT_SIGNED);
            }
            signer = AddressTool.getAddress(transactionSignature.getP2PHKSignatures().get(0).getPublicKey(), chain.getConfig().getChainId());
        }
        //签名者是否为节交易创建者
        if (!Arrays.equals(creator, signer)) {
            chain.getLogger().error("The signature of the entrusted transaction is not the signature of the entrusting party");
            throw new NulsException(ConsensusErrorCode.TX_CREATOR_NOT_SIGNED);
        }
        //验证from中地址与to中地址是否相同且为委托者
        CoinData coinData = tx.getCoinDataInstance();
        if (!Arrays.equals(creator, coinData.getFrom().get(0).getAddress()) || !Arrays.equals(creator, coinData.getTo().get(0).getAddress())) {
            chain.getLogger().error("From address or to address in coinData is not the principal address");
            throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
        }
        //验证委托资产是否为本链主资产
        for (CoinTo coinTo : coinData.getTo()){
            if(coinTo.getLockTime() == ConsensusConstant.CONSENSUS_LOCK_TIME &&
                    (coinTo.getAssetsChainId() != chain.getConfig().getAgentChainId() || coinTo.getAssetsId() != chain.getConfig().getAgentAssetId())){
                chain.getLogger().info("Entrusted assets are not consensus assets");
                throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
            }
        }
        return true;
    }

    /**
     * 获取智能合约委托交易金额
     * @param contractDepositList  智能合约委托交易列表
     * */
    private Map<NulsHash, BigInteger> getContractDepositMap(Chain chain, List<Transaction> contractDepositList) throws NulsException{
        if(contractDepositList == null || contractDepositList.size() == 0){
            return null;
        }
        Map<NulsHash, BigInteger> depositMap = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        for (Transaction tx : contractDepositList){
            Deposit deposit = new Deposit();
            deposit.parse(tx.getTxData(), 0);
            NulsHash agentHash = deposit.getAgentHash();
            BigInteger totalDeposit = BigInteger.ZERO;
            if (depositMap.containsKey(agentHash)) {
                totalDeposit = depositMap.get(agentHash).add(deposit.getDeposit());
                depositMap.put(agentHash, totalDeposit);
            } else {
                List<DepositPo> poList = txValidator.getDepositListByAgent(chain, deposit.getAgentHash());
                for (DepositPo cd : poList) {
                    totalDeposit = totalDeposit.add(cd.getDeposit());
                }
                totalDeposit = totalDeposit.add(deposit.getDeposit());
                depositMap.put(agentHash, totalDeposit);
            }
        }
        return depositMap;
    }
}
