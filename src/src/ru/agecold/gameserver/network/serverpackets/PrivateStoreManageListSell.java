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
package ru.agecold.gameserver.network.serverpackets;

import javolution.util.FastList;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.TradeList;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;


/**
 * 3 section to this packet
 * 1)playerinfo which is always sent
 * dd
 *
 * 2)list of items which can be added to sell
 * d(hhddddhhhd)
 *
 * 3)list of items which have already been setup
 * for sell in previous sell private store sell manageent
 * d(hhddddhhhdd) *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PrivateStoreManageListSell extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
	private int _playerAdena;
	private boolean _packageSale;
	private FastList<TradeList.TradeItem> _itemList;
	private FastList<TradeList.TradeItem> _sellList;

	public PrivateStoreManageListSell(L2PcInstance player)
	{
		_activeChar = player;
		_playerAdena = _activeChar.getAdena();
		_activeChar.getSellList().updateItems();
		_packageSale = _activeChar.getSellList().isPackaged();
		_itemList = _activeChar.getInventory().getAvailableItems(_activeChar.getSellList());
		_sellList = _activeChar.getSellList().getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x9a);
		//section 1
		writeD(_activeChar.getObjectId());
		writeD(_packageSale ? 1 : 0); // Package sell
		writeD(_playerAdena);

		//section2
		writeD(_itemList.size()); //for potential sells
		for (FastList.Node<TradeList.TradeItem> n = _itemList.head(), end = _itemList.tail(); (n = n.getNext()) != end;) 
		{
			TradeList.TradeItem item = n.getValue();
			
			L2ItemInstance InvItem = _activeChar.getInventory().getItemByObjectId(item.getObjectId());
			if (InvItem.isAugmented())
				continue;
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getCount());
			writeH(0);
			writeH(item.getEnchant());//enchant lvl
			writeH(0);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice()); //store price
		}
			
		//section 3
		writeD(_sellList.size()); //count for any items already added for sell
		for (FastList.Node<TradeList.TradeItem> n = _sellList.head(), end = _sellList.tail(); (n = n.getNext()) != end;) 
		{
			TradeList.TradeItem item = n.getValue();
			
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getCount());
			writeH(0);
			writeH(item.getEnchant());//enchant lvl
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice());//your price
			writeD(item.getItem().getReferencePrice()); //store price
		}
	}

	/* (non-Javadoc)
	 * @see ru.agecold.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "S.PrivateSellListSell";
	}
}
