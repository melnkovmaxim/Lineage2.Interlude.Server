package items;

import javolution.util.FastMap;

import ru.agecold.gameserver.datatables.SkillTable;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PlayableInstance;
import ru.agecold.gameserver.network.serverpackets.MagicSkillUser;
import scripts.items.ItemHandler;
import scripts.items.IItemHandler;

public class DonateScrolls implements IItemHandler
{
	private final static FastMap<Integer, Integer[]> SCROLLS = new FastMap<Integer, Integer[]>().shared("DonateScrolls.SCROLLS");
	private static int[] ITEM_IDS = null;
	
	public DonateScrolls()
	{
		/**шаблон
		**SCROLLS.put(итем_ид, new Integer[] { ид_баффа, уровень_баффа, ид_скилла_анимации, продолжительность_анимации(мс.)), кушать_скролл(1 да, 0 нет)) });
		**/
		SCROLLS.put(11986, new Integer[] { 9959, 1, 2036, 1, 1 });
		SCROLLS.put(11030, new Integer[] { 8210, 1, 2036, 1, 1 });
		SCROLLS.put(12970, new Integer[] { 1315, 1, 2036, 1, 0 });
		SCROLLS.put(12971, new Integer[] { 1315, 2, 2036, 1, 0 });
		SCROLLS.put(12972, new Integer[] { 1315, 3, 2036, 1, 0 });
		SCROLLS.put(12973, new Integer[] { 1368, 1, 2036, 1, 0 });
		SCROLLS.put(12974, new Integer[] { 1368, 2, 2036, 1, 0 });
		SCROLLS.put(12975, new Integer[] { 1368, 3, 2036, 1, 0 });
		SCROLLS.put(12976, new Integer[] { 1313, 1, 2036, 1, 0 });
		SCROLLS.put(12977, new Integer[] { 1313, 2, 2036, 1, 0 });
		SCROLLS.put(12978, new Integer[] { 1313, 3, 2036, 1, 0 });
		SCROLLS.put(12979, new Integer[] { 1314, 1, 2036, 1, 0 });
		SCROLLS.put(12980, new Integer[] { 1314, 2, 2036, 1, 0 });
		SCROLLS.put(12981, new Integer[] { 1314, 3, 2036, 1, 0 });
		SCROLLS.put(12982, new Integer[] { 1371, 1, 2036, 1, 0 });
		SCROLLS.put(12983, new Integer[] { 1371, 2, 2036, 1, 0 });
		SCROLLS.put(12984, new Integer[] { 1371, 3, 2036, 1, 0 });
		SCROLLS.put(12985, new Integer[] { 1372, 1, 2036, 1, 0 });
		SCROLLS.put(12986, new Integer[] { 1372, 2, 2036, 1, 0 });
		SCROLLS.put(12987, new Integer[] { 1372, 3, 2036, 1, 0 });
		SCROLLS.put(12988, new Integer[] { 1370, 1, 2036, 1, 0 });
		SCROLLS.put(12989, new Integer[] { 1370, 2, 2036, 1, 0 });
		SCROLLS.put(12990, new Integer[] { 1370, 3, 2036, 1, 0 });
		
		
		//
		Integer[] tmp_ids = (Integer[]) SCROLLS.keySet().toArray(new Integer[SCROLLS.size()]);
		ITEM_IDS = toIntArray(tmp_ids);
		tmp_ids = null;
		ItemHandler.getInstance().registerItemHandler(this);
	}

	public static void main (String... arguments )
	{
		new DonateScrolls();
	}

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
   	{
		if (!playable.isPlayer())
			return;

		L2PcInstance player = (L2PcInstance) playable;
		if (player.isAllSkillsDisabled())
		{
			player.sendActionFailed();
			return;
		}
		
		if (player.isInEvent())
		{
			player.sendMessage("Доп скилы не юзаются на Евентах :(");
			player.sendActionFailed();
			return;
		}

		if (player.isInOlympiadMode())
		{
			player.sendPacket(Static.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			player.sendActionFailed();
			return;
		}
		

		Integer[] data = SCROLLS.get(item.getItemId());
		if(data != null)
		{
			player.stopSkillEffects(data[0]);
			SkillTable.getInstance().getInfo(data[0], data[1]).getEffects(player, player);
			player.broadcastPacket(new MagicSkillUser(player, player, data[2], 1, data[3], 0));
			if (data[4] == 1)
				player.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
   	}

	private int[] toIntArray(Integer[] arr)
	{
		int[] ret = new int[arr.length];
		int i = 0;
		for (Integer e : arr)  
			ret[i++] = e.intValue();
		return ret;
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
