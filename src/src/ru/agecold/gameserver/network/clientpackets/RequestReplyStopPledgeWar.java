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
package ru.agecold.gameserver.network.clientpackets;

import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.datatables.ClanTable;
import ru.agecold.gameserver.model.L2Clan;
import ru.agecold.gameserver.model.L2ClanMember;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.SystemMessageId;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestReplyStopPledgeWar extends L2GameClientPacket {

    private int _answer;

    @Override
    protected void readImpl() {
        @SuppressWarnings("unused")
        String _reqName = readS();
        _answer = readD();
    }

    @Override
    protected void runImpl() {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        L2PcInstance requestor = player.getTransactionRequester();
        if (requestor == null) {
            return;
        }

        if (!requestor.isInZonePeace() || !player.isInZonePeace()) {
            player.sendMessage("�������� �������� � ���������� ���� ��� � ������.");
            requestor.sendMessage("�������� �������� � ���������� ���� ��� � ������.");
            player.setTransactionRequester(null);
            requestor.onTransactionResponse();
            return;
        }

        L2Clan playerClan = player.getClan();
        if (playerClan == null) {
            player.setTransactionRequester(null);
            requestor.onTransactionResponse();
            return;
        }

        L2Clan requestorClan = player.getClan();
        if (requestorClan == null) {
            player.setTransactionRequester(null);
            requestor.onTransactionResponse();
            return;
        }

        for (L2ClanMember member : playerClan.getMembers()) {
            if (member == null || member.getPlayerInstance() == null) {
                continue;
            }

            if (AttackStanceTaskManager.getInstance().getAttackStanceTask(member.getPlayerInstance())) {
                player.sendMessage("������ �������� �����, ����� ���� �� ������ ����� " + playerClan.getName() + " ��������� � ���.");
                requestor.sendMessage("������ �������� �����, ����� ���� �� ������ ����� " + playerClan.getName() + " ��������� � ���.");
                player.setTransactionRequester(null);
                requestor.onTransactionResponse();
                return;
            }
        }

        for (L2ClanMember member : requestorClan.getMembers()) {
            if (member == null || member.getPlayerInstance() == null) {
                continue;
            }

            if (AttackStanceTaskManager.getInstance().getAttackStanceTask(member.getPlayerInstance())) {
                player.sendMessage("������ �������� �����, ����� ���� �� ������ ����� " + requestorClan.getName() + " ��������� � ���.");
                requestor.sendMessage("������ �������� �����, ����� ���� �� ������ ����� " + requestorClan.getName() + " ��������� � ���.");
                player.setTransactionRequester(null);
                requestor.onTransactionResponse();
                return;
            }
        }

        if (_answer == 1) {
            ClanTable.getInstance().deleteclanswars(requestor.getClanId(), player.getClanId());
        } else {
            requestor.sendPacket(Static.REQUEST_TO_END_WAR_HAS_BEEN_DENIED);
        }

        player.setTransactionRequester(null);
        requestor.onTransactionResponse();
    }

    /* (non-Javadoc)
     * @see ru.agecold.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType() {
        return "C.ReplyStopPledgeWar";
    }
}
