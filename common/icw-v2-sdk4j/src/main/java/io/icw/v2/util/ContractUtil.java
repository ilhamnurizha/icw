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
package io.icw.v2.util;

import io.icw.v2.constant.Constant;
import io.icw.v2.error.ContractErrorCode;
import io.icw.v2.model.dto.ProgramMultyAssetValue;
import io.icw.v2.tx.CreateContractTransaction;
import io.icw.v2.txdata.CallContractData;
import io.icw.v2.txdata.ContractData;
import io.icw.v2.txdata.CreateContractData;
import io.icw.v2.txdata.DeleteContractData;
import io.icw.base.RPCUtil;
import io.icw.base.basic.AddressTool;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.TransactionFeeCalculator;
import io.icw.base.data.*;
import io.icw.core.basic.NulsData;
import io.icw.core.basic.Result;
import io.icw.core.basic.VarInt;
import io.icw.core.constant.CommonCodeConstanst;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.core.model.LongUtils;
import io.icw.core.model.StringUtils;
import io.icw.v2.tx.CallContractTransaction;
import io.icw.v2.tx.DeleteContractTransaction;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static io.icw.core.constant.TxType.*;
import static io.icw.core.model.StringUtils.isBlank;

/**
 * @author: PierreLuo
 * @date: 2018/8/25
 */
public class ContractUtil {

    public static String[][] twoDimensionalArray(Object[] args, String[] types) {
        if (args == null) {
            return null;
        } else {
            int length = args.length;
            String[][] two = new String[length][];
            Object arg;
            for (int i = 0; i < length; i++) {
                arg = args[i];
                if (arg == null) {
                    two[i] = new String[0];
                    continue;
                }
                if (arg instanceof String) {
                    String argStr = (String) arg;
                    // 非String类型参数，若传参是空字符串，则赋值为空一维数组，避免数字类型转化异常 -> 空字符串转化为数字
                    if (types != null && isBlank(argStr) && !Constant.STRING.equalsIgnoreCase(types[i])) {
                        two[i] = new String[0];
                    } else {
                        two[i] = new String[]{argStr};
                    }
                } else if (arg.getClass().isArray()) {
                    int len = Array.getLength(arg);
                    String[] result = new String[len];
                    for (int k = 0; k < len; k++) {
                        result[k] = valueOf(Array.get(arg, k));
                    }
                    two[i] = result;
                } else if (arg instanceof ArrayList) {
                    ArrayList resultArg = (ArrayList) arg;
                    int size = resultArg.size();
                    String[] result = new String[size];
                    for (int k = 0; k < size; k++) {
                        result[k] = valueOf(resultArg.get(k));
                    }
                    two[i] = result;
                } else {
                    two[i] = new String[]{valueOf(arg)};
                }
            }
            return two;
        }
    }

    public static byte[] extractContractAddressFromTxData(Transaction tx) {
        if (tx == null) {
            return null;
        }
        int txType = tx.getType();
        if (txType == CREATE_CONTRACT
                || txType == CALL_CONTRACT
                || txType == DELETE_CONTRACT) {
            return extractContractAddressFromTxData(tx.getTxData());
        }
        return null;
    }

    private static byte[] extractContractAddressFromTxData(byte[] txData) {
        if (txData == null) {
            return null;
        }
        int length = txData.length;
        if (length < Address.ADDRESS_LENGTH * 2) {
            return null;
        }
        byte[] contractAddress = new byte[Address.ADDRESS_LENGTH];
        System.arraycopy(txData, Address.ADDRESS_LENGTH, contractAddress, 0, Address.ADDRESS_LENGTH);
        return contractAddress;
    }


    public static String[][] twoDimensionalArray(Object[] args) {
        return twoDimensionalArray(args, null);
    }

    public static String valueOf(Object obj) {
        return (obj == null) ? null : obj.toString();
    }


    public static boolean isContractTransaction(Transaction tx) {
        if (tx == null) {
            return false;
        }
        int txType = tx.getType();
        if (txType == CREATE_CONTRACT
                || txType == CALL_CONTRACT
                || txType == DELETE_CONTRACT
                || txType == CONTRACT_TRANSFER
                || txType == CONTRACT_RETURN_GAS) {
            return true;
        }
        return false;
    }

    public static boolean isGasCostContractTransaction(Transaction tx) {
        if (tx == null) {
            return false;
        }
        int txType = tx.getType();
        if (txType == CREATE_CONTRACT
                || txType == CALL_CONTRACT) {
            return true;
        }
        return false;
    }


    public static boolean isLockContract(long lastestHeight, long blockHeight) throws NulsException {
        if (blockHeight > 0) {
            long confirmCount = lastestHeight - blockHeight;
            if (confirmCount < 7) {
                return true;
            }
        }
        return false;
    }


    public static String bigInteger2String(BigInteger bigInteger) {
        if (bigInteger == null) {
            return "0";
        }
        return bigInteger.toString();
    }

