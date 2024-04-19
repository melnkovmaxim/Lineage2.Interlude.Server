package ai;

import ru.agecold.L2DatabaseFactory;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PlayableInstance;
import ru.agecold.mysql.Close;
import ru.agecold.mysql.Connect;
import scripts.items.IItemHandler;
import scripts.items.ItemHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BonusChest implements IItemHandler {

    //item id; reward id, reward count,reward id, reward count, item id; reward id, reward count
    private String[] data = {"17040;12222,5,11974,2,12019,10,14555,2"};
    private static int[] ITEM_IDS = null;

    //attempts
    private int attempts = 7;

    public BonusChest() {
        ITEM_IDS = new int[(data.length + 1)];
        for (int i = 0; i < data.length; i++) {
            ITEM_IDS[i] = Integer.valueOf(data[i].split(";")[0]);
        }
        ItemHandler.getInstance().registerItemHandler(this);
    }

    public static void main(String... arguments) {
        new BonusChest();
    }

    @Override
    public void useItem(L2PlayableInstance playable, L2ItemInstance item) {
        if (!playable.isPlayer())
            return;

        L2PcInstance player = (L2PcInstance) playable;
        if (player.isAllSkillsDisabled()) {
            player.sendActionFailed();
            return;
        }

        if (player.isInOlympiadMode()) {
            player.sendPacket(Static.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
            player.sendActionFailed();
            return;
        }

        Connect con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            con = L2DatabaseFactory.get();
            con.setTransactionIsolation(1);
            st = con.prepareStatement("SELECT amount,time FROM bonus_chest WHERE item_obj=?");
            st.setInt(1, item.getObjectId());
            rs = st.executeQuery();
            //boolean have = false;
            if (rs.next()) {
                int att = rs.getInt("amount");
                long time = rs.getLong("time");
                if (System.currentTimeMillis() < time) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    Date resultdate = new Date(time);
                    player.sendMessage("Сундук сейчас закрыт. Откроется завтра в " + sdf.format(resultdate) + ".");
                    //Close.CSR(con, st, rs);
                    //return;
                } else {
                    att++;
                    reward(player, item);
                    if (att >= attempts) {
                        player.destroyItem("Consume", item.getObjectId(), 1, null, false);
                        st = con.prepareStatement("DELETE FROM bonus_chest WHERE item_obj=?");
                        st.setInt(1, item.getObjectId());
                        st.execute();
                    } else {
                        st = con.prepareStatement("REPLACE INTO bonus_chest (item_obj, amount, time) values(?,?,?)");
                        st.setInt(1, item.getObjectId());
                        st.setInt(2, att);
                        st.setLong(3, System.currentTimeMillis() + (1000 * 60 * 60 * 24));
                        st.execute();
                        st.close();
                    }
                }
                //have = true;
            } else {
                st = con.prepareStatement("REPLACE INTO bonus_chest (item_obj, amount, time) values(?,?,?)");
                st.setInt(1, item.getObjectId());
                st.setInt(2, 1);
                st.setLong(3, System.currentTimeMillis() + (1000 * 60 * 60 * 24));
                st.execute();
                reward(player, item);
                st.close();
            }
            Close.SR(st, rs);
        } catch (SQLException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Error BonusChest: " + e);
            e.printStackTrace();
        } finally {
            Close.CSR(con, st, rs);
        }
    }

    private void reward(L2PcInstance player, L2ItemInstance item) {
        for (String aData : data) {
            if (Integer.valueOf(aData.split(";")[0]) == item.getItemId()) {
                for (int j = 0; j < aData.split(";")[1].split(",").length; j += 2) {
                    player.addItem("reawrd", Integer.valueOf(aData.split(";")[1].split(",")[j]), Integer.valueOf(aData.split(";")[1].split(",")[j + 1]), null, true);
                }
                player.sendItems(false);
            }
        }
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
