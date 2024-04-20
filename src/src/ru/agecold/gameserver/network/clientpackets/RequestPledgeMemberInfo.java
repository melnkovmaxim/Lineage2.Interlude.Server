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

import ru.agecold.gameserver.model.L2Clan;
import ru.agecold.gameserver.model.L2ClanMember;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.serverpackets.PledgeReceiveMemberInfo;

/**
 * Format: (ch) dS
 * @author  -Wooden-
 *
 */
public final class RequestPledgeMemberInfo extends L2GameClientPacket
{
    @SuppressWarnings("unused")
    private int _unk1;
    private String _player;

    @Override
	protected void readImpl()
    {
        _unk1 = readD();
        _player = readS();
    }

    /**
     * @see ru.agecold.gameserver.clientpackets.ClientBasePacket#runImpl()
     */
    @Override
	protected void runImpl()
    {
        //System.out.println("C5: RequestPledgeMemberInfo d:"+_unk1);
        //System.out.println("C5: RequestPledgeMemberInfo S:"+_player);
        L2PcInstance player = getClient().getActiveChar();
        if(player == null)
        	return;
        //do we need powers to do that??
        L2Clan clan = player.getClan();
        if(clan == null)
        	return;
        L2ClanMember member = clan.getClanMember(_player);
        if(member == null)
        	return;
        player.sendPacket(new PledgeReceiveMemberInfo(member));
    }

    /**
     * @see ru.agecold.gameserver.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return "C.PledgeMemberInfo";
    }

}