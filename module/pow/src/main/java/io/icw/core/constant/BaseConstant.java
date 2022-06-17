package io.icw.core.constant;


public class BaseConstant {
    /**
     * 涓荤綉鍜屾祴璇曠綉鐨勯粯璁hainID
     */
    public static final short MAINNET_CHAIN_ID = 1;
    public static final short TESTNET_CHAIN_ID = 2;

    public static final String MAINNET_DEFAULT_ADDRESS_PREFIX = "EDAO";
    public static final String TESTNET_DEFAULT_ADDRESS_PREFIX = "tEDAO";
    /**
     * hash length
     */
    public static final int ADDRESS_LENGTH = 23;

    /**
     * 榛樿鐨勫湴鍧�绫诲瀷锛屼竴鏉￠摼鍙互鍖呭惈鍑犵鍦板潃绫诲瀷锛屽湴鍧�绫诲瀷鍖呭惈鍦ㄥ湴鍧�涓�
     * The default address type, a chain can contain several address types, and the address type is contained in the address.
     */
    public static byte DEFAULT_ADDRESS_TYPE = 1;

    /**
     * 鏅鸿兘鍚堢害鍦板潃绫诲瀷
     * contract address type
     */
    public static byte CONTRACT_ADDRESS_TYPE = 2;

    /**
     * 澶氶噸绛惧悕鍦板潃
     * contract address type
     */
    public static byte P2SH_ADDRESS_TYPE = 3;

    /**
     * 涓荤綉杩愯涓殑鐗堟湰锛岄粯璁や负1锛屼細鏍规嵁閽卞寘鏇存柊鍒扮殑鍧楃殑鏈�鏂扮増鏈仛淇敼
     */
    public static volatile Integer MAIN_NET_VERSION = 1;

    /**
     * utxo閿佸畾鏃堕棿鍒嗙晫鍊�
     * 灏忎簬璇ュ�艰〃绀烘寜鐓ч珮搴﹂攣瀹�
     * 澶т簬璇ュ�艰〃绀烘寜鐓ф椂闂撮攣瀹�
     */
    public static long BlOCKHEIGHT_TIME_DIVIDE = 1000000000000L;

    /**
     * 鍑哄潡闂撮殧鏃堕棿锛堢锛�
     * Block interval time.
     * unit:second
     */
    public static long BLOCK_TIME_INTERVAL_SECOND = 10;
    /**
     * 妯″潡缁熶竴娑堟伅澶勭悊鍣≧PC鎺ュ彛
     */
    public static final String MSG_PROCESS = "msgProcess";

    /**
     * 妯″潡缁熶竴浜ゆ槗楠岃瘉鍣≧PC鎺ュ彛
     */
    public static final String TX_VALIDATOR = "txValidator";

    /**
     * 妯″潡缁熶竴浜ゆ槗鎻愪氦RPC鎺ュ彛
     */
    public static final String TX_COMMIT = "txCommit";

    /**
     * 妯″潡缁熶竴浜ゆ槗鍥炴粴RPC鎺ュ彛
     */
    public static final String TX_ROLLBACK = "txRollback";
}
