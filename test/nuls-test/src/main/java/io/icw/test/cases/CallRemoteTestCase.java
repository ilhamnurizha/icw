package io.icw.test.cases;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.network.NetworkProvider;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.log.Log;
import io.icw.core.model.StringUtils;
import io.icw.core.parse.MapUtils;
import io.icw.test.Config;
import io.icw.test.controller.RemoteCaseReq;
import io.icw.test.controller.RemoteResult;
import io.icw.test.utils.RestFulUtils;
import io.icw.test.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-25 14:03
 * @Description: 功能描述
 */
public abstract class CallRemoteTestCase<T,P> extends BaseTestCase<T,P> {

    protected NetworkProvider networkProvider = ServiceManager.get(NetworkProvider.class);

    protected List<String> getRemoteNodes() throws TestFailException {
        Result<String> nodes = networkProvider.getNodes();
        Config config = SpringLiteContext.getBean(Config.class);
        if(!config.isMaster()){
            throw new RuntimeException("非master节点不允许进行远程调用");
        }
        List<String> nodeList;
        if(StringUtils.isNotBlank(config.getTestNodeList())){
            nodeList = Arrays.asList(config.getTestNodeList().split(","));
        }else{
            nodeList = nodes.getList().stream().map(node->node.split(":")[0]).filter(node->config.getNodeExclude().indexOf(node)==-1).collect(Collectors.toList());
        }
        if(nodeList.isEmpty()){
            throw new TestFailException("remote fail ,network node is empty");
        }
        int testNodeCount = config.getTestNodeCount() > nodeList.size() ? nodeList.size() : config.getTestNodeCount();
        return nodeList.subList(0,testNodeCount);
    }

    public <S> S doRemoteTest(String node, Class<? extends TestCaseIntf> caseCls, Object param) throws TestFailException {
        Config config = SpringLiteContext.getBean(Config.class);
        RemoteCaseReq req = new RemoteCaseReq();
        req.setCaseClass(caseCls);
        if(param != null){
            req.setParam(Utils.toJson(param));
        }
        RestFulUtils.getInstance().setServerUri("http://" + node.split(":")[0] + ":" + config.getHttpPort() + "/api");
        Log.debug("call {} remote case:{}",node,req);
        RemoteResult<S> result = RestFulUtils.getInstance().post("remote/call", MapUtils.beanToMap(req));
        Log.debug("call remote case returl :{}",result);
        checkResultStatus(new Result(result.getData()));
        return result.getData();
    }

}
