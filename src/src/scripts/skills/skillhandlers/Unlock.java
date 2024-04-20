/* This program is free software; you can redistribute it and/or modify
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

import ru.agecold.gameserver.ai.CtrlIntention;
import scripts.skills.ISkillHandler;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.L2Skill;
import ru.agecold.gameserver.model.L2Skill.SkillType;
import ru.agecold.gameserver.model.actor.instance.L2ChestInstance;
import ru.agecold.gameserver.model.actor.instance.L2DoorInstance;
import ru.agecold.gameserver.network.SystemMessageId;
import ru.agecold.gameserver.network.serverpackets.ActionFailed;
import ru.agecold.gameserver.network.serverpackets.SocialAction;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.gameserver.skills.Formulas;
import ru.agecold.util.Rnd;
import javolution.util.FastList;
import javolution.util.FastList.Node;

public class Unlock implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(Unlock.class.getName());
	private static final SkillType[] SKILL_IDS = {SkillType.UNLOCK};

	public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
	{
		/*FastList<L2Object> targetList = skill.getTargetList(activeChar);

		if (targetList.isEmpty()) 
			return;*/

		for (FastList.Node<L2Object> n = targets.head(), end = targets.tail(); (n = n.getNext()) != end;)
		{
			L2Object target =  n.getValue();

			boolean success = Formulas.calculateUnlockChance(skill);
			if (target.isL2Door())
			{
				L2DoorInstance door = (L2DoorInstance) target;
				/*if (!door.isUnlockable())
				{
					activeChar.sendPacket(Static.FAILED_TO_UNLOCK_DOOR);
					activeChar.sendPacket(new ActionFailed());
					return;
				}*/

				if (success && (!door.getOpen()))
				{
					door.openMe();
					door.onOpen();
					SystemMessage systemmessage = SystemMessage.id(SystemMessageId.S1_S2);

					systemmessage.addString("Unlock the door!");
					activeChar.sendPacket(systemmessage);
				}
				else
					activeChar.sendPacket(Static.FAILED_TO_UNLOCK_DOOR);
			}
			else if (target instanceof L2ChestInstance)
			{
				L2ChestInstance chest = (L2ChestInstance) target;
				if (chest.getCurrentHp() <= 0 || chest.isInteracted())
				{
					activeChar.sendPacket(new ActionFailed());
					return;
				}
				else
				{
					int chestChance = 0;
					int chestGroup = 0;
					int chestTrapLimit = 0;

					if (chest.getLevel() > 60) chestGroup = 4;
					else if (chest.getLevel() > 40) chestGroup = 3;
					else if (chest.getLevel() > 30) chestGroup = 2;
					else chestGroup = 1;

					switch (chestGroup)
					{
						case 1:
						{
							if (skill.getLevel() > 10) chestChance = 100;
							else if (skill.getLevel() >= 3) chestChance = 50;
							else if (skill.getLevel() == 2) chestChance = 45;
							else if (skill.getLevel() == 1) chestChance = 40;

							chestTrapLimit = 10;
						}
							break;
						case 2:
						{
							if (skill.getLevel() > 12) chestChance = 100;
							else if (skill.getLevel() >= 7) chestChance = 50;
							else if (skill.getLevel() == 6) chestChance = 45;
							else if (skill.getLevel() == 5) chestChance = 40;
							else if (skill.getLevel() == 4) chestChance = 35;
							else if (skill.getLevel() == 3) chestChance = 30;

							chestTrapLimit = 30;
						}
							break;
						case 3:
						{
							if (skill.getLevel() >= 14) chestChance = 50;
							else if (skill.getLevel() == 13) chestChance = 45;
							else if (skill.getLevel() == 12) chestChance = 40;
							else if (skill.getLevel() == 11) chestChance = 35;
							else if (skill.getLevel() == 10) chestChance = 30;
							else if (skill.getLevel() == 9) chestChance = 25;
							else if (skill.getLevel() == 8) chestChance = 20;
							else if (skill.getLevel() == 7) chestChance = 15;
							else if (skill.getLevel() == 6) chestChance = 10;

							chestTrapLimit = 50;
						}
							break;
						case 4:
						{
							if (skill.getLevel() >= 14) chestChance = 50;
							else if (skill.getLevel() == 13) chestChance = 45;
							else if (skill.getLevel() == 12) chestChance = 40;
							else if (skill.getLevel() == 11) chestChance = 35;

							chestTrapLimit = 80;
						}
							break;
					}
					if (Rnd.get(100) <= chestChance)
					{
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(),3));
	                    chest.setSpecialDrop();
	                    chest.setMustRewardExpSp(false);
	                    chest.setInteracted();
	                    chest.reduceCurrentHp(99999999, activeChar);
					}
					else
					{
	                    activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(),13));
	                    if (Rnd.get(100) < chestTrapLimit) chest.chestTrap(activeChar);
	                    chest.setInteracted();
	                    chest.addDamageHate(activeChar,0,1);
	                    chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
					}
				}
			}
		}
		//targets.clear();
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
