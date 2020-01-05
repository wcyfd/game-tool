package com.aimfd.game.tool.gamble;

import java.util.Iterator;

public interface IGambleBox {

	/**
	 * 获取权重接口
	 * 
	 * @return
	 */
	IGambleWeight getIGambleWeight();

	/**
	 * 迭代器
	 * 
	 * @return
	 */
	Iterator<?> getIterator();

	/**
	 * 获取数量
	 * 
	 * @param object
	 * @return
	 */
	IGambleCount getIGambleCount();

	/**
	 * 获得结果
	 * 
	 * @return
	 */
	IGambleResult getIGambleResult();
}
