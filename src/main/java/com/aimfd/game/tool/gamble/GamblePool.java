package com.aimfd.game.tool.gamble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * 赌池 赌池内可以分组
 * 
 * @author AIM
 *
 */
@Slf4j
public class GamblePool {

	private final Map<String, GambleBoxWeight> boxGroup = new HashMap<>();
	private Random r = new Random();
	private boolean autoBox;

	/**
	 * 如果为true,则只会在有库存的库里找,避免其中一个库没有货之后返回空的问题
	 * 
	 * @param value
	 */
	public void setAutoSwitchBox(boolean value) {
		this.autoBox = value;
	}

	/**
	 * 添加仓库
	 * 
	 * @param name
	 * @param box
	 * @param weight
	 */
	public void addBox(String name, IGambleBox box, int weight) {
		GambleBoxWeight boxWeight = new GambleBoxWeight();
		boxWeight.box = box;
		boxWeight.weight = weight;

		boxGroup.put(name, boxWeight);
	}

	/**
	 * 删除库
	 * 
	 * @param name
	 */
	public void removeBox(String name) {
		boxGroup.remove(name);
	}

	private int getRandomBetween(int min, int max) {
		if (min == max)
			return min;
		return r.nextInt(max - min + 1) + min;
	}

	private IGambleBox randomBox(Set<Object> set) {

		Iterable<GambleBoxWeight> iterable = null;
		int total = 0;
		if (autoBox) {
			List<GambleBoxWeight> list = new ArrayList<>();
			iterable = list;
			// 获取还有库存的库
			for (GambleBoxWeight boxWeight : this.boxGroup.values()) {
				Iterator<?> it = boxWeight.box.getIterator();
				while (it.hasNext()) {
					Object obj = it.next();
					if (null != set && set.contains(obj)) {
						continue;
					} else {
						list.add(boxWeight);
						break;
					}
				}
			}
		} else {
			iterable = this.boxGroup.values();
		}

		for (GambleBoxWeight boxWeight : iterable) {
			total += boxWeight.weight;
		}

		int random = getRandomBetween(0, total);

		for (GambleBoxWeight boxWeight : iterable) {
			IGambleBox box = boxWeight.box;
			int weight = boxWeight.weight;

			if (random <= weight) {
				return box;
			} else {
				random -= weight;
			}
		}

		return null;
	}

	/**
	 * 获取一个单一值，没有排除值
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T find() {
		IGambleBox box = randomBox(null);
		if (box == null)
			return null;

		int random = this.getRandomBetween(0, getTotalElementWeightInBox(box, null));

		Iterator<?> it = box.getIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			int weight = box.getIGambleWeight().getWeight(obj);
			if (random <= weight) {
				return (T) obj;
			} else {
				random -= weight;
			}
		}

		return null;
	}

	/**
	 * 获取指定个数的唯一物品
	 * 
	 * @param count
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findUniqueByCount(int count) {
		Set<Object> set = new HashSet<>();
		List<T> list = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			Object obj = findExclude(set);
			if (obj != null) {
				set.add(obj);
			}

			list.add(obj != null ? (T) obj : null);
		}

		return list;
	}

	/**
	 * 除了指定集合中的元素外从池里获取元素<br>
	 * 该方法适用于直接对目标物品进行随机，加入有两个A道具和一个B道具,则概率随机范围是[0,A权重*2+B权重]
	 * 
	 * @param set
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findExclude(Set<Object> set) {
		log.debug("GamblePool.findExclude() exclude {}", set);
		IGambleBox box = randomBox(set);
		if (box == null) {
			log.debug("GamblePool.findExclude() randomBox is null");
			return null;
		}
		log.debug("GamblePool.findExclude() source {}", ((DefaultGambleBox) box).source);

		int random = this.getRandomBetween(0, getTotalElementWeightInBox(box, set));
		Iterator<?> it = box.getIterator();
		log.debug("GamblePool.findExclude() randomValue is {}", random);
		while (it.hasNext()) {
			Object obj = it.next();
			log.debug("GamblePool.findExclude() nextObject is {}", obj);
			if (null != set && set.contains(obj))
				continue;

			int weight = box.getIGambleWeight().getWeight(obj);
			if (random <= weight) {
				return (T) obj;
			} else {
				random -= weight;
			}
		}
		return null;
	}

	private int getTotalElementWeightInBox(IGambleBox box, Set<Object> excludeSet) {
		int total = 0;
		Iterator<?> it = box.getIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (null != excludeSet && excludeSet.contains(obj)) {
				continue;
			}
			total += box.getIGambleWeight().getWeight(obj);
		}
		return total;
	}

	/**
	 * 该方法用于有指定数量，且同一类型物品数量会大于1时，抽出不会超出指定数量物品的物品<br>
	 * 该方法适用于数据源采用概率表，由接口传入现已有的物品数量
	 * 
	 * 
	 * @return
	 */
	public <T> T findWithLimitedCount() {
		return this.findWithLimitedExclude(null);
	}

