package ai;

import ru.agecold.L2DatabaseFactory;
import ru.agecold.gameserver.Announcements;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.mysql.Close;
import ru.agecold.mysql.Connect;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AnnTopClanRep extends QuestJython
{
    private int time = 14400;

	public AnnTopClanRep(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		
        ThreadPoolManager.getInstance().scheduleGeneral(new Announce(), time * 1000);
	}
	
    private class Announce implements Runnable {
        @Override
        public void run() {
            Announcements.getInstance().announceToAll("=====Топ кланов=====\n");
            Connect con = null;
            PreparedStatement st = null;
            ResultSet rs = null;
            try {
                con = L2DatabaseFactory.get();
                con.setTransactionIsolation(1);
                st = con.prepareStatement("SELECT reputation_score,clan_name FROM `clan_data` ORDER BY `reputation_score` DESC LIMIT 3");
                rs = st.executeQuery();
                int position = 0;
                while (rs.next()) {
                    int clanRep = rs.getInt("reputation_score");
                    String name = rs.getString("clan_name");
                    position++;
                    Announcements.getInstance().announceToAll(position + ". " + name + " - " + clanRep + " крп.");
                }
                Close.SR(st, rs);
            } catch (SQLException e) {
                _log.warning("Error Announce Clan Rep: " + e);
            } finally {
                Close.CSR(con, st, rs);
            }
            Announcements.getInstance().announceToAll("=================");
            ThreadPoolManager.getInstance().scheduleGeneral(new Announce(), time * 1000);
        }
    }

	public static void main(String[] args)
	{
		new AnnTopClanRep(-1, "AnnTopClanRep", "ai");
	}
}