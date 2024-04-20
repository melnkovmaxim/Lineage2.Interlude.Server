/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.agecold.gameserver.model;

import javolution.util.FastMap;
import ru.agecold.gameserver.datatables.SkillTable;
import ru.agecold.gameserver.model.L2Skill;
import ru.agecold.gameserver.model.L2Skill.SkillTargetType;
import scripts.skills.ISkillHandler;
import scripts.skills.SkillHandler;
import ru.agecold.gameserver.network.serverpackets.MagicSkillLaunched;
import ru.agecold.gameserver.network.serverpackets.MagicSkillUser;

/**
 *
 * @author  kombat
 */
public class ChanceSkillList extends FastMap<L2Skill, ChanceCondition> {

    private static final long serialVersionUID = 1L;
    private L2Character _owner;

    public ChanceSkillList(L2Character owner) {
        super();
        shared("ChanceSkillList.ChanceSkillList");
        _owner = owner;
    }

    public L2Character getOwner() {
        return _owner;
    }

    public void setOwner(L2Character owner) {
        _owner = owner;
    }

    public void onHit(L2Character target, boolean ownerWasHit, boolean wasCrit) {
        int event;
        if (ownerWasHit) {
            event = ChanceCondition.EVT_ATTACKED | ChanceCondition.EVT_ATTACKED_HIT;
            if (wasCrit) {
                event |= ChanceCondition.EVT_ATTACKED_CRIT;
            }
        } else {
            event = ChanceCondition.EVT_HIT;
            if (wasCrit) {
                event |= ChanceCondition.EVT_CRIT;
            }
        }

        onEvent(event, target);
    }

    public void onSkillHit(L2Character target, boolean ownerWasHit, boolean wasMagic, boolean wasOffensive) {
        int event;
        if (ownerWasHit) {
            event = ChanceCondition.EVT_HIT_BY_SKILL;
            if (wasOffensive) {
                event |= ChanceCondition.EVT_HIT_BY_OFFENSIVE_SKILL;
                event |= ChanceCondition.EVT_ATTACKED;
            } else {
                event |= ChanceCondition.EVT_HIT_BY_GOOD_MAGIC;
            }
        } else {
            event = ChanceCondition.EVT_CAST;
            event |= wasMagic ? ChanceCondition.EVT_MAGIC : ChanceCondition.EVT_PHYSICAL;
            event |= wasOffensive ? ChanceCondition.EVT_MAGIC_OFFENSIVE : ChanceCondition.EVT_MAGIC_GOOD;
        }

        onEvent(event, target);
    }

    public void onEvent(int event, L2Character target) {
        for (FastMap.Entry<L2Skill, ChanceCondition> e = head(), end = tail(); (e = e.getNext()) != end;) {
            if (e.getValue() != null && e.getValue().trigger(event)) {
                makeCast(e.getKey(), target);
            }
        }
    }

    private void makeCast(L2Skill skill, L2Character target) {
        try {
            if (skill.getWeaponDependancy(_owner, true)) {
                L2Skill skillTemp = SkillTable.getInstance().getInfo(skill.getChanceTriggeredId(), skill.getChanceTriggeredLevel());
                if (skillTemp != null) {
                    skill = skillTemp;
                }

                if (target.isDebuffImmun(skill)) {
                    return;
                }

                _owner.setLastTrigger();
                //ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
                //L2Object[] targets = skill.getTargetList(_owner, false, target);

                //_owner.broadcastPacket(new MagicSkillLaunched(_owner, skill.getDisplayId(), skill.getLevel(), targets));
                _owner.broadcastPacket(new MagicSkillUser(_owner, target, skill.getDisplayId(), skill.getLevel(), 0, 0));

                // Launch the magic skill and calculate its effects
                if (skill.getTargetType() == SkillTargetType.TARGET_SELF) {
                    target = _owner;
                }

                SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()).getEffects(target, target);
                //if (handler != null)
                //handler.useSkill(_owner, skill, target);
                //else
                //skill.useSkill(_owner, target);
            }
        } catch (Exception e) {
        }
    }
}