	/**
	 * 该方法用于有指定数量，且同一类型物品数量会大于1时，抽出的物品不会超出指定数量<br>
	 * 该方法使用于数据源采用概率表，由接口传入现已有的物品数量
	 * 
	 * @param mapCount
	 *            已经排除的数量，用于不同GambPool一起排除，联合使用
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findWithLimitedExclude(Map<Object, Integer> mapCount) {

		IGambleBox box = randomBoxByCount(mapCount);
		if (box == null) {
			log.debug("GamblePool.findExclude() randomBox is null");
			return null;
		}
		log.debug("GamblePool.findExclude() source {}", ((DefaultGambleBox) box).source);

		// 获取总值
		int total = 0;
		{
			Iterator<?> it = box.getIterator();
			while (it.hasNext()) {
				Object odds = it.next();
				if (isValid(box, odds, mapCount)) {
					continue;
				}
				total += box.getIGambleWeight().getWeight(odds);
			}
		}

		int random = this.getRandomBetween(0, total);
		log.debug("GamblePool.findExclude() randomValue is {}", random);

		Iterator<?> it = box.getIterator();
		while (it.hasNext()) {
			Object odds = it.next();// 几率表中的值
			log.debug("GamblePool.findExclude() nextObject is {}", odds);
			// 根据几率表的信息获取对应的实际物品数量
			if (this.isValid(box, odds, mapCount)) {
				continue;
			}

			int weight = box.getIGambleWeight().getWeight(odds);
			if (random <= weight) {
				Object obj = box.getIGambleResult().getResult(odds);
				record(mapCount, box, odds);
				return (T) obj;
			} else {
				random -= weight;
			}
		}
		return null;
	}

	/**
	 * 是否需要跳过
	 * 
	 * @param box
	 * @param odds
	 * @param mapCount
	 * @return
	 */
	private boolean isValid(IGambleBox box, Object odds, Map<Object, Integer> mapCount) {
		if (mapCount == null) {
			return false;
		}
		IGambleCount count = box.getIGambleCount();
		IGambleResult result = box.getIGambleResult();
		return count.getCount(odds) - mapCount.getOrDefault(result.getResult(odds), 0) == 0;
	}

	/**
	 * 记录数量
	 * 
	 * @param mapCount
	 * @param box
	 * @param odds
	 */
	private void record(Map<Object, Integer> mapCount, IGambleBox box, Object odds) {
		if (mapCount != null) {
			Object target = box.getIGambleResult().getResult(odds);
			mapCount.put(target, (mapCount.containsKey(target) ? mapCount.get(target) : 0) + 1);
		}
	}

	private IGambleBox randomBoxByCount(Map<Object, Integer> mapCount) {
		Iterable<GambleBoxWeight> iterable = null;
		int total = 0;
		if (autoBox) {
			List<GambleBoxWeight> list = new ArrayList<>();
			iterable = list;
			// 获取还有库存的库
			for (GambleBoxWeight boxWeight : this.boxGroup.values()) {
				IGambleBox box = boxWeight.box;
				IGambleCount count = box.getIGambleCount();
				IGambleResult result = box.getIGambleResult();

				Iterator<?> it = box.getIterator();
				while (it.hasNext()) {
					Object obj = it.next();

					// 获得已经记录的数量
					int value = mapCount == null ? 0 : mapCount.getOrDefault(result.getResult(obj), 0);
					if (count.getCount(obj) - value == 0) {
						continue;
					} else {
						list.add(boxWeight);
						break;
					}

				}
			}
		} else {
			iterable = this.boxGroup.values();
		}

		for (GambleBoxWeight boxWeight : iterable) {
			total += boxWeight.weight;
		}

		int random = getRandomBetween(0, total);

		for (GambleBoxWeight boxWeight : iterable) {
			IGambleBox box = boxWeight.box;
			int weight = boxWeight.weight;

			if (random <= weight) {
				return box;
			} else {
				random -= weight;
			}
		}

		return null;
	}

}
