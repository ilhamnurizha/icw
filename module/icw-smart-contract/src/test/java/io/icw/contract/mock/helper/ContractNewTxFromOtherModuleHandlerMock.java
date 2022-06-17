package io.icw.contract.mock.helper;

import io.icw.base.basic.AddressTool;
import io.icw.base.data.Transaction;
import io.icw.contract.util.Log;
import io.icw.contract.vm.Frame;
import io.icw.contract.helper.ContractNewTxFromOtherModuleHandler;

public class ContractNewTxFromOtherModuleHandlerMock extends ContractNewTxFromOtherModuleHandler{

    @Override
    public Transaction updateNonceAndVmBalance(int chainId, byte[] contractAddressBytes, String txHash, String txStr, Frame frame) {
        Log.info("chainId: {}, contractAddress: {}, txHash: {}, txStr: {}", chainId, AddressTool.getStringAddressByBytes(contractAddressBytes), txHash, txStr);
        return null;
    }
}