package io.icw.poc.utils.validator;

import io.icw.base.basic.AddressTool;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.data.*;
import io.icw.poc.model.bo.round.MeetingMember;
import io.icw.poc.model.bo.round.MeetingRound;
import io.icw.poc.model.bo.round.RoundValidResult;
import io.icw.poc.model.bo.tx.txdata.Agent;
import io.icw.poc.model.bo.tx.txdata.RedPunishData;
import io.icw.poc.utils.enumeration.PunishReasonEnum;
import io.icw.poc.utils.manager.PunishManager;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.core.model.DoubleUtils;
import io.icw.core.rpc.util.NulsDateUtils;
import io.icw.poc.constant.ConsensusConstant;
import io.icw.poc.constant.ConsensusErrorCode;
import io.icw.poc.model.bo.Chain;
import io.icw.poc.utils.compare.CoinFromComparator;
import io.icw.poc.utils.compare.CoinToComparator;
import io.icw.poc.utils.manager.CoinDataManager;
import io.icw.poc.utils.manager.ConsensusManager;
import io.icw.poc.utils.manager.RoundManager;

import java.io.IOException;
import java.util.*;

/**
 * 区块验证工具类
 * Block Verification Tool Class
 *
 * @author tag
 * 2018/11/30
 */
@Component
public class BlockValidator {
    @Autowired
    private RoundManager roundManager;
    @Autowired
    private PunishManager punishManager;
    @Autowired
    private ConsensusManager consensusManager;
    @Autowired
    private CoinDataManager coinDataManager;

    /**
     * 区块头验证
     * Block verification
     *
     * @param isDownload block status
     * @param chain      chain info
     * @param block      block info
     */
    public void validate(boolean isDownload, Chain chain, Block block) throws Exception {
        BlockHeader blockHeader = block.getHeader();
        //验证梅克尔哈希]
        if (!blockHeader.getMerkleHash().equals(NulsHash.calcMerkleHash(block.getTxHashList()))) {
            throw new NulsException(ConsensusErrorCode.MERKEL_HASH_ERROR);
        }
        //区块头签名验证
        if (blockHeader.getBlockSignature().verifySignature(blockHeader.getHash()).isFailed()) {
            chain.getLogger().error("Block Header Verification Error!");
            throw new NulsException(ConsensusErrorCode.SIGNATURE_ERROR);
        }
        if (block.getHeader().getTime() - 12 > NulsDateUtils.getCurrentTimeSeconds()) {
            chain.getLogger().error("There is a big difference between the block time and the actual time!");
            throw new NulsException(ConsensusErrorCode.ERROR_UNLOCK_TIME);
        }
        RoundValidResult roundValidResult;
        String blockHeaderHash = blockHeader.getHash().toHex();
        try {
            roundValidResult = roundValidate(isDownload, chain, blockHeader, blockHeaderHash);
        } catch (Exception e) {
            throw new NulsException(e);
        }
        MeetingRound currentRound = roundValidResult.getRound();
        BlockExtendsData extendsData = blockHeader.getExtendsData();
        MeetingMember member = currentRound.getMember(extendsData.getPackingIndexOfRound());
        boolean validResult = punishValidate(block, currentRound, member, chain, blockHeaderHash);
        if (!validResult) {
        	currentRound = roundManager.getRound(chain, extendsData, false);
        	validResult = punishValidate(block, currentRound, member, chain, blockHeaderHash);
        }
        if (!validResult) {
            if (roundValidResult.isValidResult()) {
                roundManager.rollBackRound(chain, currentRound.getIndex());
            }
            throw new NulsException(ConsensusErrorCode.BLOCK_PUNISH_VALID_ERROR);
        }
        validResult = coinBaseValidate(block, currentRound, member, chain, blockHeaderHash, extendsData);
        if (!validResult) {
            if (roundValidResult.isValidResult()) {
                roundManager.rollBackRound(chain, currentRound.getIndex());
            }
            throw new NulsException(ConsensusErrorCode.BLOCK_COINBASE_VALID_ERROR);
        }
    }

