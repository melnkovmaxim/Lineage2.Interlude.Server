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

import java.nio.BufferUnderflowException;

import ru.agecold.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * cS
 * @version $Revision: 1.1.2.2.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRecipeShopMessageSet extends L2GameClientPacket
{
    private String _name;

    @Override
	protected void readImpl()
    {
		try
		{
			_name = readS();
		}
		catch (BufferUnderflowException e)
		{
			_name = "";
		}
	}

	@Override
	protected void runImpl()
	{
		if (_name.length() < 1)
			return;

        L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

        if (player.getCreateList() != null)
            player.getCreateList().setStoreName(_name);
    }
}
