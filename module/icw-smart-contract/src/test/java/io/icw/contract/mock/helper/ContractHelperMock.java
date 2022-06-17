package io.icw.contract.mock.helper;

import io.icw.base.RPCUtil;
import io.icw.contract.helper.ContractHelper;
import io.icw.contract.model.bo.ContractBalance;

import java.util.Arrays;

public class ContractHelperMock extends ContractHelper {

    public ContractBalance getBalance(int chainId, byte[] address) {
        byte[] currentNonceBytes = Arrays.copyOfRange(address, address.length - 8, address.length);
        ContractBalance contractBalance = ContractBalance.newInstance();
        contractBalance.setNonce(RPCUtil.encode(currentNonceBytes));
        return contractBalance;
    }
}