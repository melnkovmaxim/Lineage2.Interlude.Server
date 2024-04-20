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

import javolution.util.FastTable;

import ru.agecold.gameserver.model.L2ShortCut;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * ShortCutInit
 * format   d *(1dddd)/(2ddddd)/(3dddd)
 *
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShortCutInit extends L2GameServerPacket {

    private FastTable<L2ShortCut> _shortCuts;
    private L2PcInstance _activeChar;

    public ShortCutInit(L2PcInstance activeChar) {
        _activeChar = activeChar;

        if (_activeChar == null) {
            return;
        }
        _shortCuts = new FastTable<L2ShortCut>();
        _shortCuts.addAll(_activeChar.getAllShortCuts());
    }

    @Override
    protected final void writeImpl() {
        writeC(0x45);
        writeD(_shortCuts.size());

        for (int i = 0, n = _shortCuts.size(); i < n; i++) {
            write(_shortCuts.get(i));
        }
    }

    private void write(L2ShortCut sc) {
        if (sc == null) {
            return;
        }

        writeD(sc.getType());
        writeD(sc.getSlot() + sc.getPage() * 12);

        switch (sc.getType()) {
            case L2ShortCut.TYPE_ITEM: //1
                writeD(sc.getId());
                writeD(0x01);
                writeD(-1);
                writeD(0x00);
                writeD(0x00);
                writeH(0x00);
                writeH(0x00);
                break;
            case L2ShortCut.TYPE_SKILL: //2
                writeD(sc.getId());
                writeD(sc.getLevel());
                writeC(0); // C5
                writeD(0); // C6
                break;
            default:
                writeD(sc.getId());
                writeD(0x01); // C6
                break;
            /*case L2ShortCut.TYPE_ACTION: //3
            case L2ShortCut.TYPE_MACRO: //4
            case L2ShortCut.TYPE_RECIPE: //5
            writeD(sc.getId());
            writeD(0x01); // C6
            break;*/
        }
    }

    @Override
    public void gc() {
        _shortCuts.clear();
        _shortCuts = null;
    }
}
