/* This program is free software; you can redistribute it and/or modify
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
import ru.agecold.gameserver.instancemanager.CursedWeaponsManager;
import ru.agecold.gameserver.model.CursedWeapon;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.network.serverpackets.ExCursedWeaponLocation;
import ru.agecold.gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;
import ru.agecold.util.Point3D;

/**
 * Format: (ch)
 * @author  -Wooden-
 */
public final class RequestCursedWeaponLocation extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		//nothing to read it's just a trigger
	}

	/**
	 * @see ru.agecold.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected
	void runImpl()
	{
		L2Character player = getClient().getActiveChar();
		if (player == null)
			return;

		List<CursedWeaponInfo> list = new FastList<CursedWeaponInfo>();
		for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			if (!cw.isActive()) continue;

			Point3D pos = cw.getWorldPosition();

			if (pos != null)
				list.add(new CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
		}


		//send the ExCursedWeaponLocation
		if (!list.isEmpty())
		{
			player.sendPacket(new ExCursedWeaponLocation(list));
		}
		//list.clear();
	}
}
