package ru.agecold.gameserver.network.serverpackets;

import javolution.util.FastList;
import ru.agecold.gameserver.model.L2CommandChannel;
import ru.agecold.gameserver.model.L2Party;

/**
 * Update a Command Channel
 * @author SYS
 *
 * Format:
 * ch sddd[sdd]
 * ������ ������ � ���� (828 ��������):
 * fe 31 00
 * 62 00 75 00 73 00 74 00 65 00 72 00 00 00 - ��� ������ ��
 * 00 00 00 00 - ? Looting type
 * 19 00 00 00 - ����� ����� ������� � ��
 * 04 00 00 00 - ����� ����� ������ � ��
 * [
 * 62 00 75 00 73 00 74 00 65 00 72 00 00 00  - ����� ������ 1
 * 36 46 70 4c - ObjId ���� ������ 1
 * 08 00 00 00 - ���������� �������� � ���� 1
 * 4e 00 31 00 67 00 68 00 74 00 48 00 75
 * ]
 */
public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket {

    private String channelLeaderName;
    private int memberCount;
    private FastList<ChannelPartyInfo> parties;

    /**
     * @param L2CommandChannel CommandChannel
     */
    public ExMultiPartyCommandChannelInfo(L2CommandChannel channel) {
        if (channel == null) {
            return;
        }

        channelLeaderName = channel.getChannelLeader().getName();
        memberCount = channel.getMemberCount();

        parties = new FastList<ChannelPartyInfo>();
        for (L2Party party : channel.getPartys()) {
            if (party == null || party.getMemberCount() == 0) {
                continue;
            }

            parties.add(new ChannelPartyInfo(party.getLeader().getName(), party.getPartyLeaderOID(), party.getMemberCount()));
        }
    }

    @Override
    protected void writeImpl() {
        if (parties == null) {
            return;
        }

        writeC(0xFE);
        writeH(0x30);
        writeS(channelLeaderName); // ��� ������ CC
        writeD(0); // Channelloot 0 or 1
        writeD(memberCount); // ����� ����� ������� � ��
        writeD(parties.size()); // ����� ����� ������ � ��

        for (ChannelPartyInfo party : parties) {
            writeS(party.Leader_name); // ��� ������ ������
            writeD(party.Leader_obj_id); // ObjId ���� ������
            writeD(party.MemberCount); // ���������� �������� � ����
        }
        parties.clear();
    }

    static class ChannelPartyInfo {

        public String Leader_name;
        public int Leader_obj_id, MemberCount;

        public ChannelPartyInfo(String _Leader_name, int _Leader_obj_id, int _MemberCount) {
            Leader_name = _Leader_name;
            Leader_obj_id = _Leader_obj_id;
            MemberCount = _MemberCount;
        }
    }
}