    /**
     * 区块轮次验证
     * Block round validation
     *
     * @param isDownload  block status 0同步中  1接收最新区块
     * @param chain       chain info
     * @param blockHeader block header info
     */
    private RoundValidResult roundValidate(boolean isDownload, Chain chain, BlockHeader blockHeader, String blockHeaderHash) throws Exception {
        BlockExtendsData extendsData = blockHeader.getExtendsData();
        BlockHeader bestBlockHeader = chain.getNewestHeader();
        BlockExtendsData bestExtendsData = bestBlockHeader.getExtendsData();

        RoundValidResult roundValidResult = new RoundValidResult();

      /*
      该区块为本地最新区块之前的区块
      * */
        boolean isBeforeBlock = extendsData.getRoundIndex() < bestExtendsData.getRoundIndex() || (extendsData.getRoundIndex() == bestExtendsData.getRoundIndex() && extendsData.getPackingIndexOfRound() <= bestExtendsData.getPackingIndexOfRound());
        if (isBeforeBlock) {
            chain.getLogger().error("new block roundData error, block height : " + blockHeader.getHeight() + " , hash :" + blockHeaderHash);
            throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
        }
        if (chain.getNewestHeader().getHeight() == 0) {
            chain.getRoundList().clear();
        }
        MeetingRound currentRound = roundManager.getCurrentRound(chain);
        boolean hasChangeRound = false;

        if (currentRound == null || extendsData.getRoundIndex() < currentRound.getIndex()) {
            MeetingRound round = roundManager.getRoundByIndex(chain, extendsData.getRoundIndex());
            if (round != null) {
                currentRound = round;
            } else {
                currentRound = roundManager.getRound(chain, extendsData, false);
            }
            if (chain.getRoundList().isEmpty()) {
                hasChangeRound = true;
            }
        } else if (extendsData.getRoundIndex() > currentRound.getIndex()) {
            if (extendsData.getRoundStartTime() < currentRound.getEndTime()) {
                chain.getLogger().error("block height " + blockHeader.getHeight() + " round index and start time not match! hash :" + blockHeaderHash);
                throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
            }
            if (extendsData.getRoundStartTime() > NulsDateUtils.getCurrentTimeSeconds() + chain.getConfig().getPackingInterval()) {
                chain.getLogger().error("block height " + blockHeader.getHeight() + " round startTime is error, greater than current time! hash :" + blockHeaderHash);
                throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
            }
            if (extendsData.getRoundStartTime() + (extendsData.getPackingIndexOfRound() - 1) * chain.getConfig().getPackingInterval() > NulsDateUtils.getCurrentTimeSeconds() + chain.getConfig().getPackingInterval()) {
                chain.getLogger().error("block height " + blockHeader.getHeight() + " is the block of the future and received in advance! hash :" + blockHeaderHash);
                throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
            }
            MeetingRound tempRound = roundManager.getRound(chain, extendsData, !isDownload);
            if (tempRound.getIndex() > currentRound.getIndex()) {
                tempRound.setPreRound(currentRound);
                hasChangeRound = true;
            }
            currentRound = tempRound;
        }
        if (extendsData.getRoundIndex() != currentRound.getIndex() || extendsData.getRoundStartTime() != currentRound.getStartTime()) {
            chain.getLogger().error("block height " + blockHeader.getHeight() + " round startTime is error! hash :" + blockHeaderHash);
            throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
        }
        
//        chain.getLogger().error(extendsData.toString());
//        chain.getLogger().error(currentRound.toString());
//        chain.getLogger().error(String.valueOf(extendsData.getConsensusMemberCount()));
//        chain.getLogger().error(String.valueOf(currentRound.getMemberCount()));
//        chain.getLogger().error(String.valueOf(extendsData.getConsensusMemberCount() != currentRound.getMemberCount()));
        
        if (extendsData.getConsensusMemberCount() != currentRound.getMemberCount()) {
            chain.getLogger().error("block height " + blockHeader.getHeight() + " packager count is error! hash :" + blockHeaderHash);
            throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
        }
        // 验证打包人是否正确
        MeetingMember member = currentRound.getMember(extendsData.getPackingIndexOfRound());
        if (!Arrays.equals(member.getAgent().getPackingAddress(), blockHeader.getPackingAddress(chain.getConfig().getChainId()))) {
            chain.getLogger().error("block height " + blockHeader.getHeight() + " packager error! hash :" + blockHeaderHash);
            throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
        }
        if (member.getPackEndTime() != blockHeader.getTime()) {
            chain.getLogger().error("block height " + blockHeader.getHeight() + " time error! hash :" + blockHeaderHash);
            throw new NulsException(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
        }
        if (hasChangeRound) {
            roundManager.addRound(chain, currentRound);
            roundValidResult.setValidResult(true);
        }
        roundValidResult.setRound(currentRound);
        return roundValidResult;
    }

    /**
     * 区块惩罚交易验证
     * Block Penalty Trading Verification
     *
     * @param block        block info
     * @param currentRound Block round information
     * @param member       Node packing information
     * @param chain        chain info
     */
    private boolean punishValidate(Block block, MeetingRound currentRound, MeetingMember member, Chain chain, String blockHeaderHash) throws NulsException {
        List<Transaction> txs = block.getTxs();
        List<Transaction> redPunishTxList = new ArrayList<>();
        Transaction yellowPunishTx = null;
        Transaction tx;
      /*
      检查区块交中是否存在多个黄牌交易
      Check whether there are multiple yellow trades in block handover
      */
        for (int index = 1; index < txs.size(); index++) {
            tx = txs.get(index);
            if (tx.getType() == TxType.COIN_BASE) {
                chain.getLogger().error("Coinbase transaction more than one! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
                return false;
            }
            if (tx.getType() == TxType.YELLOW_PUNISH) {
                if (yellowPunishTx == null) {
                    yellowPunishTx = tx;
                } else {
                    chain.getLogger().error("Yellow punish transaction more than one! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
                    return false;
                }
            } else if (tx.getType() == TxType.RED_PUNISH) {
                redPunishTxList.add(tx);
            }
        }
      /*
      校验区块交易中的黄牌交易是否正确
      Check the correctness of yellow card trading in block trading
      */
        try {
            Transaction newYellowPunishTX = punishManager.createYellowPunishTx(chain, chain.getNewestHeader(), member, currentRound);
            boolean isMatch = (yellowPunishTx == null && newYellowPunishTX == null) || (yellowPunishTx != null && newYellowPunishTX != null);
            if (!isMatch) {
                chain.getLogger().error("The yellow punish tx is wrong! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
                return false;
            } else if (yellowPunishTx != null && !yellowPunishTx.getHash().equals(newYellowPunishTX.getHash())) {
                chain.getLogger().error("The yellow punish tx's hash is wrong! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
                return false;
            }
        } catch (Exception e) {
            chain.getLogger().error("The tx's wrong! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash, e);
            return false;
        }
      /*
      区块中红牌交易验证
      Verification of Red Card Trading in Blocks
      */
        if (!redPunishTxList.isEmpty()) {
            Set<String> punishAddress = new HashSet<>();
            if (null != yellowPunishTx) {
                io.icw.poc.model.bo.tx.txdata.YellowPunishData yellowPunishData = new io.icw.poc.model.bo.tx.txdata.YellowPunishData();
                yellowPunishData.parse(yellowPunishTx.getTxData(), 0);
                List<byte[]> addressList = yellowPunishData.getAddressList();
                for (byte[] address : addressList) {
                    MeetingMember item = currentRound.getMemberByAgentAddress(address);
                    if (null == item) {
                        item = currentRound.getPreRound().getMemberByAgentAddress(address);
                    }
                    if (DoubleUtils.compare(item.getAgent().getRealCreditVal(), ConsensusConstant.RED_PUNISH_CREDIT_VAL) <= 0) {
                        punishAddress.add(AddressTool.getStringAddressByBytes(item.getAgent().getAgentAddress()));
                    }
                }
            }
            int countOfTooMuchYP = 0;
            for (Transaction redTx : redPunishTxList) {
                io.icw.poc.model.bo.tx.txdata.RedPunishData data = new io.icw.poc.model.bo.tx.txdata.RedPunishData();
                data.parse(redTx.getTxData(), 0);
                if (data.getReasonCode() == PunishReasonEnum.TOO_MUCH_YELLOW_PUNISH.getCode()) {
                    countOfTooMuchYP++;
                    if (!punishAddress.contains(AddressTool.getStringAddressByBytes(data.getAddress()))) {
                        chain.getLogger().error("There is a wrong red punish tx!" + blockHeaderHash);
                        return false;
                    }
                    if (redTx.getTime() != block.getHeader().getTime()) {
                        chain.getLogger().error("red punish CoinData & TX time is wrong! " + blockHeaderHash);
                        return false;
                    }
                }
                boolean result = verifyRedPunish(chain, redTx);
                if (!result) {
                    return false;
                }
            }
            if (countOfTooMuchYP != punishAddress.size()) {
                chain.getLogger().error("There is a wrong red punish tx!" + blockHeaderHash);
                return false;
            }
        }
        return true;
    }


    /**
     * 红牌交易验证
     *
     * @param chain chain info
     * @param tx    transaction info
     */
    private boolean verifyRedPunish(Chain chain, Transaction tx) throws NulsException {
        io.icw.poc.model.bo.tx.txdata.RedPunishData punishData = new io.icw.poc.model.bo.tx.txdata.RedPunishData();
        punishData.parse(tx.getTxData(), 0);
      /*
      红牌交易类型为连续分叉
      The type of red card transaction is continuous bifurcation
      */
        if (punishData.getReasonCode() == PunishReasonEnum.BIFURCATION.getCode()) {
            NulsByteBuffer byteBuffer = new NulsByteBuffer(punishData.getEvidence());
            long[] roundIndex = new long[ConsensusConstant.REDPUNISH_BIFURCATION];
            for (int i = 0; i < ConsensusConstant.REDPUNISH_BIFURCATION && !byteBuffer.isFinished(); i++) {
                BlockHeader header1 = null;
                BlockHeader header2 = null;
                try {
                    header1 = byteBuffer.readNulsData(new BlockHeader());
                    header2 = byteBuffer.readNulsData(new BlockHeader());
                } catch (NulsException e) {
                    chain.getLogger().error(e.getMessage());
                }
                if (null == header1 || null == header2) {
                    throw new NulsException(ConsensusErrorCode.DATA_NOT_EXIST);
                }
                if (header1.getHeight() != header2.getHeight()) {
                    throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
                }
                if (!Arrays.equals(header1.getBlockSignature().getPublicKey(), header2.getBlockSignature().getPublicKey())) {
                    throw new NulsException(ConsensusErrorCode.BLOCK_SIGNATURE_ERROR);
                }
                BlockExtendsData blockExtendsData = header1.getExtendsData();
                roundIndex[i] = blockExtendsData.getRoundIndex();
            }
            //验证三次分叉是否是100轮以内
            if (roundIndex[ConsensusConstant.REDPUNISH_BIFURCATION - 1] - roundIndex[0] > ConsensusConstant.VALUE_OF_ONE_HUNDRED) {
                throw new NulsException(ConsensusErrorCode.BLOCK_RED_PUNISH_ERROR);
            }
        }
      /*
      红牌交易类型为黄牌过多
      The type of red card trading is too many yellow cards
      */
        else if (punishData.getReasonCode() != PunishReasonEnum.TOO_MUCH_YELLOW_PUNISH.getCode()) {
            throw new NulsException(ConsensusErrorCode.BLOCK_PUNISH_VALID_ERROR);
        }

      /*
      CoinData验证
      CoinData verification
      */
        if (!coinDataValidate(chain, tx)) {
            throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
        }
        return true;
    }


    /**
     * 区块CoinBase交易验证
     * Block CoinBase transaction verification
     *
     * @param block        block info
     * @param currentRound Block round information
     * @param member       Node packing information
     * @param chain        chain info
     */
    private boolean coinBaseValidate(Block block, MeetingRound currentRound, MeetingMember member, Chain chain, String blockHeaderHash, BlockExtendsData extendsData) throws Exception {
        Transaction tx = block.getTxs().get(0);
        if (tx.getType() != TxType.COIN_BASE) {
            chain.getLogger().error("CoinBase transaction order wrong! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
            return false;
        }
        Transaction coinBaseTransaction = consensusManager.createCoinBaseTx(chain, member, block.getTxs(), currentRound, 0, extendsData);
        if (null == coinBaseTransaction) {
            chain.getLogger().error("the coin base tx is wrong! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
            return false;
        } else if (!tx.getHash().equals(coinBaseTransaction.getHash())) {
            CoinFromComparator fromComparator = new CoinFromComparator();
            CoinToComparator toComparator = new CoinToComparator();

            CoinData coinBaseCoinData = coinBaseTransaction.getCoinDataInstance();
            coinBaseCoinData.getFrom().sort(fromComparator);
            coinBaseCoinData.getTo().sort(toComparator);
            coinBaseTransaction.setCoinData(coinBaseCoinData.serialize());
            coinBaseTransaction.setHash(null);

            Transaction originTransaction = new Transaction();
            originTransaction.parse(tx.serialize(), 0);
            CoinData originCoinData = originTransaction.getCoinDataInstance();
            originCoinData.getFrom().sort(fromComparator);
            originCoinData.getTo().sort(toComparator);
            originTransaction.setCoinData(originCoinData.serialize());

            if (!originTransaction.getHash().equals(coinBaseTransaction.getHash())) {
                chain.getLogger().error("the coin base tx is wrong! height: " + block.getHeader().getHeight() + " , hash : " + blockHeaderHash);
                return false;
            }
        }
        return true;
    }

    /**
     * CoinData 验证
     * CoinData Verification
     *
     * @param tx    red punish transaction
     * @param chain chain info
     * @return 验证是否成功/Verify success
     */
    private boolean coinDataValidate(Chain chain, Transaction tx) throws NulsException {
        Agent punishAgent = null;
        io.icw.poc.model.bo.tx.txdata.RedPunishData punishData;
        punishData = new RedPunishData();
        punishData.parse(tx.getTxData(), 0);
        for (Agent agent : chain.getAgentList()) {
            if (agent.getDelHeight() > 0 && (tx.getBlockHeight() <= 0 || agent.getDelHeight() < tx.getBlockHeight())) {
                continue;
            }
            if (Arrays.equals(punishData.getAddress(), agent.getAgentAddress())) {
                punishAgent = agent;
                break;
            }
        }
        if (null == punishAgent) {
            Log.info(ConsensusErrorCode.AGENT_NOT_EXIST.getMsg());
            return false;
        }
        CoinData coinData = coinDataManager.getStopAgentCoinData(chain, punishAgent, tx.getTime() + chain.getConfig().getRedPublishLockTime());
        try {
            CoinFromComparator fromComparator = new CoinFromComparator();
            CoinToComparator toComparator = new CoinToComparator();
            coinData.getFrom().sort(fromComparator);
            coinData.getTo().sort(toComparator);
            CoinData txCoinData = new CoinData();
            txCoinData.parse(tx.getCoinData(), 0);
            txCoinData.getFrom().sort(fromComparator);
            txCoinData.getTo().sort(toComparator);
            if (!Arrays.equals(coinData.serialize(), txCoinData.serialize())) {
                chain.getLogger().error("++++++++++ RedPunish verification does not pass, redPunish type:{}, - height:{}, - redPunish tx timestamp:{}", punishData.getReasonCode(), tx.getBlockHeight(), tx.getTime());
                return false;
            }
        } catch (IOException e) {
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }
}
