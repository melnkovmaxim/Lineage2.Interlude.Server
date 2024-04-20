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
package ru.agecold.gameserver.model.actor.instance;

import ru.agecold.gameserver.ai.CtrlIntention;
import ru.agecold.gameserver.instancemanager.SiegeManager;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.L2SiegeClan;
import ru.agecold.gameserver.model.entity.Siege;
import ru.agecold.gameserver.network.serverpackets.MyTargetSelected;
import ru.agecold.gameserver.network.serverpackets.StatusUpdate;
import ru.agecold.gameserver.network.serverpackets.ValidateLocation;
import ru.agecold.gameserver.templates.L2NpcTemplate;

public class L2SiegeFlagInstance extends L2MonsterInstance
{
    private L2PcInstance _player;
    private Siege _siege;

	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template)
	{
		super(objectId, template);

        _player = player;
        _siege = SiegeManager.getInstance().getSiege(_player.getX(), _player.getY(), _player.getZ());
        if (_player.getClan() == null || _siege == null)
        {
            deleteMe();
        }
        else
        {
            L2SiegeClan sc = _siege.getAttackerClan(_player.getClan());
            if (sc == null)
                deleteMe();
            else
                sc.addFlag(this);
        }
	}
	
    @Override
	public void onSpawn()
    {
		setIsImobilised(true);
		setIsParalyzed(true);
    }

    @Override
	public boolean isAttackable()
    {
        // Attackable during siege by attacker only
        return (getCastle() != null
                && getCastle().getCastleId() > 0
                && getCastle().getSiege().getIsInProgress());
    }

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Attackable during siege by attacker only
		return (attacker != null
		        && attacker.isPlayer()
		        && getCastle() != null
		        && getCastle().getCastleId() > 0
		        && getCastle().getSiege().getIsInProgress());
	}

    @Override
	public boolean doDie(L2Character killer)
    {
    	if (!super.doDie(killer))
    		return false;
    	L2SiegeClan sc = _siege.getAttackerClan(_player.getClan());
        if (sc != null)
        	sc.removeFlag();
        return true;
    }

    @Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (player == null || !canTarget(player))
			return;

			// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int)getStatus().getCurrentHp() );
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
			player.sendPacket(su);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			else
			{
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendActionFailed();
			}
		}
	}
	
    @Override
	public boolean isDebuffProtected()
    {
        return true;
    }
}
