/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package ru.agecold.gameserver.network.clientpackets;

import java.util.List;

import javolution.util.FastList;
import ru.agecold.Config;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.datatables.ItemTable;
import ru.agecold.gameserver.instancemanager.CastleManorManager;
import ru.agecold.gameserver.instancemanager.CastleManorManager.CropProcure;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.L2Manor;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.actor.instance.L2ManorManagerInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.SystemMessageId;
import ru.agecold.gameserver.network.serverpackets.ActionFailed;
import ru.agecold.gameserver.network.serverpackets.InventoryUpdate;
import ru.agecold.gameserver.network.serverpackets.StatusUpdate;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.gameserver.templates.L2Item;
import ru.agecold.gameserver.util.Util;

@SuppressWarnings("unused")
public class RequestBuyProcure extends L2GameClientPacket 
{
	private int _listId;
	private int _count;
	private int[] _items;
	private List<CropProcure> _procureList = new FastList<CropProcure>();

	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count > 500) // protect server
		{
			_count = 0;
			return;
		}

		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			long servise = readD();
			int itemId   = readD(); _items[i * 2 + 0] = itemId;
			long cnt      = readD();
			if (cnt > Integer.MAX_VALUE || cnt < 1)
			{
				_count=0; _items = null;
				return;
			}
			_items[i * 2 + 1] = (int)cnt;
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null) return;


		// Alt game - Karma punishment
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0) return;

		L2Object target = player.getTarget();

        if(_count < 1)
        {
            player.sendActionFailed();
            return;
        }

        long subTotal = 0;
        int tax = 0;

    	// Check for buylist validity and calculates summary values
        int slots = 0;
        int weight = 0;
        L2ManorManagerInstance manor = (target != null && target instanceof L2ManorManagerInstance) ? (L2ManorManagerInstance)target : null;


		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i * 2 + 0];
			int count  = _items[i * 2 + 1];
			int price = 0;
			if (count > Integer.MAX_VALUE)
			{
				//Util.handleIllegalPlayerAction(player,"Warning!! Character "+player.getName()+" of account "+player.getAccountName()+" tried to purchase over "+Integer.MAX_VALUE+" items at the same time.",  Config.DEFAULT_PUNISH);
				player.sendPacket(Static.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}

			L2Item template = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(
					itemId,manor.getCastle().getCrop(itemId,CastleManorManager.PERIOD_CURRENT).getReward()));
	        weight += count * template.getWeight();

			if (!template.isStackable()) slots += count;
			else if (player.getInventory().getItemByItemId(itemId) == null) slots++;
		}

		if (!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(Static.WEIGHT_LIMIT_EXCEEDED);
			return;
		}


		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(Static.SLOTS_FULL);
			return;
		}

		// Proceed the purchase
		InventoryUpdate playerIU = new InventoryUpdate();
		_procureList =  manor.getCastle().getCropProcure(CastleManorManager.PERIOD_CURRENT);

		for (int i=0; i < _count; i++)
		{
			int itemId = _items[i * 2 + 0];
			int count  = _items[i * 2 + 1];
			if (count < 0) count = 0;

			int rewradItemId=L2Manor.getInstance().getRewardItem(
					itemId,manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward());

			int rewradItemCount = 1; //L2Manor.getInstance().getRewardAmount(itemId, manor.getCastle().getCropReward(itemId));

			rewradItemCount = count / rewradItemCount;

			// Add item to Inventory and adjust update packet
			L2ItemInstance item = player.getInventory().addItem("Manor",rewradItemId,rewradItemCount,player,manor);
			L2ItemInstance iteme = player.getInventory().destroyItemByItemId("Manor",itemId,count,player,manor);

			if (item == null || iteme == null)
				continue;

			playerIU.addRemovedItem(iteme);
			if (item.getCount() > rewradItemCount) playerIU.addModifiedItem(item);
			else playerIU.addNewItem(item);

			// Send Char Buy Messages
			player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(rewradItemId).addNumber(rewradItemCount));

			//manor.getCastle().setCropAmount(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getAmount() - count);
		}

		// Send update packets
		player.sendPacket(playerIU);

		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
}
