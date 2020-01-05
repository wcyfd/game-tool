package com.aimfd.game.tool.trade;

import lombok.Builder;
import lombok.Getter;

/**
 * 交易器
 * 
 * @author wcy
 *
 */
@Builder
@Getter
public class Trade {
	/** 单价 */
	private int price;
	/** 数量 */
	private int count;
	/** 资金 */
	private int fund;

	public boolean trade() {
		int totalPrice = price * count;
		if (totalPrice <= this.fund) {
			return true;
		}
		return false;
	}

	/**
	 * 获取最后总价格
	 * 
	 * @return
	 */
	public int getTotalPrice() {
		return price * count;
	}
}
