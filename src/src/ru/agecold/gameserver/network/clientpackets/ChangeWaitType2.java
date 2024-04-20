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

import ru.agecold.gameserver.instancemanager.CastleManager;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2StaticObjectInstance;
import ru.agecold.gameserver.network.serverpackets.ChairSit;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class ChangeWaitType2 extends L2GameClientPacket
{
	private boolean _typeStand;

	@Override
	protected void readImpl()
	{
		_typeStand = (readD() == 1);
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
			return;
			
		L2Object target = player.getTarget();

		if (player.isOutOfControl())
		{
			player.sendActionFailed();
			return;
		}

		if (player.getMountType() != 0) //prevent sit/stand if you riding
			return;
		if (target != null && !player.isSitting() && target instanceof L2StaticObjectInstance && ((L2StaticObjectInstance)target).getType() == 1 && CastleManager.getInstance().getCastle(target) != null && player.isInsideRadius(target, L2StaticObjectInstance.INTERACTION_DISTANCE, false, false))
		{
			ChairSit cs = new ChairSit(player,((L2StaticObjectInstance)target).getStaticObjectId());
			player.sendPacket(cs);
			player.sitDown();
			player.broadcastPacket(cs);
		}
				
		if (_typeStand)
			player.standUp();
		else
			player.sitDown();
	}
}
