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

import ru.agecold.gameserver.datatables.NpcTable;
import scripts.skills.ISkillHandler;
import ru.agecold.gameserver.idfactory.IdFactory;
import ru.agecold.gameserver.instancemanager.CastleManager;
import ru.agecold.gameserver.instancemanager.SiegeManager;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.L2Skill;
import ru.agecold.gameserver.model.L2Skill.SkillType;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2SiegeFlagInstance;
import ru.agecold.gameserver.model.entity.Castle;
import ru.agecold.gameserver.network.SystemMessageId;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import javolution.util.FastList;

/**
 * @author _drunk_
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SiegeFlag implements ISkillHandler
{
    //private static Logger _log = Logger.getLogger(SiegeFlag.class.getName());
	private static final SkillType[] SKILL_IDS = {SkillType.SIEGEFLAG};

    public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
    {
        if (activeChar == null || !(activeChar.isPlayer())) return;

        L2PcInstance player = (L2PcInstance)activeChar;

        if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId()) return;

        Castle castle = CastleManager.getInstance().getCastle(player);

        if (castle == null || !checkIfOkToPlaceFlag(player, castle, true)) 
			return;
        try
        {
            // Spawn a new flag
            L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062));
            flag.setTitle(player.getClan().getName());
            if (skill.getId() == 326)
				flag.setCurrentHpMp(flag.getMaxHp()*2, flag.getMaxMp());
			else
				flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());	
            flag.setHeading(player.getHeading());
            flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
            castle.getSiege().addFlag(player.getClan(), flag);
        }
        catch (Exception e)
        {
            player.sendMessage("Error placing flag:" + e);
        }
		//targets.clear();
    }

    public SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }

    /**
     * Return true if character clan place a flag<BR><BR>
     *
     * @param activeChar The L2Character of the character placing the flag
     * @param isCheckOnly if false, it will send a notification to the player telling him
     * why it failed
     */
    public static boolean checkIfOkToPlaceFlag(L2Character activeChar, boolean isCheckOnly)
    {
        return checkIfOkToPlaceFlag(activeChar, CastleManager.getInstance().getCastle(activeChar), isCheckOnly);
    }

    public static boolean checkIfOkToPlaceFlag(L2Character activeChar, Castle castle, boolean isCheckOnly)
    {
        if (activeChar == null || !(activeChar.isPlayer()))
            return false;

        SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
        L2PcInstance player = (L2PcInstance)activeChar;

        if (castle == null || castle.getCastleId() <= 0)
            sm.addString("�� ���������� ����� ��� ��������� �����");
        else if (!castle.getSiege().getIsInProgress())
            sm.addString("��������� ����� �������� ������ �� ����� �����");
        else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
            sm.addString("������ ������������������ �� ����� ����� ������� ����");
        else if (player.getClan() == null || !player.isClanLeader())
            sm.addString("������ ���������� ����� ������� ����");
        else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount())
        	sm.addString("�� ������ ��������� ������ 1 ����");
		else if (!player.isInSiegeFlagArea())
			sm.addString("�� ���������� ����� ��� ��������� �����");
        else
            return true;

        if (!isCheckOnly) {player.sendPacket(sm);}
        return false;
    }
}
