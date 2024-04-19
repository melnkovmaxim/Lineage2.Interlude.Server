package quests.q242_PossessorOfAPreciousSoul_2;

import ru.agecold.gameserver.model.L2World;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.QuestState;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;

public class q242_PossessorOfAPreciousSoul_2 extends QuestJython
{
	private static final int VIRGILS_LETTER_1_PART = 7677;
	private static final int BLONDE_STRAND = 7590;
	private static final int SORCERY_INGREDIENT = 7596;
	private static final int CARADINE_LETTER = 7678;
	private static final int ORB_OF_BINDING = 7595;

	private static final int PureWhiteUnicorn = 31747;
	private L2NpcInstance PureWhiteUnicornSpawn = null;
	
	private State PROGRESS = new State("Progress", this);
	private State COMPLETED = new State("Completed", this);

	public q242_PossessorOfAPreciousSoul_2(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		State st = new State("Start", this);
		
		this.setInitialState(st);
		
		this.addStartNpc(31742);

		this.addTalkId(31742);
		this.addTalkId(31743);
		this.addTalkId(31751);
		this.addTalkId(31752);
		this.addTalkId(30759);
		this.addTalkId(30738);
		this.addTalkId(31744);
		this.addTalkId(31748);
		this.addTalkId(PureWhiteUnicorn);
		this.addTalkId(31746);

		this.addKillId(27317);

		//addQuestItem(new int[] { ORB_OF_BINDING, SORCERY_INGREDIENT, BLONDE_STRAND });
	}
	
