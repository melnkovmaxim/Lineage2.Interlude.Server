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

import java.util.Map;

import ru.agecold.gameserver.model.entity.Hero;
import ru.agecold.gameserver.model.entity.olympiad.Olympiad;
import ru.agecold.gameserver.templates.StatsSet;

/**
 * Format: (ch) d [SdSdSdd]
 * d: size
 * [
 * S: hero name
 * d: hero class ID
 * S: hero clan name
 * d: hero clan crest id
 * S: hero ally name
 * d: hero Ally id
 * d: count
 * ]
 * @author -Wooden-
 * Format from KenM
 *
 * Re-written by godson
 *
 */
public class ExHeroList extends L2GameServerPacket {

    private Map<Integer, StatsSet> _heroList;

    public ExHeroList() {
        _heroList = Hero.getInstance().getHeroes();
    }

    /* (non-Javadoc)
     * @see ru.agecold.gameserver.serverpackets.ServerBasePacket#writeImpl()
     */
    @Override
    protected void writeImpl() {
        writeC(0xfe);
        writeH(0x23);

        writeD(_heroList.size());
        for (Integer heroId : _heroList.keySet()) {
            StatsSet hero = _heroList.get(heroId);
            writeS(hero.getString(Olympiad.CHAR_NAME));
            writeD(hero.getInteger(Olympiad.CLASS_ID));
            writeS(hero.getString(Hero.CLAN_NAME, ""));
            writeD(hero.getInteger(Hero.CLAN_CREST, 0));
            writeS(hero.getString(Hero.ALLY_NAME, ""));
            writeD(hero.getInteger(Hero.ALLY_CREST, 0));
            writeD(hero.getInteger(Hero.COUNT));
        }
    }
}