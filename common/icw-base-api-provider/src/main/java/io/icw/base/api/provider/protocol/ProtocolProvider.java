package io.icw.base.api.provider.protocol;

import io.icw.base.api.provider.protocol.facade.GetVersionReq;
import io.icw.base.api.provider.protocol.facade.VersionInfo;
import io.icw.base.api.provider.Result;

/**
 * @Author: zhoulijun
 * @Time: 2020-01-15 18:16
 * @Description: 功能描述
 */
public interface ProtocolProvider {

    Result<VersionInfo> getVersion(GetVersionReq req);

}
