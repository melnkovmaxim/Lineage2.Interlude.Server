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
package ru.agecold.gameserver.network.serverpackets;

/**
 * Format: (ch) dd
 *
 * @author  -Wooden-
 */
public class PledgeSkillListAdd extends L2GameServerPacket
{
	private int _id;
	private int _lvl;

	public PledgeSkillListAdd(int id, int lvl)
	{
		_id = id;
		_lvl = lvl;
	}

	/**
	 * @see ru.agecold.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3a);

		writeD(_id);
		writeD(_lvl);
	}

	/**
	 * @see ru.agecold.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "S.PledgeSkillListAdd";
	}
}