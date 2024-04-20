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
import ru.agecold.gameserver.cache.CrestCache;
import ru.agecold.gameserver.network.serverpackets.AllyCrest;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestAllyCrest extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestAllyCrest.class.getName());

	private int _crestId;
	/**
	 * packet type id 0x88 format: cd
	 *
	 * @param rawPacket
	 */
	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}

	@Override
	protected void runImpl()
	{
		//if (Config.DEBUG) _log.fine("allycrestid " + _crestId + " requested");

        byte[] data = CrestCache.getInstance().getAllyCrest(_crestId);

		if (data != null)
			sendPacket(new AllyCrest(_crestId,data));
		//else
		//{
			//if (Config.DEBUG) _log.fine("allycrest is missing:" + _crestId);
		//}
	}
}
