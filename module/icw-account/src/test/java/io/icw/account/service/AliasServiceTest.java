package io.icw.account.service;

import io.icw.account.AccountBootstrap;
import io.icw.account.config.NulsConfig;
import io.icw.account.model.bo.Account;
import io.icw.account.model.bo.tx.txdata.Alias;
import io.icw.account.storage.AliasStorageServiceTest;
import io.icw.base.basic.AddressTool;
import io.icw.core.rockdb.service.RocksDBService;
import io.icw.core.core.inteceptor.ModularServiceMethodInterceptor;
import io.icw.core.core.ioc.SpringLiteContext;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/8
 */
public class AliasServiceTest {

    protected static AccountService accountService;
    protected static AliasService aliasService;
    protected int chainId = 2;
    protected String password = "a2678";

    @BeforeClass
    public static void beforeTest() {
        //初始化配置
        SpringLiteContext.init("io.icw.account", new ModularServiceMethodInterceptor());
        AccountBootstrap accountBootstrap = SpringLiteContext.getBean(AccountBootstrap.class);
        //初始化配置
        accountBootstrap.initCfg();        //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(NulsConfig.DATA_PATH);
        //启动时间同步线程
//        TimeService.getInstance().start();
        accountService = SpringLiteContext.getBean(AccountService.class);
        aliasService = SpringLiteContext.getBean(AliasService.class);
    }

    /**
     * save the alias
     * <p>
     * Nov.10th 2018
     *
     * @auther EdwardChan
     */
    @Test
    public void saveAliasTest() throws Exception {
        // create account
        Alias alias = AliasStorageServiceTest.createAlias();
        boolean result = aliasService.aliasTxCommit(chainId,alias);
        assertTrue(result);
        Account account = accountService.getAccount(chainId, AddressTool.getStringAddressByBytes(alias.getAddress()));
        assertNotNull(account);
        assertEquals(account.getAlias(),alias.getAlias());
    }
}
