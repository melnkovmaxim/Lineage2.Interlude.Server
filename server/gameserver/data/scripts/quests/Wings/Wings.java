package quests.Wings;

import ru.agecold.gameserver.model.L2Party;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.QuestState;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.util.Rnd;
@SuppressWarnings("all")
public class Wings extends QuestJython
{
	//NPC
	private final static int npcId = 41113;
	private final static int[] MOBS = {15079};
    private final static int RaidBoss = 15121;
	//QuestItem
	private final static int BLACK_FEATHER = 12010;
    private final static int ITEM_FROM_RB = 12011;
    //Chance from mobs
    private final static int chance1 = 75;
    //Chance from RB
    private final static int chance2 = 100;
	//Item
	private final static int FEATHER = 12014;
    private int count;
    //Разрешено ли повторять квест?
    private static boolean REPEATABLE = true;

    private int cond = 0;

	private State STARTED = new State("Started", this);

	public Wings(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		
		State st = new State("Start", this);
		this.setInitialState(st);
		
		addStartNpc(npcId);
		addTalkId(npcId);
        for (int mobs : MOBS)
            addKillId(mobs);
        addKillId(RaidBoss);
	}

	public String onEvent(String event, QuestState st)
	{
        L2PcInstance player = st.getPlayer();
        if (event.equalsIgnoreCase("start"))
        {
            st.set("cond","1");
            event = "77777-2.htm";
            st.setState(STARTED);
        }
        else if (event.equalsIgnoreCase("sobral"))
        {
            count = player.getInventory().getInventoryItemCount(BLACK_FEATHER,0);
            if (count < 300)
                event = "77777-no.htm";
            else
            {
                st.takeItems(BLACK_FEATHER,300);
                st.set("cond","2");
                event = "77777-4.htm";
            }
        }
        else if (event.equalsIgnoreCase("item_RB"))
        {
            count = player.getInventory().getInventoryItemCount(ITEM_FROM_RB,0);
            if (count < 1)
                event = "77777-no1.htm";
            else
            {
                st.takeItems(ITEM_FROM_RB,1);
                st.giveItems(FEATHER,1);
                st.exitQuest(REPEATABLE);
                event = "<html><body>kvest zavershen</body><html>";
            }
        }
        else if (event.equalsIgnoreCase("otmena"))
        {
            event = "<html><body>kvest otmenen</body></html>";
            st.exitQuest(true);
        }
		return event;
	}

	public String onTalk(L2NpcInstance npc, L2PcInstance talker)
	{
		QuestState st = talker.getQuestState(getName());
		String htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		if(st == null) return htmltext;
		int npcId = npc.getNpcId();
        cond = st.getInt("cond");
		if(npcId == npcId)
		{
            if (cond == 0)
                htmltext = "77777-1.htm";
            else if (cond == 1)
                htmltext = "77777-3.htm";
            else if (cond == 2)
                htmltext = "77777-5.htm";
		}
		return htmltext;
	}

    public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet)
    {
        QuestState st = killer.getQuestState(getName());
        if(st == null) return null;
        int npcId = npc.getNpcId();
        int cond = st.getInt("cond");
        L2Party party = killer.getParty();
		switch(cond)
		{
			case 1:
				for (int id : MOBS)
					if (npcId == id)
						if (Rnd.get(100) < chance1)
							if (st.getPlayer().getInventory().getInventoryItemCount(BLACK_FEATHER,0) < 300)
								st.giveItems(BLACK_FEATHER,1);
				break;
			case 2:
				if (npcId == RaidBoss)
					if (Rnd.get(100) < chance2)
						if (party != null)
							for (L2PcInstance member : party.getPartyMembers())
								if (member.getInventory().getInventoryItemCount(ITEM_FROM_RB,0) == 0)
									member.getQuestState(getName()).giveItems(ITEM_FROM_RB,1);
						else
							if (killer.getInventory().getInventoryItemCount(ITEM_FROM_RB,0) == 0)
								st.giveItems(ITEM_FROM_RB,1);
				break;
		}
        return null;
    }

	public static void main(String[] args)
	{
		new Wings(121, "Wings", "quests");
	}
}
