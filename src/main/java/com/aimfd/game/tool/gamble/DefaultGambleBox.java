package com.aimfd.game.tool.gamble;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 默认支持的数据源是List,Map,Set<br>
 * Map迭代器使用entrySet().iterator()
 * 
 * @author AIM
 *
 */
public class DefaultGambleBox implements IGambleBox {
	protected Object source;
	private IGambleWeight weightInterface;
	private IGambleCount countInterface;
	private IGambleResult resultInterface;

	public DefaultGambleBox(Object source, IGambleWeight weightInterface) {
		this.source = source;
		this.weightInterface = weightInterface;
	}

	public DefaultGambleBox(Object oddsSource, IGambleWeight weightInterface, IGambleCount countInterface, IGambleResult resultInterface) {
		this(oddsSource, weightInterface);
		this.countInterface = countInterface;
		this.resultInterface = resultInterface;
	}

	@Override
	public IGambleWeight getIGambleWeight() {
		return weightInterface;
	}

	@Override
	public IGambleCount getIGambleCount() {
		return this.countInterface;
	}

	@Override
	public IGambleResult getIGambleResult() {
		return this.resultInterface;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator<?> getIterator() {
		if (source instanceof Map) {
			return ((Map) source).entrySet().iterator();
		} else if (source instanceof List) {
			return ((List) source).iterator();
		} else if (source instanceof Set) {
			return ((Set) source).iterator();
		} else {
			throw new RuntimeException("无法识别该数据结构,无法进行迭代 " + source.getClass());
		}
	}

}
