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
package scripts.commands.usercommandhandlers;

import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IUserCommandHandler;

/**
 * Support for /dismount command.
 * @author Micht
 */
public class DisMount implements IUserCommandHandler {

    private static final int[] COMMAND_IDS = {62};

    /* (non-Javadoc)
     * @see ru.agecold.gameserver.handler.IUserCommandHandler#useUserCommand(int, ru.agecold.gameserver.model.L2PcInstance)
     */
    @Override
    public synchronized boolean useUserCommand(int id, L2PcInstance player) {
        if (id != COMMAND_IDS[0]) {
            return false;
        }

        if (player.isRentedPet()) {
            player.stopRentPet();
        } else if (player.isMounted()) {
            player.tryUnmounPet(player.getPet());
        }

        return true;
    }

    /* (non-Javadoc)
     * @see ru.agecold.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    @Override
    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
