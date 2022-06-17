package io.icw.api.rpc.controller.runner;

/**
 * 交易类型
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/24 18:47
 */
public enum TxTypeEnum {
    COIN_BASE("出块奖励","COIN BASE",1),
    TRANSFER("转账","TRANSFER",2),
    ACCOUNT_ALIAS("设置账户别名","ACCOUNT ALIAS",3),
    REGISTER_AGENT("新建共识节点","REGISTER AGENT",4),
    DEPOSIT("委托参与共识","DEPOSIT",5),
    CANCEL_DEPOSIT("取消委托","CANCEL DEPOSIT",6),
    YELLOW_PUNISH("黄牌","YELLOW PUNISH",7),
    RED_PUNISH("红牌","RED PUNISH",8),
    STOP_AGENT("注销共识节点","STOP AGENT",9),
    CROSS_CHAIN("跨链转账","CROSS CHAIN",10),
    REGISTER_CHAIN_AND_ASSET("注册链","REGISTER CHAIN AND ASSET",11),
    DESTROY_CHAIN_AND_ASSET("注销链","DESTROY CHAIN AND ASSET",12),
    ADD_ASSET_TO_CHAIN("为链新增一种资产","ADD ASSET TO CHAIN",13),
    REMOVE_ASSET_FROM_CHAIN("删除链上资产","REMOVE ASSET FROM CHAIN",14),
    CREATE_CONTRACT("创建智能合约","CREATE CONTRACT",15),
    CALL_CONTRACT("调用智能合约","CALL CONTRACT",16),
    DELETE_CONTRACT("删除智能合约","DELETE CONTRACT",17),
    CONTRACT_TRANSFER("合约内部转账","CONTRACT TRANSFER",18),
    CONTRACT_RETURN_GAS("合约执行手续费返还","CONTRACT RETURN GAS",19),
    CONTRACT_CREATE_AGENT("合约新建共识节点","CONTRACT CREATE AGENT",20),
    CONTRACT_DEPOSIT("合约委托参与共识","CONTRACT DEPOSIT",21),
    CONTRACT_CANCEL_DEPOSIT("合约取消委托共识","CONTRACT CANCEL DEPOSIT",22),
    CONTRACT_STOP_AGENT("合约注销共识节点","CONTRACT STOP AGENT",23),
    VERIFIER_CHANGE("验证人变更","VERIFIER CHANGE",24),
    VERIFIER_INIT("验证人初始化","VERIFIER INIT",25),
    CONTRACT_TOKEN_CROSS_TRANSFER("合约token跨链转账","CONTRACT TOKEN CROSS TRANSFER",26),
    LEDGER_ASSET_REG_TRANSFER("账本链内资产注册登记","LEDGER ASSET REG TRANSFER",27),
    APPEND_AGENT_DEPOSIT("追加节点保证金","APPEND AGENT DEPOSIT",28),
    REDUCE_AGENT_DEPOSIT("撤销节点保证金","REDUCE AGENT DEPOSIT",29),
    QUOTATION("喂价交易","QUOTATION",30),
    FINAL_QUOTATION("最终喂价交易","FINAL QUOTATION",31),
    BATCH_WITHDRAW("批量退出staking交易","BATCH WITHDRAW",32),
    BATCH_STAKING_MERGE("合并活期staking记录","BATCH STAKING MERGE",33),
    COIN_TRADING("创建交易对","COIN TRADING",228),
    TRADING_ORDER("挂单委托","TRADING ORDER",229),
    TRADING_ORDER_CANCEL("挂单撤销","TRADING ORDER CANCEL",230),
    TRADING_DEAL("挂单成交","TRADING DEAL",231),
    EDIT_COIN_TRADING("修改交易对","EDIT COIN TRADING",232),
    ORDER_CANCEL_CONFIRM("撤单交易确认","ORDER CANCEL CONFIRM",233),
    CONFIRM_CHANGE_VIRTUAL_BANK("确认 虚拟银行变更交易","CONFIRM CHANGE VIRTUAL BANK",40),
    CHANGE_VIRTUAL_BANK("虚拟银行变更交易","CHANGE VIRTUAL BANK",41),
    RECHARGE("链内充值交易","RECHARGE",42),
    WITHDRAWAL("提现交易","WITHDRAWAL",43),
    CONFIRM_WITHDRAWAL("确认提现成功状态交易","CONFIRM WITHDRAWAL",44),
    PROPOSAL("发起提案交易","PROPOSAL",45),
    VOTE_PROPOSAL("对提案进行投票交易","VOTE PROPOSAL",46),
    DISTRIBUTION_FEE("异构链交易手续费补贴","DISTRIBUTION FEE",47),
    INITIALIZE_HETEROGENEOUS("虚拟银行初始化异构链","INITIALIZE HETEROGENEOUS",48),
    HETEROGENEOUS_CONTRACT_ASSET_REG_PENDING("异构链合约资产注册等待","HETEROGENEOUS CONTRACT ASSET REG PENDING",49),
    HETEROGENEOUS_CONTRACT_ASSET_REG_COMPLETE("异构链合约资产注册完成","HETEROGENEOUS CONTRACT ASSET REG COMPLETE",50),
    CONFIRM_PROPOSAL("确认提案执行交易","CONFIRM PROPOSAL",51),
    RESET_HETEROGENEOUS_VIRTUAL_BANK("重置异构链(合约)虚拟银行","RESET HETEROGENEOUS VIRTUAL BANK",52),
    CONFIRM_HETEROGENEOUS_RESET_VIRTUAL_BANK("确认重置异构链(合约)虚拟银行","CONFIRM HETEROGENEOUS RESET VIRTUAL BANK",53),
    RECHARGE_UNCONFIRMED("异构链充值待确认交易","RECHARGE UNCONFIRMED",54),
    WITHDRAWAL_HETEROGENEOUS_SEND("异构链提现已发布到异构链网络","WITHDRAWAL HETEROGENEOUS SEND",55),
    WITHDRAWAL_ADDITIONAL_FEE("追加提现手续费","WITHDRAWAL ADDITIONAL FEE",56),
    HETEROGENEOUS_MAIN_ASSET_REG("异构链主资产注册","HETEROGENEOUS MAIN ASSET REG",57),
    REGISTERED_CHAIN_CHANGE("已注册跨链的链信息变更","REGISTERED CHAIN CHANGE",60),
    RESET_LOCAL_VERIFIER_LIST("重置跨链模块存储的本链验证人列表","RESET LOCAL VERIFIER LIST",61);

