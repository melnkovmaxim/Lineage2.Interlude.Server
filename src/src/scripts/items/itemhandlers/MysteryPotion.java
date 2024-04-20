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
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PlayableInstance;
import ru.agecold.gameserver.network.SystemMessageId;
import ru.agecold.gameserver.network.serverpackets.MagicSkillUser;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.gameserver.taskmanager.EffectTaskManager;

/**
 * This class ...
 *
 * @version $Revision: 1.1.6.4 $ $Date: 2005/04/06 18:25:18 $
 */
public class MysteryPotion implements IItemHandler {

    private static final int[] ITEM_IDS = {5234};
    private static final int BIGHEAD_EFFECT = 0x2000;
    private static final int MYSTERY_POTION_SKILL = 2103;
    private static final int EFFECT_DURATION = 1200000; // 20 mins

    public void useItem(L2PlayableInstance playable, L2ItemInstance item) {
        if (!(playable.isPlayer())) {
            return;
        }
        L2PcInstance activeChar = (L2PcInstance) playable;
		//item.getItem().getEffects(item, activeChar);

        // Use a summon skill effect for fun ;)
        activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2103, 1, 0, 0));

        activeChar.startAbnormalEffect(BIGHEAD_EFFECT);
        activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);

        activeChar.sendPacket(SystemMessage.id(SystemMessageId.USE_S1).addSkillName(MYSTERY_POTION_SKILL));
        EffectTaskManager.getInstance().schedule(new MysteryPotionStop(playable), EFFECT_DURATION);
    }

    public static class MysteryPotionStop implements Runnable {

        private L2PlayableInstance _playable;

        public MysteryPotionStop(L2PlayableInstance playable) {
            _playable = playable;
        }

        public void run() {
            try {
                if (!(_playable.isPlayer())) {
                    return;
                }

                ((L2PcInstance) _playable).stopAbnormalEffect(BIGHEAD_EFFECT);
            } catch (Throwable t) {
            }
        }
    }

    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
