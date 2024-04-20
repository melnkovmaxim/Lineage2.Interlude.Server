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

import ru.agecold.gameserver.model.L2Object;

/**
 * format dddd
 *
 * sample 0000: 3a 69 08 10 48 02 c1 00 00 f7 56 00 00 89 ea ff :i..H.....V.....
 * 0010: ff 0c b2 d8 61 ....a
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class TeleportToLocation extends L2GameServerPacket {

    private int _targetObjId;
    private int _x;
    private int _y;
    private int _z;
    private boolean _fromServer = false;

    /**
     * @param _characters
     */
    public TeleportToLocation(L2Object obj, int x, int y, int z, boolean flag) {
        _targetObjId = obj.getObjectId();
        _x = x;
        _y = y;
        _z = z;
        _fromServer = flag;
    }

    public TeleportToLocation(L2Object obj, int x, int y, int z) {
        _targetObjId = obj.getObjectId();
        _x = x;
        _y = y;
        _z = z;
    }

    @Override
    protected final void writeImpl() {
        if (!_fromServer) {
            return;
        }

        writeC(0x28);
        writeD(_targetObjId);
        writeD(_x);
        writeD(_y);
        writeD(_z);
    }

    /* (non-Javadoc)
     * @see ru.agecold.gameserver.serverpackets.ServerBasePacket#getType()
     */
    @Override
    public String getType() {
        return "S.TeleportToLocation";
    }
}
