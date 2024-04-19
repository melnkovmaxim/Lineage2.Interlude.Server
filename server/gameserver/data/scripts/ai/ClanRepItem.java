package ai;

import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PlayableInstance;
import scripts.items.IItemHandler;
import scripts.items.ItemHandler;

public class ClanRepItem implements IItemHandler {

    //Item id, clan rep, Item id, clan rep
    private int[] data = {17045,30000};
    private static int[] ITEM_IDS = null;

    public ClanRepItem() {
        ITEM_IDS = new int[((data.length / 2) + 1)];
        for (int i = 0; i < data.length; i += 2) {
            ITEM_IDS[i] = data[i];
        }
        ItemHandler.getInstance().registerItemHandler(this);
    }

    public static void main(String... arguments) {
        new ClanRepItem();
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

        for (int i = 0; i < data.length; i += 2) {
            if (item.getItemId() == data[i]) {
                player.getClan().setReputationScore(player.getClan().getReputationScore() + data[i + 1], true);
                player.destroyItem("Consume", item.getObjectId(), 1, null, false);
            }
        }
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
