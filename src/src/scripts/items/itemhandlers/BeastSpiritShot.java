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
package scripts.items.itemhandlers;

import scripts.items.IItemHandler;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.L2Summon;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PlayableInstance;
import ru.agecold.gameserver.network.SystemMessageId;
//import ru.agecold.gameserver.network.serverpackets.ExAutoSoulShot;
import ru.agecold.gameserver.network.serverpackets.MagicSkillUser;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.gameserver.util.Broadcast;

/**
 * Beast SpiritShot Handler
 *
 * @author Tempy
 */
public class BeastSpiritShot implements IItemHandler {
    // All the item IDs that this handler knows.

    private static final int[] ITEM_IDS = {6646, 6647};

    public void useItem(L2PlayableInstance playable, L2ItemInstance item) {
        if (playable == null) {
            return;
        }

        L2PcInstance activeOwner = null;
        if (playable instanceof L2Summon) {
            activeOwner = ((L2Summon) playable).getOwner();
            activeOwner.sendPacket(Static.PET_CANNOT_USE_ITEM);
            return;
        } else if (playable.isPlayer()) {
            activeOwner = (L2PcInstance) playable;
        }

        if (activeOwner == null) {
            return;
        }
        L2Summon activePet = activeOwner.getPet();

        if (activePet == null) {
            activeOwner.sendPacket(Static.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
            return;
        }

        if (activePet.isDead()) {
            activeOwner.sendPacket(Static.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET);
            return;
        }

        int itemId = item.getItemId();
        boolean isBlessed = (itemId == 6647);
        //int shotConsumption = 1;

        if (activePet.getChargedSpiritShot() != L2ItemInstance.CHARGED_NONE) {
            return;
        }

        if (isBlessed) {
            activePet.setChargedSpiritShot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
        } else {
            activePet.setChargedSpiritShot(L2ItemInstance.CHARGED_SPIRITSHOT);
        }

        // Pet uses the power of spirit.
        activeOwner.sendPacket(Static.PET_USE_THE_POWER_OF_SPIRIT);

        Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUser(activePet, activePet, isBlessed ? 2009 : 2008, 1, 0, 0), 360000/*600*/);
    }

    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
