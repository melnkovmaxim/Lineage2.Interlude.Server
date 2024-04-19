package quests.q422_RepentYourSins;

import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.L2Summon;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
//import ru.agecold.gameserver.model.quest.Quest;
import ru.agecold.gameserver.model.quest.QuestState;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.util.Rnd;

public class q422_RepentYourSins extends QuestJython
{
	// Items
	private final static short SCAVENGER_WERERAT_SKULL = 4326;
	private final static short TUREK_WARHOUND_TAIL = 4327;
	private final static short TYRANT_KINGPIN_HEART = 4328;
	private final static short TRISALIM_TARANTULAS_VENOM_SAC = 4329;
	private final static short MANUAL_OF_MANACLES = 4331;
	private final static short PENITENTS_MANACLES = 4425; // для призыв
	private final static short PENITENTS_MANACLES1 = 4330; // заготовка
	private final static short PENITENTS_MANACLES2 = 4426; // остается после завершения квеста
	private final static short SILVER_NUGGET = 1873;
	private final static short ADAMANTINE_NUGGET = 1877;
	private final static short BLACKSMITHS_FRAME = 1892;
	private final static short COKES = 1879;
	private final static short STEEL = 1880;

	// NPCs
	private final static int Black_Judge = 30981;
	private final static int Katari = 30668;
	private final static int Piotur = 30597;
	private final static int Casian = 30612;
	private final static int Joan = 30718;
	private final static int Pushkin = 30300;

	private final static int Sin_Eater = 12564;

	// Mobs
	private final static int SCAVENGER_WERERAT = 20039;
	private final static int TUREK_WARHOUND = 20494;
	private final static int TYRANT_KINGPIN = 20193;
	private final static int TRISALIM_TARANTULA = 20561;

	public int findPetLvl(QuestState st)
	{
		L2ItemInstance item = st.getPlayer().getInventory().getItemByItemId(PENITENTS_MANACLES);
		if(item == null)
			return 0;
		L2Summon pet = st.getPlayer().getPet();
		if(pet == null)
			return item.getEnchantLevel();
		if(pet.getNpcId() != Sin_Eater)
			return 0;
		return pet.getLevel();
	}

	private State STARTED = new State("Started", this);
	
	public q422_RepentYourSins(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		State st = new State("Start", this);
		
		this.setInitialState(st);

		this.addStartNpc(Black_Judge);
		this.addTalkId(Black_Judge);
		this.addTalkId(Katari);
		this.addTalkId(Piotur);
		this.addTalkId(Casian);
		this.addTalkId(Joan);
		this.addTalkId(Pushkin);

		this.addKillId(SCAVENGER_WERERAT);
		this.addKillId(TUREK_WARHOUND);
		this.addKillId(TYRANT_KINGPIN);
		this.addKillId(TRISALIM_TARANTULA);

		/*addQuestItem(SCAVENGER_WERERAT_SKULL);
		addQuestItem(TUREK_WARHOUND_TAIL);
		addQuestItem(TYRANT_KINGPIN_HEART);
		addQuestItem(TRISALIM_TARANTULAS_VENOM_SAC);
		addQuestItem(MANUAL_OF_MANACLES);
		addQuestItem(PENITENTS_MANACLES);
		addQuestItem(PENITENTS_MANACLES1);*/
	}
	
