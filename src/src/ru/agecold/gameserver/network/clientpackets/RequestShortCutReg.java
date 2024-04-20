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

import ru.agecold.gameserver.model.L2ShortCut;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.serverpackets.ShortCutRegister;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestShortCutReg extends L2GameClientPacket
{
	private int _type;
	private int _id;
	private int _slot;
	private int _page;
	private int _unk;


	@Override
	protected void readImpl()
	{
		_type = readD();
		int slot = readD();
		_id = readD();
		_unk = readD();

		_slot = slot % 12;
		_page = slot / 12;

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		    return;

		/*if(System.currentTimeMillis() - player.gCPF() < 500)
			return;
			
		player.sCPF();
		
		if(player.isOutOfControl())
			return;*/
		
		switch (_type)
		{
			case 0x01:	// item
			case 0x03:	// action
			case 0x04:	// macro
            case 0x05:  // recipe
			{
				L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, -1, _unk);
				sendPacket(new ShortCutRegister(sc));
				player.registerShortCut(sc);
				break;
			}
			case 0x02:	// skill
			{
				int level = player.getSkillLevel( _id );
				if (level > 0)
				{
					L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, level, _unk);
					sendPacket(new ShortCutRegister(sc));
					player.registerShortCut(sc);
				}
				break;
			}
		}

	}
}
