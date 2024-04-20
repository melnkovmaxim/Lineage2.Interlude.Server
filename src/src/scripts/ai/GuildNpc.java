package scripts.ai;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import javolution.util.FastMap;
import ru.agecold.Config;
import ru.agecold.L2DatabaseFactory;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.templates.L2NpcTemplate;
import ru.agecold.mysql.Close;
import ru.agecold.mysql.Connect;

public class GuildNpc extends L2NpcInstance {

    public GuildNpc(int objectId, L2NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(L2PcInstance player, String command) {
        if (command.startsWith("invite")) {
            if (player.isInGuild()) {
                player.sendHtmlMessage("�� ��� �������� � �������.");
                return;
            }
            int id = Integer.parseInt(command.substring(7));

            if (Config.GUILD_BALANCE_TEAM) {
                if (isGuildFull(id)) {
                    player.sendHtmlMessage("� ������ ������� ��� ��������� ����!");
                    return;
                }
            }

            if (Config.GUILD_MOD_COIN > 0 && !player.destroyItemByItemId("GuildNpc", Config.GUILD_MOD_COIN, Config.GUILD_MOD_PRICE, player, true)) {
                player.sendHtmlMessage("��������� ���������� " + Config.GUILD_MOD_PRICE + " " + Config.GUILD_MOD_COIN_NAME);
                return;
            }

            player.setGuildSide(id);
            player.setGuildPenalty(0);
            player.giveItem(Config.GUILD_MOD_MASKS.get(id), 1);
            if (player.getClan() != null) {
                player.getClan().updateClanMember(player, true);
            }

            if (player.getParty() != null) {
                player.getParty().updateMembers();
            }
            player.teleToLocation(player.getX(), player.getY(), player.getZ());

            Connect con = null;
            PreparedStatement st = null;
            try {
                con = L2DatabaseFactory.get();

                st = con.prepareStatement("INSERT INTO `z_guild_mod` (`char_id`, `side`, `penalty`) VALUES (?,?,?)");
                st.setInt(1, player.getObjectId());
                st.setInt(2, id);
                st.setInt(3, 0);
                st.execute();
            } catch (SQLException e) {
                _log.severe("Could not insert z_guild_mod: " + e);
            } finally {
                Close.CS(con, st);
            }

            player.sendHtmlMessage("�����������, �� �������� � ����: " + Config.GUILD_MOD_NAMES.get(id));
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    private boolean isGuildFull(int id) {
        Map<Integer, Integer> guilds = new FastMap<Integer, Integer>().shared("GuildNpc.guilds");
        for (Integer num : Config.GUILD_MOD_NAMES.keySet()) {
            if (num == null) {
                continue;
            }
            guilds.put(num, 0);
        }
        Connect con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            con = L2DatabaseFactory.get();
            con.setTransactionIsolation(1);
            st = con.prepareStatement("SELECT side FROM `z_guild_mod`");
            rs = st.executeQuery();
            while (rs.next()) {
                int side = rs.getInt("side");
                //Integer count = guilds.get(side);
                guilds.put(side, guilds.get(side) + 1);
            }
        } catch (final SQLException e) {
            _log.warning("isGuildFull(int id) error: " + e);
        } finally {
            Close.CSR(con, st, rs);
        }

        if (guilds.isEmpty()) {
            return false;
        }

        int current = guilds.get(id);
        for (Map.Entry<Integer, Integer> entry : guilds.entrySet()) {
            Integer side = entry.getKey();
            if (side == null) {
                continue;
            }
            if (side == id) {
                continue;
            }
            Integer count = entry.getValue();
            if (count == null) {
                continue;
            }

            if (current > count) {
                return true;
            }
        }
        return false;
    }
}