	public static void main (String... arguments )
	{
		new q422_RepentYourSins(422, "q422_RepentYourSins", "Repent Your Sins");
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		int Pk_remove;
		if(event.equalsIgnoreCase("Start"))
		{
			st.playSound(SOUND_ACCEPT);
			st.setState(STARTED);
			if(st.getPlayer().getLevel() <= 20)
			{
				st.set("cond", "1");
				st.set("cond", "2");
				return "black_judge_q0422_03.htm";
			}
			if(st.getPlayer().getLevel() <= 30)
			{
				st.set("cond", "3");
				return "black_judge_q0422_04.htm";
			}
			if(st.getPlayer().getLevel() <= 40)
			{
				st.set("cond", "4");
				return "black_judge_q0422_05.htm";
			}
			st.set("cond", "5");
			return "black_judge_q0422_06.htm";
		}

		if(event.equalsIgnoreCase("1"))
		{
			if(st.getQuestItemsCount(PENITENTS_MANACLES1) >= 1)
				st.takeItems(PENITENTS_MANACLES1, -1);
			if(st.getQuestItemsCount(PENITENTS_MANACLES) >= 1)
				st.takeItems(PENITENTS_MANACLES, -1);
			st.set("cond", "16");
			st.set("level", String.valueOf(st.getPlayer().getLevel()));
			st.giveItems(PENITENTS_MANACLES, 1);
			return "black_judge_q0422_11.htm";
		}
		if(event.equalsIgnoreCase("2"))
			return "black_judge_q0422_14.htm";
		if(event.equalsIgnoreCase("3"))
		{
			int plevel = findPetLvl(st);
			int level = st.getPlayer().getLevel();
			int olevel = st.getInt("level");
			L2Summon pet = st.getPlayer().getPet();
			if(pet != null)
			{
				if(pet.getNpcId() == Sin_Eater)
					return "black_judge_q0422_15t.htm";
			}
			else
			{
				if(level > olevel)
					Pk_remove = plevel - level;
				else
					Pk_remove = plevel - olevel;
				if(Pk_remove < 0)
					Pk_remove = 0;
				Pk_remove = Rnd.get(10 + Pk_remove) + 1;
				if(st.getPlayer().getPkKills() <= Pk_remove)
				{
					st.getPlayer().setPkKills(0);
					st.playSound(SOUND_FINISH);
					if(st.getQuestItemsCount(PENITENTS_MANACLES2) < 1)
						st.giveItems(PENITENTS_MANACLES2, 1);
					st.exitQuest(true);
					return "black_judge_q0422_15.htm";
				}
				st.takeItems(PENITENTS_MANACLES, 1);
				int Pk_new = st.getPlayer().getPkKills() - Pk_remove;
				st.getPlayer().setPkKills(Pk_new);
				st.set("level", "0");
				return "black_judge_q0422_16.htm";
			}
		}
		if(event.equalsIgnoreCase("4"))
			return "black_judge_q0422_17.htm";
		if(event.equalsIgnoreCase("Quit"))
		{
			st.playSound(SOUND_FINISH);
			st.exitQuest(true);
			return "black_judge_q0422_18.htm";
		}

		return event;
	}

	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance talker)
	{ 
		QuestState st = talker.getQuestState("q422_RepentYourSins");
		if (st == null)
			return null;
		int cond = st.getInt("cond");
		int npcId = npc.getNpcId();

		if(npcId == Black_Judge)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getPkKills() >= 1 && st.getPlayer().getLevel() <= 85)
					return "black_judge_q0422_02.htm";
				st.exitQuest(true);
				return "black_judge_q0422_01.htm";
			}
			if(cond <= 9)
				return "black_judge_q0422_07.htm";
			if(cond <= 13 && cond > 9 && st.getQuestItemsCount(MANUAL_OF_MANACLES) < 1)
			{
				if(st.getQuestItemsCount(PENITENTS_MANACLES2) < 1)
				{
					st.set("cond", "14");
					st.giveItems(MANUAL_OF_MANACLES, 1);
					return "black_judge_q0422_08.htm";
				}
				st.takeItems(PENITENTS_MANACLES2, -1);
				if(st.getQuestItemsCount(PENITENTS_MANACLES) < 1)
					st.giveItems(PENITENTS_MANACLES, 1);
				st.set("cond", "16");
				cond = 16;
			}
			if(cond == 14 && st.getQuestItemsCount(MANUAL_OF_MANACLES) > 0)
				return "black_judge_q0422_09.htm";
			if(cond == 15 && st.getQuestItemsCount(PENITENTS_MANACLES1) > 0)
				return "black_judge_q0422_10.htm";
			if(cond >= 16)
			{
				if(st.getQuestItemsCount(PENITENTS_MANACLES) > 0)
				{
					int plevel = findPetLvl(st);
					int level = st.getPlayer().getLevel();
					if(st.getInt("level") > level)
						level = st.getInt("level");
					if(plevel > 0)
					{
						if(plevel > level)
							return "black_judge_q0422_13.htm";
						return "black_judge_q0422_12.htm";
					}
					return "black_judge_q0422_12.htm";
				}
				return "black_judge_q0422_16t.htm";
			}
		}

		if(npcId == Katari)
		{
			if(cond == 2)
			{
				st.set("cond", "6");
				return "katari_q0422_01.htm";
			}
			if(cond == 6 && st.getQuestItemsCount(SCAVENGER_WERERAT_SKULL) < 10)
				return "katari_q0422_02.htm";
			if(cond == 10)
				return "katari_q0422_04.htm";
			st.set("cond", "10");
			st.takeItems(SCAVENGER_WERERAT_SKULL, -1);
			return "katari_q0422_03.htm";
		}

		if(npcId == Piotur)
		{
			if(cond == 3)
			{
				st.set("cond", "7");
				return "piotur_q0422_01.htm";
			}
			if(cond == 7 && st.getQuestItemsCount(TUREK_WARHOUND_TAIL) < 10)
				return "piotur_q0422_02.htm";
			if(cond == 11)
				return "piotur_q0422_04.htm";
			st.set("cond", "11");
			st.takeItems(TUREK_WARHOUND_TAIL, -1);
			return "piotur_q0422_03.htm";
		}

		if(npcId == Casian)
		{
			if(cond == 4)
			{
				st.set("cond", "8");
				return "sage_kasian_q0422_01.htm";
			}
			if(cond == 8)
			{
				if(st.getQuestItemsCount(TYRANT_KINGPIN_HEART) < 1)
					return "sage_kasian_q0422_02.htm";
				st.set("cond", "12");
				st.takeItems(TYRANT_KINGPIN_HEART, -1);
				return "sage_kasian_q0422_03.htm";
			}
			if(cond == 12)
				return "sage_kasian_q0422_04.htm";
		}

		if(npcId == Joan)
		{
			if(cond == 5)
			{
				st.set("cond", "9");
				return "magister_joan_q0422_01.htm";
			}
			if(cond == 9 && st.getQuestItemsCount(TRISALIM_TARANTULAS_VENOM_SAC) < 3)
				return "magister_joan_q0422_02.htm";
			if(st.getQuestItemsCount(TRISALIM_TARANTULAS_VENOM_SAC) >= 3)
			{
				st.set("cond", "13");
				st.takeItems(TRISALIM_TARANTULAS_VENOM_SAC, -1);
				return "magister_joan_q0422_03.htm";
			}
			if(cond == 13)
				return "magister_joan_q0422_04.htm";
		}

		if(npcId == Pushkin)
			if(cond >= 14)
			{
				if(st.getQuestItemsCount(MANUAL_OF_MANACLES) == 1)
				{
					if(st.getQuestItemsCount(SILVER_NUGGET) < 10 || st.getQuestItemsCount(STEEL) < 5 || st.getQuestItemsCount(ADAMANTINE_NUGGET) < 2 || st.getQuestItemsCount(COKES) < 10 || st.getQuestItemsCount(BLACKSMITHS_FRAME) < 1)
						return "blacksmith_pushkin_q0422_02.htm";
					if(st.getQuestItemsCount(SILVER_NUGGET) >= 10 && st.getQuestItemsCount(STEEL) >= 5 && st.getQuestItemsCount(ADAMANTINE_NUGGET) >= 2 && st.getQuestItemsCount(COKES) >= 10 && st.getQuestItemsCount(BLACKSMITHS_FRAME) >= 1)
					{
						st.set("cond", "15");
						st.takeItems(MANUAL_OF_MANACLES, 1);
						st.takeItems(SILVER_NUGGET, 10);
						st.takeItems(ADAMANTINE_NUGGET, 2);
						st.takeItems(COKES, 10);
						st.takeItems(STEEL, 5);
						st.takeItems(BLACKSMITHS_FRAME, 1);
						st.giveItems(PENITENTS_MANACLES1, 1);
						st.playSound(SOUND_MIDDLE);
						return "blacksmith_pushkin_q0422_02.htm";
					}
				}
				if(st.getQuestItemsCount(PENITENTS_MANACLES1) > 0 || st.getQuestItemsCount(PENITENTS_MANACLES) > 0 || st.getQuestItemsCount(PENITENTS_MANACLES2) > 0)
					return "blacksmith_pushkin_q0422_03.htm";
			}
		return "noquest";
	}

	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{
		QuestState st = killer.getQuestState("q422_RepentYourSins");
		if (st == null)
			return null;
		int cond = st.getInt("cond");
		if(cond == 0)
			return null;
		int npcId = npc.getNpcId();
		long skulls = st.getQuestItemsCount(SCAVENGER_WERERAT_SKULL);
		long tails = st.getQuestItemsCount(TUREK_WARHOUND_TAIL);
		long heart = st.getQuestItemsCount(TYRANT_KINGPIN_HEART);
		long sacs = st.getQuestItemsCount(TRISALIM_TARANTULAS_VENOM_SAC);
		if(npcId == SCAVENGER_WERERAT)
			if(cond == 6 && skulls < 10)
			{
				st.giveItems(SCAVENGER_WERERAT_SKULL, 1);
				if(skulls == 10)
					st.playSound(SOUND_MIDDLE);
				else
					st.playSound(SOUND_ITEMGET);
			}
		if(npcId == TUREK_WARHOUND)
			if(cond == 7 && tails < 10)
			{
				st.giveItems(TUREK_WARHOUND_TAIL, 1);
				if(tails == 10)
					st.playSound(SOUND_MIDDLE);
				else
					st.playSound(SOUND_ITEMGET);
			}
		if(npcId == TYRANT_KINGPIN)
			if(cond == 8 && heart < 1)
			{
				st.giveItems(TYRANT_KINGPIN_HEART, 1);
				st.playSound(SOUND_MIDDLE);
			}
		if(npcId == TRISALIM_TARANTULA)
			if(cond == 9 && sacs < 3)
			{
				st.giveItems(TRISALIM_TARANTULAS_VENOM_SAC, 1);
				if(skulls == 3)
					st.playSound(SOUND_MIDDLE);
				else
					st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}