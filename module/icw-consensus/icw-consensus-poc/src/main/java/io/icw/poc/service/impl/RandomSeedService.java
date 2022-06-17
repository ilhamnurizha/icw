package io.icw.poc.service.impl;


import io.icw.base.data.BlockExtendsData;
import io.icw.base.data.BlockHeader;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.model.ArraysTool;
import io.icw.poc.constant.ConsensusConstant;
import io.icw.poc.model.po.RandomSeedPo;
import io.icw.poc.model.po.RandomSeedStatusPo;
import io.icw.poc.storage.RandomSeedsStorageService;
import io.icw.poc.utils.RandomSeedUtils;

/**
 * @author Niels
 */
@Component
public class RandomSeedService {

    @Autowired
    private RandomSeedsStorageService randomSeedsStorageService;

    public void processBlock(int chainId, BlockHeader header, byte[] prePackingAddress) {
        byte[] headerPackingAddress = header.getPackingAddress(chainId);
        byte[] nextSeed = null;
        if (ArraysTool.arrayEquals(headerPackingAddress, RandomSeedUtils.CACHE_SEED.getAddress())) {
            nextSeed = RandomSeedUtils.CACHE_SEED.getNextSeed();
        }
        BlockExtendsData extendsData = header.getExtendsData();
        byte[] seed = extendsData.getSeed();
        RandomSeedStatusPo po = this.randomSeedsStorageService.getAddressStatus(chainId, headerPackingAddress);
        long preHeight = 0;

        // pierre test comment out
        if (null == po || ArraysTool.arrayEquals(prePackingAddress, headerPackingAddress) || !ArraysTool.arrayEquals(RandomSeedUtils.getLastDigestEightBytes(extendsData.getSeed()), po.getSeedHash())) {
            seed = ConsensusConstant.EMPTY_SEED;
        }
        //if (null == po || !ArraysTool.arrayEquals(RandomSeedUtils.getLastDigestEightBytes(extendsData.getSeed()), po.getSeedHash())) {
        //    seed = ConsensusConstant.EMPTY_SEED;
        //}
        if (null != po) {
            preHeight = po.getHeight();
        }
        randomSeedsStorageService.saveAddressStatus(chainId, headerPackingAddress, header.getHeight(), nextSeed, extendsData.getNextSeedHash());
        randomSeedsStorageService.saveRandomSeed(chainId, header.getHeight(), preHeight, seed, extendsData.getNextSeedHash());
    }

    public void rollbackBlock(int chainId, BlockHeader header) {
        RandomSeedPo po = randomSeedsStorageService.getSeed(chainId, header.getHeight());
        randomSeedsStorageService.deleteRandomSeed(chainId, header.getHeight());
        byte[] headerPackingAddress = header.getPackingAddress(chainId);
        if (null == po || po.getPreHeight() == 0L) {
            randomSeedsStorageService.deleteAddressStatus(chainId, headerPackingAddress);
        } else {
            randomSeedsStorageService.saveAddressStatus(chainId, headerPackingAddress, po.getPreHeight(), po.getSeed(), RandomSeedUtils.getLastDigestEightBytes(po.getSeed()));
        }
    }
}
