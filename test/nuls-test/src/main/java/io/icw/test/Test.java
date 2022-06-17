package io.icw.test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 17:41
 * @Description: 功能描述
 */
public class Test {

    public static void main(String[] args) {
//        RemoteCaseReq req = new RemoteCaseReq();
//        req.setCaseClass(SleepAdapter.$15SEC.class);
//        req.setParam("5MR_2CaLdKkCgdLAg9NYnppSRU9o5Lkx9wT");
//        RestFulUtils.getInstance().setServerUri("http://192.168.1.115:9999/api");
//        RemoteResult<AccountInfo> res = RestFulUtils.getInstance().post("/remote/call", MapUtils.beanToMap(req));
//        System.out.println("res:{}" + res);
        
//        Map<Long, Set> roundMemberMap = new HashMap<Long, Set>();
//        long index = 1;
//        Set set = new HashSet();
//        set.add(123);
//        roundMemberMap.put(index, set);
//        System.out.println(roundMemberMap.get(index));
        
        
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("cmd", "cmd");
        paramMap.put("messageBody", "123");
        System.out.println(paramMap.toString());
    }

}
