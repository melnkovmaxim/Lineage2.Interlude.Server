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
import ru.agecold.gameserver.GameTimeController;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.ai.CtrlIntention;
import ru.agecold.gameserver.datatables.SkillTable;
import ru.agecold.gameserver.datatables.MapRegionTable;
import ru.agecold.gameserver.instancemanager.CastleManager;
import ru.agecold.gameserver.instancemanager.CoupleManager;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.L2Skill;
import ru.agecold.gameserver.model.L2Skill.SkillType;
import ru.agecold.gameserver.model.L2World;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.entity.Castle;
import ru.agecold.gameserver.model.entity.TvTEvent;
import ru.agecold.gameserver.network.SystemMessageId;
import ru.agecold.gameserver.network.serverpackets.ActionFailed;
import ru.agecold.gameserver.network.serverpackets.ConfirmDlg;
import ru.agecold.gameserver.network.serverpackets.MagicSkillUser;
import ru.agecold.gameserver.network.serverpackets.SetupGauge;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.gameserver.util.Broadcast;
import scripts.skills.ISkillHandler;
import javolution.util.FastList;
import javolution.util.FastList.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WeddingTP implements ISkillHandler
{
    static final Log _log = LogFactory.getLog(WeddingTP.class);
	private static final SkillType[] SKILL_IDS = {SkillType.WEDDINGTP};

 	public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
	{
		L2PcInstance player = (L2PcInstance)activeChar;
	
		if ((player.getX() >= -150000 && player.getX() <= -60000) && (player.getY() >= -250000 && player.getY() <= -180000))		
			return;
			
        if(!player.isMarried())
        {
			if (!player.getAppearance().getSex())
				player.sendMessage("�� �� ������");
			else
				player.sendMessage("�� �� �������");
            return;
        }

        if(player.getPartnerId()==0)
        {
            player.sendMessage("���, �������� �� ���� ������ ��������������");
            return;
        }
			
        L2PcInstance partner;
        partner = (L2PcInstance)L2World.getInstance().findObject(player.getPartnerId());
		
		if(partner == null)
        {
			player.sendMessage("���� ��������� �� � ����");
            return;
        }
		
		String Sex = "";
		if (!partner.getAppearance().getSex())
			Sex = "���� ����";
		else
			Sex = "��� ���";
		
		Castle castleac = CastleManager.getInstance().getCastle(player);
		Castle castlepn = CastleManager.getInstance().getCastle(partner);

        if(player.isInJail())
        {
            player.sendMessage("�� � ������");
            return;
        }
        else if(player.isInOlympiadMode() || player.getOlympiadSide() > 1)
        {
            player.sendMessage("�� �� ���������");
            return;
        }
        else if(player.atEvent)
        {
            player.sendMessage("�� �� ������");
            return;
        }
        else  if (player.isInDuel())
        {
            player.sendMessage("������ ������");
            return;
        }
        else if (player.inObserverMode())
        {
        	player.sendMessage("�������� ������ ���� ���������");
            return;
        }
        else if(player.getClan() != null
        		&& CastleManager.getInstance().getCastleByOwner(player.getClan()) != null
        		&& CastleManager.getInstance().getCastleByOwner(player.getClan()).getSiege().getIsInProgress())
        {
        	player.sendMessage("�� �� �����");
        	return;
        }
		else if (castleac != null && castleac.getSiege().getIsInProgress())
		{
			player.sendMessage("�� �� �����");
			return;
		}
        else if (player.isFestivalParticipant())
        {
            player.sendMessage("�� �� ���������");
            return;
        }
        else if (player.isInParty() && player.getParty().isInDimensionalRift())
        {
            player.sendMessage("�� � �����");
            return;
        }
        else if (player.isInsideSilenceZone())
        {
            player.sendMessage("����� ������");
            return;
        }
        // Thanks nbd
        else if (!TvTEvent.onEscapeUse(player.getName()))
        {
        	player.sendPacket(new ActionFailed());
			player.sendMessage("�� �� ���");
        	return;
        }
		if (player.isEventWait() || player.getChannel() > 1)
		{
			player.sendMessage("����� ������");
			return;
		}
		
        if(partner.isInJail())
        {
			player.sendMessage(""+Sex+" � ������");
            return;
        }
        else if(partner.isDead())
        {
			player.sendMessage(""+Sex+" �� ��� �����");
            return;
        }
        else if(partner.isInOlympiadMode() || partner.getOlympiadSide() > 1)
        {
			player.sendMessage(""+Sex+" �� ���������");
            return;
        }
        else if(partner.atEvent)
        {
            player.sendMessage(""+Sex+" �� ������");
            return;
        }
        else  if (partner.isInDuel())
        {
            player.sendMessage(""+Sex+" ��������");
            return;
        }
        else if (partner.isFestivalParticipant())
        {
            player.sendMessage(""+Sex+" �� ��������� ����");
            return;
        }
        else if (partner.isInParty() && partner.getParty().isInDimensionalRift())
        {
            player.sendMessage(""+Sex+" � �����");
            return;
        }
        else if (partner.inObserverMode())
        {
        	player.sendMessage(""+Sex+" ��������� �� ����������");
            return;
        }
        else if (partner.isInsideSilenceZone())
        {
            player.sendMessage("����� ������");
            return;
        }
        else if(partner.getClan() != null
        		&& CastleManager.getInstance().getCastleByOwner(partner.getClan()) != null
        		&& CastleManager.getInstance().getCastleByOwner(partner.getClan()).getSiege().getIsInProgress())
        {
        	player.sendMessage(""+Sex+" �� �����");
        	return;
        }
		else if (castlepn != null && castlepn.getSiege().getIsInProgress())
		{
			player.sendMessage(""+Sex+" �� �����");
			return;
		}
		if (partner.isEventWait() || player.getChannel() > 1)
		{
			player.sendMessage("����� ������");
			return;
		}

		player.teleToLocation(partner.getX(), partner.getY(), partner.getZ());
		//targets.clear();
 	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}