package quests.q348_ArrogantSearch;

import javolution.util.FastMap;

import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
//import ru.agecold.gameserver.model.quest.Quest;
import ru.agecold.gameserver.model.quest.QuestState;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.gameserver.network.serverpackets.RadarControl;
import ru.agecold.util.Rnd;

public class q348_ArrogantSearch extends QuestJython
{
	private final static int ARK_GUARDIAN_ELBEROTH = 27182;
	private final static int ARK_GUARDIAN_SHADOWFANG = 27183;
	private final static int ANGEL_KILLER = 27184;
	private final static int PLATINUM_TRIBE_SHAMAN = 20828;
	private final static int PLATINUM_TRIBE_OVERLORD = 20829;
	private final static int LESSER_GIANT_MAGE = 20657;
	private final static int LESSER_GIANT_ELDER = 20658;
	private final static int GUARDIAN_ANGEL = 20859;
	private final static int SEAL_ANGEL = 20860;

	private final static int HANELLIN = 30864;
	private final static int HOLY_ARK_OF_SECRECY_1 = 30977;
	private final static int HOLY_ARK_OF_SECRECY_2 = 30978;
	private final static int HOLY_ARK_OF_SECRECY_3 = 30979;
	private final static int ARK_GUARDIANS_CORPSE = 30980;
	private final static int HARNE = 30144;
	private final static int CLAUDIA_ATHEBALT = 31001;
	private final static int MARTIEN = 30645;

	private final static int TITANS_POWERSTONE = 4287;
	private final static int HANELLINS_FIRST_LETTER = 4288;
	private final static int HANELLINS_SECOND_LETTER = 4289;
	private final static int HANELLINS_THIRD_LETTER = 4290;
	private final static int FIRST_KEY_OF_ARK = 4291;
	private final static int SECOND_KEY_OF_ARK = 4292;
	private final static int THIRD_KEY_OF_ARK = 4293;
	private final static int WHITE_FABRIC_1 = 4294;//to use on Platinum Tribe Shamans/Overlords
	private final static int BLOODED_FABRIC = 4295;
	//private final static int HANELLINS_WHITE_FLOWER = 4394;
	//private final static int HANELLINS_RED_FLOWER = 4395;
	//private final static int HANELLINS_YELLOW_FLOWER = 4396;
	private final static int BOOK_OF_SAINT = 4397;//Ark2 (after fight with Elberoth)
	private final static int BLOOD_OF_SAINT = 4398;//Ark1 (after fight with Angel Killer)
	private final static int BRANCH_OF_SAINT = 4399;//Ark3 (after fight with Shadowfang)
	private final static int WHITE_FABRIC_0 = 4400;//talk to Hanellin to see what to do (for companions)
	private final static int WHITE_FABRIC_2 = 5232;//to use on Guardian Angels and Seal Angels
	private final static int ANTIDOTE = 1831;
	private final static int HEALING_POTION = 1061;

	//ARK: [key, summon, no-key text, openning-with-key text, already-openned text, content item]
	private final static FastMap<Integer, Integer[]> ARKS = new FastMap<Integer, Integer[]>().shared("q348_ArrogantSearch.ARKS");
	private final static FastMap<Integer, String[]> ARKS_TEXT = new FastMap<Integer, String[]>().shared("q348_ArrogantSearch.ARKS_TEXT");
	//npc: letter to take, item to check for, 1st time htm, return htm, completed part htm, [x,y,z of chest]
	private final static FastMap<Integer, Integer[]> ARK_OWNERS = new FastMap<Integer, Integer[]>().shared("q348_ArrogantSearch.ARK_OWNERS");
	private final static FastMap<Integer, String[]> ARK_OWNERS_TEXT = new FastMap<Integer, String[]>().shared("q348_ArrogantSearch.ARK_OWNERS_TEXT");
	//mob: cond, giveItem, amount, chance%, takeItem (assumed to take only 1 of it)
	private final static FastMap<Integer, Integer[]> DROPS = new FastMap<Integer, Integer[]>().shared("q348_ArrogantSearch.DROPS");

