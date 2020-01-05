package com.aimfd.game.tool.reserve;

import lombok.Builder;
import lombok.Getter;

/**
 * 库存器,用于检验是否库存允许改变
 * 
 * @author wcy
 *
 */

@Builder
@Getter
public class Reserve {
	private int store;// 目前存储量
	private boolean max;// 是否存在最大存储量
	private int capacity;// 最大存储量
	private int delta;// 改变量
	private boolean fill;// 是否塞满
	private boolean useAll;// 是否消费完,有多少用多少

	private int $remainDelta;// 剩下的改变量
	private int $remainCount;// 剩下的库存
	private int $realDelta;// 真正变动的量

	public ReserveBuilder toBuilder() {
		return Reserve.builder().store(store).max(max).capacity(capacity).delta(delta).fill(fill).useAll(useAll);
	}

	private void clear() {
		$remainCount = 0;
		$realDelta = 0;
		$remainDelta = 0;
	}

	/**
	 * 执行数量改变，根据builder中的要求进行变更
	 * 
	 * @return 变更成功返回true，否则false
	 */
	public boolean transfer() {
		this.clear();
		if (delta >= 0) {
			if (max) {// 有最大存储量
				if (fill) {// 是否塞满
					int value = store + delta;
					if (value > capacity) {// 超出容量
						$remainCount = capacity;
						$remainDelta = value - capacity;
						$realDelta = capacity - store;
					} else {
						$remainCount = value;
						$realDelta = delta;
					}

					return true;// 要塞满肯定返回true
				} else {
					if ((store + delta) <= capacity) {
						$remainCount = store + delta;
						$realDelta = delta;
						return true;
					}
					return false;
				}
			} else {
				$remainCount = store + delta;
				$realDelta = delta;
				return true;
			}
		} else {
			if (store >= Math.abs(delta)) {
				$remainCount = store + delta;
				$realDelta = delta;
				return true;
			} else {
				if (useAll) {
					$remainCount = 0;
					$realDelta = -store;
					return true;
				}
			}

			return false;
		}
	}

	/**
	 * 获取真正变动的量
	 * 
	 * @return
	 */
	public int getRealDelta() {
		return $realDelta;
	}

	/**
	 * 获取剩下的库存
	 * 
	 * @return
	 */
	public int getRemainCount() {
		return $remainCount;
	}

	/**
	 * 获取剩下的改变量
	 * 
	 * @return
	 */
	public int getRemainDelta() {
		return $remainDelta;
	}

	/**
	 * 库存是否空了
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return $remainCount == 0;
	}

}
