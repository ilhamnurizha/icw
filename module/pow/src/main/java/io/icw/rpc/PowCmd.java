package io.icw.rpc;

import java.util.List;
import java.util.Map;

import io.icw.core.core.annotation.Component;
import io.icw.core.log.Log;
import io.icw.core.rpc.cmd.BaseCmd;
import io.icw.core.rpc.model.CmdAnnotation;
import io.icw.core.rpc.model.Parameter;
import io.icw.core.rpc.model.ResponseData;
import io.icw.core.rpc.model.TypeDescriptor;
import io.icw.core.rpc.model.message.Response;
import io.icw.thread.process.PowProcess;

@Component
public class PowCmd extends BaseCmd {
    @CmdAnnotation(cmd = "pow_roundMembers", version = 1.0, description = "pow_roundMembers")
    @Parameter(parameterName = "roundIndex", requestType = @TypeDescriptor(value = long.class), parameterDes = "roundIndex")
    @Parameter(parameterName = "blockHeight", requestType = @TypeDescriptor(value = long.class), parameterDes = "blockHeight")
    @Parameter(parameterName = "isRealTime", requestType = @TypeDescriptor(value = boolean.class), parameterDes = "isRealTime")
    @ResponseData(name = "返回值", description = "返回Members",
            responseType = @TypeDescriptor(value = List.class, collectionElement = String.class)
    )
    public Response getRoundMembers(Map<String,Object> params){
    	long roundIndex = Long.valueOf( params.get("roundIndex").toString() );
    	long blockHeight = Long.valueOf( params.get("blockHeight").toString() );
    	boolean isRealTime = Boolean.valueOf( params.get("isRealTime").toString() );
    	
//    	Log.info("pow_roundMembers: " + roundIndex + " blockHeight: " + blockHeight + " isRealTime: " + isRealTime);
    	
        List<String> members = null;
//        if (isRealTime) {
//        	members = PowProcess.getRoundMembers(roundIndex);
//        } else {
        	members = PowProcess.getRoundMembersByHeightIndex(roundIndex, blockHeight);
//        }
        
        return success(members);
    }
}