	static
	{
		ARKS.put(HOLY_ARK_OF_SECRECY_1, new Integer[] { FIRST_KEY_OF_ARK, 0, BLOOD_OF_SAINT });
		ARKS.put(HOLY_ARK_OF_SECRECY_2, new Integer[] { SECOND_KEY_OF_ARK, ARK_GUARDIAN_ELBEROTH, BOOK_OF_SAINT });
		ARKS.put(HOLY_ARK_OF_SECRECY_3, new Integer[] { THIRD_KEY_OF_ARK, ARK_GUARDIAN_SHADOWFANG, BRANCH_OF_SAINT });
		ARKS_TEXT.put(HOLY_ARK_OF_SECRECY_1, new String[] { "30977-01.htm", "30977-02.htm", "30977-03.htm" });
		ARKS_TEXT.put(HOLY_ARK_OF_SECRECY_2, new String[] { "That doesn't belong to you.  Don't touch it!", "30978-02.htm",
				"30978-03.htm" });
		ARKS_TEXT.put(HOLY_ARK_OF_SECRECY_3, new String[] { "Get off my sight, you infidels!", "30979-02.htm",
				"30979-03.htm" });

		ARK_OWNERS.put(HARNE, new Integer[] { HANELLINS_FIRST_LETTER, BLOOD_OF_SAINT, -418, 44174, -3568 });
		ARK_OWNERS.put(CLAUDIA_ATHEBALT, new Integer[] { HANELLINS_SECOND_LETTER, BOOK_OF_SAINT, 181472, 7158, -2725 });
		ARK_OWNERS.put(MARTIEN, new Integer[] { HANELLINS_THIRD_LETTER, BRANCH_OF_SAINT, 50693, 158674, 376 });
		ARK_OWNERS_TEXT.put(HARNE, new String[] { "30144-01.htm", "30144-02.htm", "30144-03.htm" });
		ARK_OWNERS_TEXT.put(CLAUDIA_ATHEBALT, new String[] { "31001-01.htm", "31001-02.htm", "31001-03.htm" });
		ARK_OWNERS_TEXT.put(MARTIEN, new String[] { "30645-01.htm", "30645-02.htm", "30645-03.htm" });

		// NPC, { min cond, item to give, max count, chance, item to take }
		DROPS.put(LESSER_GIANT_MAGE, new Integer[] { 2, TITANS_POWERSTONE, 1, 10, 0 });
		DROPS.put(LESSER_GIANT_ELDER, new Integer[] { 2, TITANS_POWERSTONE, 1, 10, 0 });
		DROPS.put(ANGEL_KILLER, new Integer[] { 5, FIRST_KEY_OF_ARK, 1, 100, 0 });
		DROPS.put(ARK_GUARDIAN_ELBEROTH, new Integer[] { 5, SECOND_KEY_OF_ARK, 1, 100, 0 });
		DROPS.put(ARK_GUARDIAN_SHADOWFANG, new Integer[] { 5, THIRD_KEY_OF_ARK, 1, 100, 0 });
		DROPS.put(PLATINUM_TRIBE_SHAMAN, new Integer[] { 25, BLOODED_FABRIC, Integer.MAX_VALUE, 10, WHITE_FABRIC_1 });
		DROPS.put(PLATINUM_TRIBE_OVERLORD, new Integer[] { 25, BLOODED_FABRIC, Integer.MAX_VALUE, 10, WHITE_FABRIC_1 });
		DROPS.put(GUARDIAN_ANGEL, new Integer[] { 28, BLOODED_FABRIC, Integer.MAX_VALUE, 25, WHITE_FABRIC_2 });
		DROPS.put(SEAL_ANGEL, new Integer[] { 28, BLOODED_FABRIC, Integer.MAX_VALUE, 25, WHITE_FABRIC_2 });
	}
	
	//private State CREATED = new State("Start", this);
	private State PROGRESS = new State("Progress", this);
	//private State COMPLETED = null;
	
