/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.icw.network.manager;

import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.network.constant.ManagerStatusEnum;
import io.icw.network.model.po.GroupNodesPo;
import io.icw.network.model.po.GroupPo;
import io.icw.network.model.po.NodePo;
import io.icw.network.storage.DbService;
import io.icw.network.storage.impl.DbServiceImpl;
import io.icw.network.model.NodeGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储管理
 * storage  manager
 *
 * @author lan
 * @date 2018/11/01
 */
public class StorageManager extends BaseManager {
    private static StorageManager storageManager = new StorageManager();
    private static Map<String, NodePo> cacheAllNodes = new ConcurrentHashMap<>();
    private DbService dbService = null;

    private StorageManager() {

    }

    public DbService getDbService() {
        return dbService;
    }

    public static StorageManager getInstance() {
        return storageManager;
    }

    /**
     * getAllNodeGroupFromDb
     *
     * @return List<NodeGroup
                    */
    List<NodeGroup> getAllNodeGroupFromDb() {
        List<NodeGroup> nodeGroups = new ArrayList<>();
        try {
            List<GroupPo> groupPos = dbService.getAllNodeGroups();
            for (GroupPo groupPo : groupPos) {
                nodeGroups.add((NodeGroup) groupPo.parseDto());
            }
        } catch (NulsException e) {
            Log.error("", e);
        }
        return nodeGroups;
    }

    /**
     * get Nodes
     *
     * @param chainId chainId
     * @return List<Node>
     */
    GroupNodesPo getNodesByChainId(int chainId) {
        try {
            return dbService.getNodesByChainId(chainId);
        } catch (NulsException e) {
            Log.error(e);
        }
        return null;
    }

    @Override
    public void init() throws Exception {
        dbService = SpringLiteContext.getBean(DbServiceImpl.class);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void change(ManagerStatusEnum toStatus) throws Exception {

    }
}
