package items;

import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PlayableInstance;
import ru.agecold.gameserver.network.serverpackets.ItemList;
import ru.agecold.util.Rnd;
import scripts.items.IItemHandler;
import scripts.items.ItemHandler;

/**
 * @author: PaiN
 */
public class Surprise implements IItemHandler
{
	private static final int SURPRISE = 13013;

	private static final int[] _itemIds = { SURPRISE };

	private Surprise()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	public static void main(String[] args)
	{
		new Surprise();
	}

	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if(playable == null || !playable.isPlayer())
			return;
		L2PcInstance player = (L2PcInstance) playable;

		if (player.isAllSkillsDisabled())
		{
			player.sendActionFailed();
			return;
		}

		player.destroyItemByItemId("Consume", SURPRISE, 1, player, true);

		int itemId = 57;
		int count = 1;

		if(Rnd.chance(40))
		{
			itemId = 11974;
			count = 1;
			player.addItem("<OpenSurprise1>", itemId, count, player, true);
		}
		if(Rnd.chance(100))
		{
			itemId = 12019;
			count = 10;
			player.addItem("<OpenSurprise2>", itemId, count, player, true);
		}
		if(Rnd.chance(100))
		{
			itemId = 11975;
			count = 100;
			player.addItem("<OpenSurprise3>", itemId, count, player, true);
		}
		if(Rnd.chance(50))
		{
			itemId = 11972;
			count = 10;
			player.addItem("<OpenSurprise4>", itemId, count, player, true);
		}
		if(Rnd.chance(50))
		{
			itemId = 13017;
			count = 1;
			player.addItem("<OpenSurprise5>", itemId, count, player, true);
		}
		if(Rnd.chance(3))
		{
			itemId = 12222;
			count = 1;
			player.addItem("<OpenSurprise6>", itemId, count, player, true);
		}
		if(Rnd.chance(100))
		{
			itemId = 11973;
			count = 150;
			player.addItem("<OpenSurprise7>", itemId, count, player, true);
		}

		player.sendPacket(new ItemList(player, false));
	}

	public final int[] getItemIds()
	{
		return _itemIds;
	}
}