	public q348_ArrogantSearch(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		State st = new State("Start", this);

		//CREATED = new State("Start", this);
		//PROGRESS = new State("Progress", this);
		//COMPLETED = new State("Completed", this);

		this.setInitialState(st);
		this.addStartNpc(HANELLIN);
		this.addTalkId(HANELLIN);

		this.addTalkId(ARK_GUARDIANS_CORPSE);

		for(int i : ARK_OWNERS.keySet())
			this.addTalkId(i);

		for(int i : ARKS.keySet())
			this.addTalkId(i);
		
		this.addAttackId(PLATINUM_TRIBE_SHAMAN);
		this.addAttackId(PLATINUM_TRIBE_OVERLORD);

		for(int i : DROPS.keySet())
			this.addKillId(i);

		/*addQuestItem(new int[] { HANELLINS_FIRST_LETTER, HANELLINS_SECOND_LETTER, HANELLINS_THIRD_LETTER,
				HANELLINS_WHITE_FLOWER, HANELLINS_RED_FLOWER, HANELLINS_YELLOW_FLOWER, BOOK_OF_SAINT, WHITE_FABRIC_1,
				BLOOD_OF_SAINT, BRANCH_OF_SAINT, WHITE_FABRIC_0, WHITE_FABRIC_2, FIRST_KEY_OF_ARK, SECOND_KEY_OF_ARK,
				THIRD_KEY_OF_ARK });*/
	}
	
