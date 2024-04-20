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

import ru.agecold.gameserver.ai.CtrlIntention;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.L2World;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PetInstance;
import ru.agecold.gameserver.model.actor.instance.L2SummonInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.4 $ $Date: 2005/03/29 23:15:33 $
 */
public final class RequestPetGetItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2World world = L2World.getInstance();
		L2ItemInstance item = (L2ItemInstance)world.findObject(_objectId);
		
		L2PcInstance player = getClient().getActiveChar();
		
		if (item == null || player == null)
		    return;

		if(System.currentTimeMillis() - player.gCPY() < 100)
		{
			player.sendActionFailed();
			return;
		}

		player.sCPY();
			
		if(player.getPet().isSummon())
		{
			player.sendActionFailed();
			return;
		}
		L2PetInstance pet = (L2PetInstance)player.getPet();
		if (pet == null || pet.isDead() || pet.isOutOfControl())
		{
			player.sendActionFailed();
			return;
		}
		pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
	}
}
