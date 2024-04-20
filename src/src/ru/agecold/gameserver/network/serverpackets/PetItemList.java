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

import java.util.logging.Logger;

import javolution.util.FastTable;
import ru.agecold.Config;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.actor.instance.L2PetInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class PetItemList extends L2GameServerPacket
{
	private static Logger _log = Logger.getLogger(PetItemList.class.getName());
	private L2PetInstance _pet;
	private FastTable<L2ItemInstance> _items;

	public PetItemList(L2PetInstance pet)
	{
		_pet = pet;
		_items = new FastTable<L2ItemInstance>();
		_items.addAll(pet.getInventory().listItems());
		//if (Config.DEBUG)
		//{
		//	L2ItemInstance[] items = _activeChar.getInventory().getItems();
		///	for (L2ItemInstance temp : items)
		//	{
		///		_log.fine("item:" + temp.getItem().getName() +
		//				" type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
		//	}
		//}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xB2);

		writeH(_items.size());

		for (int i = (_items.size() -1); i > -1; i--)
		{
			L2ItemInstance temp = _items.get(i);
			if (temp == null || temp.getItem() == null)
				continue;
				
			writeH(temp.getItem().getType1()); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			//System.out.println("#### "+temp.getCount());
			writeH(temp.getItem().getType2());	// item type2
			writeH(0xff);	// ?
			if (temp.isEquipped())
			{
				writeH(0x01);
			}
			else
			{
				writeH(0x00);
			}
			writeD(temp.getItem().getBodyPart());	// rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
//			writeH(temp.getItem().getBodyPart());	// rev 377  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
			writeH(temp.getEnchantLevel());	// enchant level
			writeH(0x00);	// ?
		}
		_items.clear();
	}
}