	public static void main (String... arguments )
	{
		new q348_ArrogantSearch(348, "q348_ArrogantSearch", "An Arrogant Search");
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		if(event.equals("30864-03.htm"))
		{
			st.set("cond", "2");
			htmltext = "30864-03.htm";
		}
		else if(event.equals("30864-04a.htm"))//work alone
		{
			st.set("cond", "4");
			st.takeItems(TITANS_POWERSTONE, -1);
			htmltext = "30864-04c.htm";
			st.set("companions", "0");
		}
		else if(event.equals("30864_04b.htm"))//work with friends
		{
			st.set("cond", "3");
			st.set("companions", "1");
			st.takeItems(TITANS_POWERSTONE, -1);
			htmltext = "not yet implemented";
			//todo: give flowers & handle the multiperson quest...
		}
		else if(event.equals("30864-09a.htm"))
		{
			st.set("cond", "29");
			st.giveItems(WHITE_FABRIC_2, 10);
		}
		else if(event.equals("30864-10a.htm"))
		{
			if(st.getQuestItemsCount(WHITE_FABRIC_2) < 10)
				st.giveItems(WHITE_FABRIC_2, 10 - st.getQuestItemsCount(WHITE_FABRIC_2));
			htmltext = "30864-10.htm";
		}
		else if(event.equals("30864-10b.htm"))
		{
			int count = st.getQuestItemsCount(BLOODED_FABRIC);
			st.takeItems(BLOODED_FABRIC, -1);
			st.giveItems(57, count * 5000); // с потолка
			htmltext = "30864-10.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance talker)
	{ 
		QuestState st = talker.getQuestState("q348_ArrogantSearch");
		if (st == null)
			return null;
		int npcId = npc.getNpcId();
		String htmltext = "noquest";
		//State id = st.getState();
		int cond = st.getInt("cond");
		if(npcId == HANELLIN)
		{
			if(cond == 0)
			// if the quest was completed and the player still has a blooded fabric
			// tell them the "secret" that they can use it in order to visit Baium.
			{
				if(st.getQuestItemsCount(BLOODED_FABRIC) >= 1)
				{
					htmltext = "30864-Baium.htm";
					st.exitQuest(true);
				}
				else
				//else, start the quest normally
				{
					st.set("cond", "0");
					if(st.getPlayer().getLevel() < 60)
					{
						htmltext = "30864-01.htm";//not qualified
						st.exitQuest(true);
					}
					else if(cond == 0)
					{
						st.setState(PROGRESS);
						st.set("cond", "1");
						htmltext = "30864-02.htm";// Successful start: begin the dialog which will set cond=2
					}
				}
			}
			// Player abandoned in the middle of last dialog...repeat the dialog.
			else if(cond == 1)
				htmltext = "30864-02.htm";// begin the dialog which will set cond=2
			// Has returned before getting the powerstone
			else if(cond == 2 && st.getQuestItemsCount(TITANS_POWERSTONE) == 0)
				htmltext = "30864-03a.htm";// go get the titan's powerstone
			else if(cond == 2)
				htmltext = "30864-04.htm";// Ask "work alone or in group?"...only alone is implemented in v0.1
			else if(cond == 4)
			{
				st.set("cond", "5");
				st.giveItems(HANELLINS_FIRST_LETTER, 1);
				st.giveItems(HANELLINS_SECOND_LETTER, 1);
				st.giveItems(HANELLINS_THIRD_LETTER, 1);
				htmltext = "30864-05.htm";// Go get the 3 sacred relics
			}
			else if(cond == 5 && st.getQuestItemsCount(BOOK_OF_SAINT) + st.getQuestItemsCount(BLOOD_OF_SAINT) + st.getQuestItemsCount(BRANCH_OF_SAINT) < 3)
				htmltext = "30864-05.htm";// Repeat: Go get the 3 sacred relics
			else if(cond == 5)
			{
				htmltext = "30864-06.htm";// All relics collected!...Get me antidotes & greater healing
				st.takeItems(BOOK_OF_SAINT, -1);
				st.takeItems(BLOOD_OF_SAINT, -1);
				st.takeItems(BRANCH_OF_SAINT, -1);
				st.set("cond", "22");
			}
			else if(cond == 22 && st.getQuestItemsCount(ANTIDOTE) < 5 && st.getQuestItemsCount(HEALING_POTION) < 1)
				htmltext = "30864-06a.htm";// where are my antidotes & greater healing
			else if(cond == 22)
			{
				st.takeItems(ANTIDOTE, 5);
				st.takeItems(HEALING_POTION, 1);
				if(st.getInt("companions") == 0)
				{
					st.set("cond", "25");
					htmltext = "30864-07.htm";// go get platinum tribe blood...
					st.giveItems(WHITE_FABRIC_1, 1);
				}
				else
				{
					st.set("cond", "23");
					htmltext = "not implemented yet";
					st.giveItems(WHITE_FABRIC_0, 3);
				}
			}
			else if(cond == 25 && st.getQuestItemsCount(BLOODED_FABRIC) < 1)
			{
				if(st.getQuestItemsCount(WHITE_FABRIC_1) < 1)
					st.giveItems(WHITE_FABRIC_1, 1);
				htmltext = "30864-07a.htm";
			}
			else if(cond == 26 && st.getQuestItemsCount(BLOODED_FABRIC) < 1)
			{
				if(st.getQuestItemsCount(WHITE_FABRIC_2) < 1)
					st.giveItems(WHITE_FABRIC_2, 1);
				htmltext = "30864-07a.htm";
			}
			else if(cond == 25 && st.getQuestItemsCount(BLOODED_FABRIC) > 0 || cond == 28)
			{
				// затычка, инфу искать надо, пропущен 1 конд с доставкой тряпок
				if(cond != 28)
					st.set("cond", "28");
				htmltext = "30864-09.htm";
			}
			else if(cond == 29)
				htmltext = "30864-10.htm";
		}
		// Other NPCs follow:
		else if(cond == 5)
			if(ARK_OWNERS.containsKey(npcId))
			{
				// first meeting...have the letter
				if(st.getQuestItemsCount(ARK_OWNERS.get(npcId)[0]) == 1)
				{
					st.takeItems(ARK_OWNERS.get(npcId)[0], 1);
					htmltext = ARK_OWNERS_TEXT.get(npcId)[0];
					st.getPlayer().sendPacket(new RadarControl(0, 1, ARK_OWNERS.get(npcId)[2], ARK_OWNERS.get(npcId)[3], ARK_OWNERS.get(npcId)[4]));
				}
				// do not have letter and do not have the item
				else if(st.getQuestItemsCount(ARK_OWNERS.get(npcId)[1]) < 1)
				{
					htmltext = ARK_OWNERS_TEXT.get(npcId)[1];
					st.getPlayer().sendPacket(new RadarControl(0, 1, ARK_OWNERS.get(npcId)[2], ARK_OWNERS.get(npcId)[3], ARK_OWNERS.get(npcId)[4]));
				}
				else
					//have the item (done)
					htmltext = ARK_OWNERS_TEXT.get(npcId)[2];
			}
			else if(ARKS.containsKey(npcId))
			{
				// if you do not have the key (first meeting)
				if(st.getQuestItemsCount(ARKS.get(npcId)[0]) == 0)
				{
					if(ARKS.get(npcId)[1] != 0)
						st.addSpawn(ARKS.get(npcId)[1], 120000);
					return ARKS_TEXT.get(npcId)[0];
				}
				// if the player already has openned the chest and has its content, show "chest empty"
				else if(st.getQuestItemsCount(ARKS.get(npcId)[2]) == 1)
					htmltext = ARKS_TEXT.get(npcId)[2];
				else
				// the player has the key and doesn't have the contents, give the contents
				{
					htmltext = ARKS_TEXT.get(npcId)[1];
					st.takeItems(ARKS.get(npcId)[0], 1);
					st.giveItems(ARKS.get(npcId)[2], 1);
				}
			}
			else if(npcId == ARK_GUARDIANS_CORPSE)
				// if you do not have the key (first meeting)
				if(st.getQuestItemsCount(FIRST_KEY_OF_ARK) == 0 && st.getInt("angelKillerIsDefeated") == 0)
				{
					st.addSpawn(ANGEL_KILLER, 120000);
					htmltext = "30980-01.htm";
				}
				else if(st.getQuestItemsCount(FIRST_KEY_OF_ARK) == 0 && st.getInt("angelKillerIsDefeated") == 1)
				{
					st.giveItems(FIRST_KEY_OF_ARK, 1);
					htmltext = "30980-02.htm";
				}
				else
					htmltext = "30980-03.htm";
		return htmltext;
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet) 
	{ 
		QuestState st = attacker.getQuestState("q348_ArrogantSearch");
		if (st == null)
			return null;
		
		int npcId = npc.getNpcId();
		Integer[] drop = DROPS.get(npcId);
		if(drop != null)
		{
			int cond = drop[0];
			int item = drop[1];
			int max = drop[2];
			int chance = drop[3];
			int take = drop[4];
			if(st.getInt("cond") >= cond && st.getQuestItemsCount(item) < max && (take == 0 || st.getQuestItemsCount(take) > 0) && Rnd.chance(chance))
			{
				st.giveItems(item, 1);
				st.playSound(SOUND_ITEMGET);
				if(take != 0)
					st.takeItems(take, 1);
			}
		}
		return null; 
	} 

	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{
		QuestState st = killer.getQuestState("q348_ArrogantSearch");
		if (st == null)
			return null;
		int npcId = npc.getNpcId();
		Integer[] drop = DROPS.get(npcId);
		if(drop != null)
		{
			int cond = drop[0];
			int item = drop[1];
			int max = drop[2];
			int chance = drop[3];
			int take = drop[4];
			if(st.getInt("cond") >= cond && st.getQuestItemsCount(item) < max && (take == 0 || st.getQuestItemsCount(take) > 0) && Rnd.chance(chance))
			{
				st.giveItems(item, 1);
				st.playSound(SOUND_ITEMGET);
				if(take != 0)
					st.takeItems(take, 1);
				/*if(BLOODED_FABRIC == item && st.getQuestItemsCount(BLOODED_FABRIC) >= 30)
				{
					QuestState FatesWhisper = st.getPlayer().getQuestState(_234_FatesWhisper.class);
					if(FatesWhisper != null && FatesWhisper.getCond() == 8)
						FatesWhisper.set("cond", 9);
				}*/
			}
		}
		if(npcId == ANGEL_KILLER)
			return "Ha, that was fun! If you wish to find the key, search the corpse";
		return null;
	}
}
