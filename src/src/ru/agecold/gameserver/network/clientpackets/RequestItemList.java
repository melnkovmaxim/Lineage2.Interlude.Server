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

import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.serverpackets.EnchantResult;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestItemList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
        if (getClient() != null && getClient().getActiveChar() != null && !getClient().getActiveChar().isInvetoryDisabled())
        {
			L2PcInstance player = getClient().getActiveChar();
			
			if (player == null) 
				return;

			if(System.currentTimeMillis() - player.gCPV() < 100)
			{
				player.sendActionFailed();
				return;
			}

			player.sCPV();
		
			if (player.getActiveTradeList() != null) 
			{
				player.cancelActiveTrade(); 
				player.sendActionFailed();
				return;
			}
			
			if (player.getActiveEnchantItem() != null)
			{
				player.setActiveEnchantItem(null);
				player.sendPacket(new EnchantResult(0, true));
				player.sendActionFailed();
				return;
			} 
			
			if (player.getActiveWarehouse() != null)
			{
				player.cancelActiveWarehouse();
				player.sendActionFailed();
				return;
			}
			
    		player.sendItems(true);
        }
	}
}
