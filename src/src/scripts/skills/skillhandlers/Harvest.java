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
package scripts.skills.skillhandlers;

import ru.agecold.Config;
import scripts.skills.ISkillHandler;
import ru.agecold.gameserver.model.L2Attackable;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.L2Skill;
import ru.agecold.gameserver.model.L2Skill.SkillType;
import ru.agecold.gameserver.model.actor.instance.L2MonsterInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.SystemMessageId;
import ru.agecold.gameserver.network.serverpackets.InventoryUpdate;
import ru.agecold.gameserver.network.serverpackets.ItemList;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.util.Rnd;
import javolution.util.FastList;
import javolution.util.FastList.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author  l3x
 */
public class Harvest implements ISkillHandler {
	private static Log _log = LogFactory.getLog(Harvest.class.getName());
    private static final SkillType[] SKILL_IDS = {SkillType.HARVEST};

    private L2PcInstance _activeChar;
    private L2MonsterInstance _target;

    public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets) 
	{
        if (!(activeChar.isPlayer()))
            return;

        _activeChar = (L2PcInstance) activeChar;

		InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

		for (FastList.Node<L2Object> n = targets.head(), end = targets.tail(); (n = n.getNext()) != end;)
		{
			L2Object obj = n.getValue();
	    	if (obj == null || !(obj.isL2Monster()))
	            continue;

	        _target = (L2MonsterInstance) obj;

	        if (_activeChar != _target.getSeeder()) {
	        	SystemMessage sm = SystemMessage.id(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
	        	_activeChar.sendPacket(sm);
	        	continue;
	        }

	        boolean send = false;
	        int total = 0;
	        int cropId = 0;

	        // TODO: check items and amount of items player harvest
	        if (_target.isSeeded()) {
	         	if (calcSuccess()) {
	         		L2Attackable.RewardItem[] items = _target.takeHarvest();
	 	            if (items != null && items.length > 0) {
	 	                for (L2Attackable.RewardItem ritem : items) {
	 	                    cropId = ritem.getItemId(); // always got 1 type of crop as reward
	 	                    if (_activeChar.isInParty())
	 	                    	_activeChar.getParty().distributeItem(_activeChar, ritem, true, _target);
	 	                    else {
	 	                        L2ItemInstance item = _activeChar.getInventory().addItem("Manor", ritem.getItemId(), ritem.getCount(), _activeChar, _target);
	 	                        if (iu != null) iu.addItem(item);
	 	                        send = true;
	 	                        total += ritem.getCount();
	 	                    }
	 	                }
	 	                if (send) {
	 	                    SystemMessage smsg = SystemMessage.id(SystemMessageId.YOU_PICKED_UP_S1_S2);
	 	                    smsg.addNumber(total);
	 	                    smsg.addItemName(cropId);
	 	                    _activeChar.sendPacket(smsg);
	 	                    if (_activeChar.getParty() != null) {
	 	                    	smsg = SystemMessage.id(SystemMessageId.S1_HARVESTED_S3_S2S);
		 	                    smsg.addString(_activeChar.getName());
		 	                    smsg.addNumber(total);
		 	                    smsg.addItemName(cropId);
		 	       	    		_activeChar.getParty().broadcastToPartyMembers(_activeChar, smsg);
		 	       	    	}

	 	                    if (iu != null) _activeChar.sendPacket(iu);
	 	            		else _activeChar.sendPacket(new ItemList(_activeChar, false));
	 	                }
	 	            }
	         	} else {
	         		_activeChar.sendPacket(SystemMessage.id(SystemMessageId.THE_HARVEST_HAS_FAILED));
	         	}
	         } else {
	             _activeChar.sendPacket(SystemMessage.id(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN));
	         }
    	}
		//targets.clear();
    }

    private boolean calcSuccess() {
        int basicSuccess = 100;
        int levelPlayer = _activeChar.getLevel();
        int levelTarget = _target.getLevel();

        int diff = (levelPlayer - levelTarget);
        if(diff < 0)
            diff = -diff;

        // apply penalty, target <=> player levels
        // 5% penalty for each level
        if(diff > 5) {
            basicSuccess -= (diff-5) * 5;
        }

        // success rate cant be less than 1%
        if(basicSuccess < 1)
            basicSuccess = 1;

        int rate = Rnd.nextInt(99);

        if(rate < basicSuccess)
            return true;
        return false;
    }

    public SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
