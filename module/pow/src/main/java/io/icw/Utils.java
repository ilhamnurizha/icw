package io.icw;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.icw.base.data.Transaction;
import io.icw.base.signture.P2PHKSignature;
import io.icw.base.signture.SignatureUtil;
import io.icw.base.signture.TransactionSignature;
import io.icw.core.constant.CommonCodeConstanst;
import io.icw.core.crypto.AESEncrypt;
import io.icw.core.crypto.ECKey;
import io.icw.core.crypto.HexUtil;
import io.icw.core.exception.CryptoException;
import io.icw.core.exception.NulsRuntimeException;
import io.icw.core.model.ObjectUtils;

public class Utils {


    public static Transaction signTransaction(Transaction transaction, String priKey,String pubKey, String password) throws IOException {
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        ECKey eckey = null;
        byte[] unencryptedPrivateKey;
        //判断当前账户是否存在私钥，如果不存在私钥这为加密账户
        BigInteger newPriv = null;
        ObjectUtils.canNotEmpty(password, "the password can not be empty");
        try {
            unencryptedPrivateKey = AESEncrypt.decrypt(HexUtil.decode(priKey), password);
            newPriv = new BigInteger(1, unencryptedPrivateKey);
        } catch (CryptoException e) {
            throw new NulsRuntimeException(CommonCodeConstanst.FAILED,"password is wrong");
        }
        eckey = ECKey.fromPrivate(newPriv);
        if (!Arrays.equals(eckey.getPubKey(), HexUtil.decode(pubKey))) {
            throw new NulsRuntimeException(CommonCodeConstanst.FAILED,"password is wrong");
        }
        P2PHKSignature p2PHKSignature = SignatureUtil.createSignatureByEckey(transaction, eckey);
        p2PHKSignatures.add(p2PHKSignature);
        transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        transaction.setTransactionSignature(transactionSignature.serialize());
        return transaction;
    }


}