    public static String simplifyErrorMsg(String errorMsg) {
        String resultMsg = "contract error - ";
        if (isBlank(errorMsg)) {
            return resultMsg;
        }
        if (errorMsg.contains("Exception:")) {
            String[] msgs = errorMsg.split("Exception:", 2);
            return resultMsg + msgs[1].trim();
        }
        return resultMsg + errorMsg;
    }

    public static Result checkVmResultAndReturn(String errorMessage, Result defaultResult) {
        if (isBlank(errorMessage)) {
            return defaultResult;
        }
        if (isNotEnoughGasError(errorMessage)) {
            return Result.getFailed(ContractErrorCode.CONTRACT_GAS_LIMIT);
        }
        return defaultResult;
    }

    private static boolean isNotEnoughGasError(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }
        if (errorMessage.contains(Constant.NOT_ENOUGH_GAS)) {
            return true;
        }
        return false;
    }

    public static boolean isTerminatedContract(int status) {
        return Constant.STOP == status;
    }

    public static boolean isTransferMethod(String method) {
        return (Constant.NRC20_METHOD_TRANSFER.equals(method)
                || Constant.NRC20_METHOD_TRANSFER_FROM.equals(method));
    }

    public static String argToString(String[][] args) {
        if (args == null) {
            return "";
        }
        String result = "";
        for (String[] a : args) {
            result += Arrays.toString(a) + "| ";
        }
        return result;
    }


    public static boolean isLegalContractAddress(int chainId, byte[] addressBytes) {
        if (addressBytes == null) {
            return false;
        }
        return AddressTool.validContractAddress(addressBytes, chainId);
    }


    public static Result getSuccess() {
        return Result.getSuccess(CommonCodeConstanst.SUCCESS);
    }

    public static Result getFailed() {
        return Result.getFailed(CommonCodeConstanst.FAILED);
    }

    public static String asString(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] asBytes(String string) {
        return Base64.getDecoder().decode(string);
    }

    public static BigInteger minus(BigInteger a, BigInteger b) {
        BigInteger result = a.subtract(b);
        if (result.compareTo(BigInteger.ZERO) < 0) {
            throw new RuntimeException("Negative number detected.");
        }
        return result;
    }

    public static int extractTxTypeFromTx(String txString) throws NulsException {
        String txTypeHexString = txString.substring(0, 4);
        NulsByteBuffer byteBuffer = new NulsByteBuffer(RPCUtil.decode(txTypeHexString));
        return byteBuffer.readUint16();
    }

    public static String toString(String[][] a) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(Arrays.toString(a[i]));
            if (i == iMax) {
                b.append(']');
                break;
            }
            b.append(", ");
        }
        return b.toString();
    }

    public static CreateContractTransaction newCreateTx(int chainId, int assetsId, BigInteger senderBalance, String nonce, CreateContractData createContractData, String remark) {
        try {
            CreateContractTransaction tx = new CreateContractTransaction();
            if (StringUtils.isNotBlank(remark)) {
                tx.setRemark(remark.getBytes(StandardCharsets.UTF_8));
            }
            tx.setTime(System.currentTimeMillis() / 1000);
            // 计算CoinData
            CoinData coinData = makeCoinData(chainId, assetsId, senderBalance, nonce, createContractData, tx.size(), calcSize(createContractData));
            tx.setTxDataObj(createContractData);
            tx.setCoinDataObj(coinData);
            tx.serializeData();
            return tx;
        } catch (IOException e) {
            Log.error(e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static CallContractTransaction newCallTx(int chainId, int assetId, BigInteger senderBalance, String nonce, CallContractData callContractData, String remark,
                                                    List<ProgramMultyAssetValue> multyAssetValues) {
        return newCallTx(chainId, assetId, senderBalance, nonce, callContractData, 0, remark, multyAssetValues);
    }

    public static CallContractTransaction newCallTx(int chainId, int assetId, BigInteger senderBalance, String nonce,
                                                    CallContractData callContractData, long time, String remark,
                                                    List<ProgramMultyAssetValue> multyAssetValues) {
        try {
            CallContractTransaction tx = new CallContractTransaction();
            if (StringUtils.isNotBlank(remark)) {
                tx.setRemark(remark.getBytes(StandardCharsets.UTF_8));
            }
            if (time == 0) {
                tx.setTime(System.currentTimeMillis() / 1000);
            } else {
                tx.setTime(time);
            }

            byte[] sender = callContractData.getSender();
            BigInteger value = callContractData.getValue();
            byte[] contractAddress = callContractData.getContractAddress();
            List<CoinFrom> froms = new ArrayList<>();
            List<CoinTo> tos = new ArrayList<>();
            if (value.compareTo(BigInteger.ZERO) > 0) {
                CoinFrom coinFrom = new CoinFrom(sender, chainId, assetId, value, RPCUtil.decode(nonce), (byte) 0);
                froms.add(coinFrom);
                CoinTo coinTo = new CoinTo(contractAddress, chainId, assetId, value);
                tos.add(coinTo);
            }
            int _assetChainId, _assetId;
            if (multyAssetValues != null) {
                for (ProgramMultyAssetValue multyAssetValue : multyAssetValues) {
                    BigInteger _value = multyAssetValue.getValue();
                    _assetChainId = multyAssetValue.getAssetChainId();
                    _assetId = multyAssetValue.getAssetId();

                    CoinFrom coinFrom = new CoinFrom(sender, _assetChainId, _assetId, _value, RPCUtil.decode(multyAssetValue.getNonce()), (byte) 0);
                    froms.add(coinFrom);

                    CoinTo coinTo = new CoinTo(contractAddress, _assetChainId, _assetId, _value);
                    tos.add(coinTo);
                }
            }

            // 计算CoinData
            CoinData coinData = new CoinData();
            coinData.setFrom(froms);
            coinData.setTo(tos);
            long gasUsed = callContractData.getGasLimit();
            BigInteger imputedValue = BigInteger.valueOf(LongUtils.mul(gasUsed, callContractData.getPrice()));
            byte[] feeAccountBytes = sender;
            BigInteger feeValue = imputedValue;
            CoinFrom feeAccountFrom = null;
            for (CoinFrom from : froms) {
                _assetChainId = from.getAssetsChainId();
                _assetId = from.getAssetsId();
                if (Arrays.equals(from.getAddress(), feeAccountBytes) && _assetChainId == chainId && _assetId == assetId) {
                    from.setAmount(from.getAmount().add(feeValue));
                    feeAccountFrom = from;
                    break;
                }
            }
            if (feeAccountFrom == null) {
                feeAccountFrom = new CoinFrom(feeAccountBytes, chainId, assetId, feeValue, RPCUtil.decode(nonce), (byte) 0);
                coinData.addFrom(feeAccountFrom);
            }
            tx.setCoinData(coinData.serialize());
            tx.setTxData(callContractData.serialize());

            BigInteger txSizeFee = TransactionFeeCalculator.getNormalUnsignedTxFee(tx.getSize() + 130);
            feeAccountFrom.setAmount(feeAccountFrom.getAmount().add(txSizeFee));

            tx.setCoinData(coinData.serialize());
            return tx;
        } catch (IOException e) {
            Log.error(e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static DeleteContractTransaction newDeleteTx(int chainId, int assetsId, BigInteger senderBalance, String nonce, DeleteContractData deleteContractData, String remark) {
        try {
            DeleteContractTransaction tx = new DeleteContractTransaction();
            if (StringUtils.isNotBlank(remark)) {
                tx.setRemark(remark.getBytes(StandardCharsets.UTF_8));
            }
            tx.setTime(System.currentTimeMillis() / 1000);
            // 计算CoinData
            CoinData coinData = makeCoinData(chainId, assetsId, senderBalance, nonce, deleteContractData, tx.size(), calcSize(deleteContractData));
            tx.setTxDataObj(deleteContractData);
            tx.setCoinDataObj(coinData);
            tx.serializeData();
            return tx;
        } catch (IOException e) {
            Log.error(e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private static CoinData makeCoinData(int chainId, int assetsId, BigInteger senderBalance, String nonce, ContractData contractData, int txSize, int txDataSize) {
        CoinData coinData = new CoinData();
        long gasUsed = contractData.getGasLimit();
        BigInteger imputedValue = BigInteger.valueOf(LongUtils.mul(gasUsed, contractData.getPrice()));
        // 总花费
        BigInteger value = contractData.getValue();
        BigInteger totalValue = imputedValue.add(value);

        CoinFrom coinFrom = new CoinFrom(contractData.getSender(), chainId, assetsId, totalValue, RPCUtil.decode(nonce), (byte) 0);
        coinData.addFrom(coinFrom);

        if (value.compareTo(BigInteger.ZERO) > 0) {
            CoinTo coinTo = new CoinTo(contractData.getContractAddress(), chainId, assetsId, value);
            coinData.addTo(coinTo);
        }

        BigInteger fee = TransactionFeeCalculator.getNormalUnsignedTxFee(txSize + txDataSize + calcSize(coinData));
        totalValue = totalValue.add(fee);
        if (senderBalance.compareTo(totalValue) < 0) {
            // Insufficient balance
            throw new RuntimeException("Insufficient balance");
        }
        coinFrom.setAmount(totalValue);
        return coinData;
    }

    private static int calcSize(NulsData nulsData) {
        if (nulsData == null) {
            return 0;
        }
        int size = nulsData.size();
        // 计算tx.size()时，当coinData和txData为空时，计算了1个长度，若此时nulsData不为空，则要扣减这1个长度
        return VarInt.sizeOf(size) + size - 1;
    }
}
