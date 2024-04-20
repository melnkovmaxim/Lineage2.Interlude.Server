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

import java.util.logging.Logger;

import ru.agecold.Config;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PetInstance;
import ru.agecold.gameserver.network.serverpackets.EnchantResult;
import ru.agecold.gameserver.util.IllegalPlayerAction;
import ru.agecold.gameserver.util.Util;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/29 23:15:33 $
 */
public final class RequestGetItemFromPet extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestGetItemFromPet.class.getName());

	private int _objectId;
	private int _amount;
	@SuppressWarnings("unused")
    private int _unknown;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount   = readD();
		_unknown  = readD();// = 0 for most trades
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
        if (player == null || player.getPet() == null || !(player.getPet().isPet())) 
			return;

		if(System.currentTimeMillis() - player.gCPU() < 400)
		{
			player.sendActionFailed();
			return;
		}

		player.sCPU();

        if (player.getActiveEnchantItem() != null)
        {
			player.setActiveEnchantItem(null); 
			player.sendPacket(new EnchantResult(0, true));
			player.sendActionFailed();
			return;
        } 
		
		if (player.getActiveTradeList() != null) 
		{
			player.cancelActiveTrade(); 
			player.sendActionFailed();
			return;
		} 
		
		if (player.getActiveWarehouse() != null)
		{
			player.cancelActiveWarehouse();
			player.sendActionFailed();
			return;
		}
		
        L2PetInstance pet = (L2PetInstance)player.getPet();

        if(_amount < 0)
        {
        	//Util.handleIllegalPlayerAction(player,"[RequestGetItemFromPet] count < 0! ban! oid: "+_objectId+" owner: "+player.getName(),Config.DEFAULT_PUNISH);
        	return;
        }
        else if(_amount == 0)
        	return;

		if (pet.transferItem("Transfer", _objectId, _amount, player.getInventory(), player, pet) == null)
		{
			_log.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
	}
}
