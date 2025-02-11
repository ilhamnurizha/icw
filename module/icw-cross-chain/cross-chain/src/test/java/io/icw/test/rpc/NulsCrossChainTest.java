package io.icw.test.rpc;
import io.icw.core.exception.NulsException;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.util.RpcCall;
import io.icw.crosschain.base.model.dto.input.CoinDTO;
import io.icw.core.rpc.info.HostInfo;
import io.icw.core.rpc.info.NoUse;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;
import io.icw.core.log.Log;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NulsCrossChainTest {
    static int assetChainId = 2;
    static int assetId = 1;
    static String version = "1.0";
    static int chainId = 2;
    static String password = "nuls123456";

    static String main_address20 = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    static String main_address21 = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    static String main_address22 = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    static String main_address23 = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    static String main_address24 = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    static String main_address25 = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";
    static String main_address26 = "tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm";
    static String main_address27 = "tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1";
    static String main_address28 = "tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2";
    static String main_address29 = "tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn";


    static String local_address1 = "8CPcA7kaXSHbWb3GHP7bd5hRLFu8RZv57rY9w";
    static String local_address2 = "8CPcA7kaj56TWAC3Cix64aYCU3XFoNpu1LN1K";
    static String local_address3 = "8CPcA7kaiDAkvVP28GwXR6eP2oDKPcnPnmvLD";
    static String local_address4 = "8CPcA7kaZDdGEzXe8gwQNQg4u4teecArHt9Dy";
    static String local_address5 = "8CPcA7kaW82Eoj9wyLr96g2uBhHtFqD9Vy4yM";
    static String local_address6 = "8CPcA7kaUW98RW3g7erqTNT7b1gyoaqwxFEY3";
    static String local_address7 = "8CPcA7kaZTXgqBR7DYVbsj8yWUD2sZah6kknY";
    static String local_address8 = "8CPcA7kaaw7jvfn93Zrf7vNwtXyRQMY71zdYF";
    static String local_address9 = "8CPcA7kaUvrGb68gYWcceJRY2Mx2KfUTMJmgB";
    static String local_address10 = "8CPcA7kag8NijwHK8eTJVVMGXjfkT3GDAVo7n";

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
    }

    @Test
    public void batchCreateCtx() throws Exception{
        for(int i = 0;i<= 1000 ;i++){
            String hash = createCtx();
            String tx = getTx(hash);
            while (tx == null || tx.isEmpty()){
                Thread.sleep(100);
                tx = getTx(hash);
            }
            Log.info("第{}笔交易，hash{}",i,hash);
        }
    }

    private String getTx(String hash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("txHash", hash);
        //调用接口
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getTx", params);
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("tx_getTx"));
        return (String)result.get("tx");
    }

    @SuppressWarnings("unchecked")
    private String createCtx(){
        try{
            List<CoinDTO> fromList = new ArrayList<>();
            List<CoinDTO> toList = new ArrayList<>();
            fromList.add(new CoinDTO("tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm",assetChainId,assetId, BigInteger.valueOf(100000000L),password));
            toList.add(new CoinDTO("GDMcKEW9i43HACuvkRNFLJgV4yQjXZQhASbed",assetChainId,assetId, BigInteger.valueOf(100000000L),password));
            Map paramMap = new HashMap();
            paramMap.put("listFrom", fromList);
            paramMap.put("listTo", toList);
            paramMap.put("chainId", chainId);
            paramMap.put("remark", "transfer test");
            //调用接口
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CC.abbr, "createCrossTx", paramMap);
            if (!cmdResp.isSuccess()) {
                Log.info("接口调用失败！" );
            }
            HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("createCrossTx"));
            String hash = (String) result.get("txHash");
            Log.debug("{}", hash);
            return hash;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testSendCrossTx(){
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, 2);
        params.put("tx", "0a00c07ed05e0000d20217020001b003d44b0cba41eb30bb5b775dc63f79247fb1a1040001000058850c0200000000000000000000000000000000000000000000000000000008c6bcaba6b2db02d50017020001b003d44b0cba41eb30bb5b775dc63f79247fb1a10200010040420f00000000000000000000000000000000000000000000000000000000000831816bd95cc2224600011704000111246da8310ed89f6ac6b16cfe17b72d7410c4de040001000058850c0200000000000000000000000000000000000000000000000000000000000000000000006a21026728caa0b388c4ae0f39394a039e5f773e3c9476192e707a3f54e6c4f1874583473045022100c050663c9c647b8985cf22c7d667953f0b4a5b1abe284d4853d47fc3ec6f15470220070df0df8adab51e47c061d70cc3f5c638846c1c885ba11cdfca3ae899ffe7e5");
        try {
            Map map = (Map) RpcCall.request(ModuleE.CC.abbr, "newApiModuleCrossTx", params);
            Log.info("{}",map);
        } catch (NulsException e) {
        }
    }


}