	public static void main (String... arguments )
	{
		new q242_PossessorOfAPreciousSoul_2(242, "q242_PossessorOfAPreciousSoul_2", "Possessor Of a Precious Soul 2");
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equalsIgnoreCase("31742-2.htm"))
		{
			st.set("cond", "1");
			st.set("CoRObjId", "0");
			st.takeItems(VIRGILS_LETTER_1_PART, 1);
			st.setState(PROGRESS);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("31743-5.htm"))
		{
			st.set("cond", "2");
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("31744-2.htm"))
		{
			st.set("cond", "3");
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("31751-2.htm"))
		{
			st.set("cond", "4");
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("30759-2.htm"))
		{
			st.takeItems(BLONDE_STRAND, 1);
			st.set("cond", "7");
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("30759-4.htm"))
		{
			st.set("cond", "9");
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("30738-2.htm"))
		{
			st.set("cond", "8");
			st.giveItems(SORCERY_INGREDIENT, 1);
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("31748-2.htm"))
		{
			st.takeItems(ORB_OF_BINDING, 1);
			//st.killNpcByObjectId(st.getInt("CoRObjId"));
			st.set("talk", "0");
			if(st.getInt("prog") < 4)
			{
				st.set("prog", String.valueOf(st.getInt("prog") + 1));
				st.playSound(SOUND_MIDDLE);
			}
			if(st.getInt("prog") == 4)
			{
				st.set("cond", "10");
				st.playSound(SOUND_MIDDLE);
			}
			L2NpcInstance vict = (L2NpcInstance) L2World.getInstance().findObject(st.getInt("CoRObjId"));
			if (vict != null)
				vict.doDie(vict);
		}
		return event;
	}

	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance talker)
	{ 
		QuestState st = talker.getQuestState("q242_PossessorOfAPreciousSoul_2");
		if (st == null || st.getState() == COMPLETED)
			return "<html><body>noquest</body></html>";
		
		if(!talker.isSubClassActive())
			return "Subclass only!";

		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if(npcId == 31742)
		{
			if(cond == 0)
			{
				if(st.getQuestItemsCount(VIRGILS_LETTER_1_PART) >= 1 && st.getPlayer().getLevel() >= 60)
					htmltext = "31742-1.htm";
				else
				{
					htmltext = "31742-0.htm";
					st.exitQuest(true);
				}
			}
			else if(cond == 1)
				htmltext = "31742-2r.htm";
		}
		else if(npcId == 31743)
		{
			if(cond == 1)
				htmltext = "31743-1.htm";
			else if(cond == 2)
				htmltext = "31743-2r.htm";
			else if(cond == 11)
			{
				htmltext = "31743-6.htm";
				st.giveItems(CARADINE_LETTER, 1);
				st.addExpAndSp(455764, 0);
				st.unset("cond");
				st.unset("CoRObjId");
				st.unset("prog");
				st.unset("talk");
				st.playSound(SOUND_FINISH);
				st.setState(COMPLETED);
				st.exitQuest(false);
			}
		}
		else if(npcId == 31744)
		{
			if(cond == 2)
				htmltext = "31744-1.htm";
			else if(cond == 3)
				htmltext = "31744-2r.htm";
		}
		else if(npcId == 31751)
		{
			if(cond == 3)
				htmltext = "31751-1.htm";
			else if(cond == 4)
				htmltext = "31751-2r.htm";
			else if(cond == 5 && st.getQuestItemsCount(BLONDE_STRAND) == 1)
			{
				st.set("cond", "6");
				htmltext = "31751-3.htm";
			}
			else if(cond == 6 && st.getQuestItemsCount(BLONDE_STRAND) == 1)
				htmltext = "31751-3r.htm";
		}
		else if(npcId == 31752)
		{
			if(cond == 4)
			{
				st.giveItems(BLONDE_STRAND, 1);
				st.playSound(SOUND_ITEMGET);
				st.set("cond", "5");
				htmltext = "31752-2.htm";
			}
			else
				htmltext = "31752-n.htm";
		}
		else if(npcId == 30759)
		{
			if(cond == 6 && st.getQuestItemsCount(BLONDE_STRAND) == 1)
				htmltext = "30759-1.htm";
			else if(cond == 7)
				htmltext = "30759-2r.htm";
			else if(cond == 8 && st.getQuestItemsCount(SORCERY_INGREDIENT) == 1)
				htmltext = "30759-3.htm";
		}
		else if(npcId == 30738)
		{
			if(cond == 7)
				htmltext = "30738-1.htm";
			else if(cond == 8)
				htmltext = "30738-2r.htm";
		}
		else if(npcId == 31748)
		{
			if(cond == 9)
				if(st.getQuestItemsCount(ORB_OF_BINDING) >= 1)
				{
					if(npc.getObjectId() != st.getInt("CoRObjId"))
					{
						st.set("CoRObjId", String.valueOf(npc.getObjectId()));
						st.set("talk", "1");
						htmltext = "31748-1.htm";
					}
					else if(st.getInt("talk") == 1)
						htmltext = "31748-1.htm";
					else
						htmltext = "noquest";
				}
				else
					htmltext = "31748-0.htm";
		}
		else if(npcId == 31746)
		{
			if(cond == 9)
				htmltext = "31746-1.htm";
			else if(cond == 10)
			{
				htmltext = "31746-1.htm";
				npc.doDie(npc);
				if(PureWhiteUnicornSpawn == null || /*!st.getPlayer().knowsObject(PureWhiteUnicornSpawn) ||*/ !PureWhiteUnicornSpawn.isVisible())
					PureWhiteUnicornSpawn = st.addSpawn(PureWhiteUnicorn, npc.getX() + 10, npc.getY(), npc.getZ(), 120000);
			}
			else
				htmltext = "noquest";
		}
		else if(npcId == PureWhiteUnicorn)
			if(cond == 10)
			{
				htmltext = "31747-1.htm";
				st.set("cond", "11");
			}
			else if(cond == 11)
				htmltext = "31747-2.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{
		QuestState st = killer.getQuestState("q242_PossessorOfAPreciousSoul_2");
		if (st == null)
			return null;
		
		if(!st.getPlayer().isSubClassActive())
			return null;
		int cond = st.getInt("cond");

		if(cond == 9 && st.getQuestItemsCount(ORB_OF_BINDING) < 4)
			st.giveItems(ORB_OF_BINDING, 1);
		if(st.getQuestItemsCount(ORB_OF_BINDING) < 4)
			st.playSound(SOUND_ITEMGET);
		else
			st.playSound(SOUND_MIDDLE);
		return null;
	}
}