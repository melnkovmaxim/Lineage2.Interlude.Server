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

import ru.agecold.gameserver.model.actor.instance.L2CubicInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestTargetCanceld extends L2GameClientPacket {

    private int _unselect;

    @Override
    protected void readImpl() {
        _unselect = readH();
    }

    @Override
    protected void runImpl() {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) {
            return;
        }

        if (System.currentTimeMillis() - player.gCPAJ() < 100) {
            return;
        }

        player.sCPAJ();

        for (L2CubicInstance cubic : player.getCubics().values()) {
            if (cubic.getId() != L2CubicInstance.LIFE_CUBIC) {
                cubic.stopAction();
            }
        }

        if (_unselect == 0) {
            if (player.isCastingNow() && player.canAbortCast()) {
                player.abortCast();
            } else if (player.getTarget() != null) {
                player.setTarget(null);
            }
        } else if (player.getTarget() != null) {
            player.setTarget(null);
        }

    }

    /* (non-Javadoc)
     * @see ru.agecold.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType() {
        return "C.TargetCanceld";
    }
}
