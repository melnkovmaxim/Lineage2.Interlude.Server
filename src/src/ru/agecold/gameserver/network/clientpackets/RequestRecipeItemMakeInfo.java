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
import ru.agecold.gameserver.network.serverpackets.RecipeItemMakeInfo;

/**
 */
public final class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
	private int _id;
	private L2PcInstance _activeChar;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_activeChar = getClient().getActiveChar();
	}

	@Override
	protected void runImpl()
	{
		sendPacket(new RecipeItemMakeInfo(_id, _activeChar));
	}

    /* (non-Javadoc)
     * @see ru.agecold.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
	public String getType()
    {
        return "C.RecipeItemMakeInfo";
    }
}
