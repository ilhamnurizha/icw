package io.icw.rpc;

import java.util.HashMap;
import java.util.Map;

import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.core.parse.JSONUtils;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;
import io.icw.round.MeetingRound;

@Component
public class ChainTools {
    public MeetingRound getRoundInfo(int chainId) throws NulsException {
    	try {
    		Map<String,Object> params = new HashMap<>();
	        params.put(Constants.CHAIN_ID, chainId);
	        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getRoundInfo", params);
	        MeetingRound round = JSONUtils.json2pojo(JSONUtils.obj2json(((HashMap) cmdResp.getResponseData()).get("cs_getRoundInfo")), MeetingRound.class);
//	        System.out.println(cmdResp.getResponseData());
	        return round;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
    
    public Map getPackingAddress(int chainId) throws NulsException {
    	Map addressMap = new HashMap();
    	try {
    		Map<String,Object> params = new HashMap<>();
	        params.put(Constants.CHAIN_ID, chainId);
	        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getPackerInfo", params);
	        Map packerInfo = (Map)((Map) cmdResp.getResponseData()).get("cs_getPackerInfo");
//	        System.out.println("packerInfo" + packerInfo);
	        String address = (String)packerInfo.get("address");
	        String password = (String)packerInfo.get("password");
//	        System.out.println("address: " + address);
	        
	        addressMap.put("address", address);
	        addressMap.put("password", password);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    	return addressMap;
    }
    
    public Map getAgentAddressList(int chainId) throws NulsException {
    	Map addressMap = new HashMap();
    	try {
    		Map<String,Object> params = new HashMap<>();
	        params.put(Constants.CHAIN_ID, chainId);
	        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentAddressList", params);
//	        System.out.println(((Map)cmdResp.getResponseData()).get("cs_getAgentAddressList"));
	        
	        Map packerInfo = (Map)((Map) cmdResp.getResponseData()).get("cs_getAgentAddressList");
//	        System.out.println("packerInfo" + packerInfo);
	        
//	        System.out.println("address" + packerInfo.get("address"));
//	        String address = (String)packerInfo.get("address");
//	        String password = (String)packerInfo.get("password");
//	        System.out.println("address: " + address);
//	        
//	        addressMap.put("address", address);
//	        addressMap.put("password", password);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    	return addressMap;
    }
}
