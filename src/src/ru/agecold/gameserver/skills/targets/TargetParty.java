package ru.agecold.gameserver.skills.targets;

import javolution.util.FastList;

import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.L2Skill;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.util.Util;

public class TargetParty extends TargetList
{
	public final FastList<L2Object> getTargetList(FastList<L2Object> targets, L2Character activeChar, boolean onlyFirst, L2Character target, L2Skill skill)
	{
		if (onlyFirst)
		{
            targets.add(activeChar);
			return targets;
		}

        targets.add(activeChar);

        L2PcInstance player = null;
		if (activeChar.isPlayer())
        {
            player = activeChar.getPlayer();
            if (player.getPet() != null)
                targets.add(player.getPet());
        }
        else if (activeChar.isL2Summon())
        {
            player = activeChar.getOwner();
            targets.add(player);
        }

		if (activeChar.getParty() == null)
			return targets;

		for(L2PcInstance member : activeChar.getParty().getPartyMembers())
		{
			if (member == null || member.equals(player)) 
				continue;

			if (member.equals(player) || member.isDead()) 
				continue;

			if (skill.getSkillRadius() > 0 && !player.canSeeTarget(member))
				continue;

			if (Util.checkIfInRange(skill.getSkillRadius(), activeChar, member, true))
			{
				targets.add(member);
				if (member.getPet() != null && !member.getPet().isDead())
					targets.add(member.getPet());
			}
		}
		return targets;
	}
}
