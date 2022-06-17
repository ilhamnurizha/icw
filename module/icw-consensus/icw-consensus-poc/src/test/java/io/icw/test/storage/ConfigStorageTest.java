package io.icw.test.storage;

import io.icw.core.rockdb.service.RocksDBService;
import io.icw.poc.constant.ConsensusConstant;
import io.icw.poc.model.bo.config.ConfigBean;
import io.icw.poc.storage.ConfigService;
import io.icw.test.TestUtil;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.log.Log;
import io.icw.core.parse.ConfigLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public class ConfigStorageTest {
    private ConfigService configService;
    @Before
    public void init(){
        try {
            Properties properties = ConfigLoader.loadProperties(ConsensusConstant.DB_CONFIG_NAME);
            String path = properties.getProperty(ConsensusConstant.DB_DATA_PATH, ConsensusConstant.DB_DATA_DEFAULT_PATH);
            RocksDBService.init(path);
            TestUtil.initTable(1);
        }catch (Exception e){
            Log.error(e);
        }
        SpringLiteContext.init(ConsensusConstant.CONTEXT_PATH);
        configService = SpringLiteContext.getBean(ConfigService.class);
    }

    @Test
    public void saveConfig()throws Exception{
        ConfigBean configBean = new ConfigBean();
        configBean.setAssetId(1);
        //configBean.setChainId(1);
        configBean.setChainId(2);
        configBean.setBlockMaxSize(5242880);
        configBean.setPackingInterval(10);
        System.out.println(configService.save(configBean,1));
        getConfig();
        getConfigList();
    }

    @Test
    public void getConfig(){
        ConfigBean configBean = configService.get(1);
        assertNotNull(configBean);
        System.out.println(configBean.getChainId());
    }

    @Test
    public void deleteConfig(){
        System.out.println(configService.delete(1));
        getConfig();
    }

    @Test
    public void getConfigList(){
        Map<Integer,ConfigBean> configBeanMap=configService.getList();
        assertNotNull(configBeanMap);
        System.out.println(configBeanMap.size());
    }
}
