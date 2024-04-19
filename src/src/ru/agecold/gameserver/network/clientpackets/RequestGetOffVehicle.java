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

import ru.agecold.gameserver.instancemanager.BoatManager;
import ru.agecold.gameserver.model.actor.instance.L2BoatInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.serverpackets.GetOffVehicle;


/**
 * @author Maktakien
 *
 */
public final class RequestGetOffVehicle extends L2GameClientPacket
{
	 private int _id, _x, _y, _z;

	@Override
	protected void readImpl()
	{
		_id  = readD();
		_x  = readD();
		_y  = readD();
		_z  = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
			return;

		if(System.currentTimeMillis() - player.gCPAM() < 500)
			return;

		player.sCPAM();
		
        L2BoatInstance boat = BoatManager.getInstance().GetBoat(_id);
        GetOffVehicle Gof = new GetOffVehicle(player,boat,_x,_y,_z);
        player.broadcastPacket(Gof);
	}
}