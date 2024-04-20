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

import java.util.List;

import javolution.util.FastList;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;


/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 *
 * @author godson
 */
public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	// chdd(dhd)
	private L2PcInstance _player;
	private List<Effect> _effects;


	private static class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _duration;

		public Effect(int pSkillId, int pLevel, int pDuration)
		{
			_skillId = pSkillId;
			_level = pLevel;
			_duration = pDuration;
		}
	}

	public ExOlympiadSpelledInfo(L2PcInstance player)
	{
		_effects = new FastList<Effect>();
        _player = player;
	}

	public void addEffect(int skillId, int level, int duration)
	{
		_effects.add(new Effect(skillId, level, duration));
	}

	@Override
	protected final void writeImpl()
	{
        if (_player == null)
            return;
		writeC(0xfe);
		writeH(0x2a);
		writeD(_player.getObjectId());
		writeD(_effects.size());
        for (Effect temp : _effects)
        {
        	writeD(temp._skillId);
        	writeH(temp._level);
        	writeD(temp._duration/1000);
        }
	}
}
