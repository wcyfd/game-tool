package com.aimfd.game.tool.gamble;

@FunctionalInterface
public interface IGambleWeight {
	/**
	 * 获取权重
	 * 
	 * @param target
	 * @return
	 */
	int getWeight(Object target);
}