    private String cnName;
	private String enName;
    private Integer index;

    TxTypeEnum(String cnName,String enName, Integer index) {
        this.cnName=cnName;
        this.enName=enName;
        this.index=index;
    }

	public static TxTypeEnum getByIndex(int index) {
        TxTypeEnum[] values = TxTypeEnum.values();
		for (TxTypeEnum value : values) {
			if (value.getIndex()==index) {
				return value;
			}
		}
		return null;
	}
    public static Integer getIndex(String cnName) {
        TxTypeEnum[] types = values();
        for (TxTypeEnum type : types) {
            if (type.cnName().equals(cnName)) {
                return type.index();
            }
        }
        return null;
    }

    public static String getCnName(Integer index) {
        TxTypeEnum[] values = values();
        for (TxTypeEnum value : values) {
            if (value.index()==index) {
                return value.cnName();
            }
        }
        return null;
    }
	public static String getEnName(Integer index) {
        TxTypeEnum[] values = values();
		for (TxTypeEnum value : values) {
			if (value.index()==index) {
				return value.getEnName();
			}
		}
		return null;
	}

    private String cnName() {
        return this.cnName;
    }

	private String enName() {
		return this.enName;
	}

    private int index() {
        return this.index;
    }

    public Integer getOrdinal() {
		return this.index;
    }

    public Integer getValue() {
        return null;
    }

    public String getCnName() {
        return cnName;
    }

    public String getEnName() {
        return enName;
    }

    public Integer getIndex() {
        return index;
    }
}
