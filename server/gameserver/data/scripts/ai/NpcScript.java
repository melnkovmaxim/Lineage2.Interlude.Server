package ai;

import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.util.Rnd;
import ru.agecold.gameserver.datatables.SkillTable;
import ru.agecold.gameserver.model.L2Party;

public class NpcScript extends QuestJython
{
	//Босс Иды
	private final static int[] BossIds = {15162};
    //Выдавать награду всей пати?
    private static boolean PartyDrop = false;
    //Включить награду нублом?
    private static boolean NOBLE = false;
    //Включить награду хиро?
    private static boolean HERO = false;
    //На сколько давать хиро
	private final static int DayHero = 1;
    //Включить награду премиум?
    private static boolean PREMIUM = true;
    //На сколько давать премиум
	private final static int DayPremium = 1;
    //Включить награду итемом?
    private static boolean RewardItem = false;
    //Итем ид
	private final static int RewardItemId = 57;
    //Количество
	private final static int RewardItemCount = 1;
    //Шанс дропа предмета
    private final static int chance = 20;
    //Сколько разрешено иметь в инве таких предметов
    private final static int count = 1;
    //Включить награду скилами?
    private static boolean RewardSkill = false;
    //Скилл ид
	private final static int RewardSkillId = 2;
    //Скилл лвл
	private final static int RewardSkillLvl = 1;

	public NpcScript(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		
        for (int boss : BossIds)
            addKillId(boss);
	}
	
    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance xzkaknazvat, boolean isPet)
    {
        int npcId = npc.getNpcId();
				for (int id : BossIds)
					if (npcId == id)
              						   if (HERO) 
              						   {
										L2Party party = xzkaknazvat.getParty();
										   if (xzkaknazvat.isHero()){
										   xzkaknazvat.sendMessage("\u0412\u044b \u0443\u0436\u0435 \u0433\u0435\u0440\u043e\u0439 \u003A\u0028.");
										   } else {
													if (party != null && PartyDrop)
													  for (L2PcInstance xzkaknazvatpaty : party.getPartyMembers())
													  {
													   xzkaknazvatpaty.setHero(DayHero);
													   xzkaknazvatpaty.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u0441\u0442\u0430\u0442\u0443\u0441 \u0433\u0435\u0440\u043e\u044f.");
													  }
													else
													{
													  xzkaknazvat.setHero(DayHero);
													  xzkaknazvat.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u0441\u0442\u0430\u0442\u0443\u0441 \u0433\u0435\u0440\u043e\u044f.");
												}
									   }
									}
              						   if (PREMIUM) 
              						   {
               						    L2Party party = xzkaknazvat.getParty();
										if (xzkaknazvat.isPremium()){
										   xzkaknazvat.sendMessage("\u0412\u044b \u0443\u0436\u0435 \u0433\u0435\u0440\u043e\u0439 \u003A\u0028.");
										   } else {
               						            if (party != null && PartyDrop)
               						      for (L2PcInstance xzkaknazvatpaty : party.getPartyMembers())
										  {
               						        xzkaknazvatpaty.storePremium(DayPremium);
										    xzkaknazvatpaty.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u043f\u0440\u0435\u043c\u0438\u0443\u043c.");
										  }
               						    else
										{
               						      xzkaknazvat.storePremium(DayPremium);
										  xzkaknazvat.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u043f\u0440\u0435\u043c\u0438\u0443\u043c.");
										}
										}
									   }
              						   if (NOBLE) 
              						   {
               						    L2Party party = xzkaknazvat.getParty();
               						    if (party != null && PartyDrop)
               						      for (L2PcInstance xzkaknazvatpaty : party.getPartyMembers())
										  {
               						        xzkaknazvatpaty.setNoble(true);
										    xzkaknazvatpaty.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u043d\u0443\u0431\u043b\u0435\u0441.");
										  }
               						    else
										{
               						      xzkaknazvat.setNoble(true);
										  xzkaknazvat.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u043d\u0443\u0431\u043b\u0435\u0441.");
										}
									   }
              						   if (RewardItem) 
              						   {
               						    L2Party party = xzkaknazvat.getParty();
               						    if (party != null && PartyDrop)
               						      for (L2PcInstance xzkaknazvatpaty : party.getPartyMembers())
               						        if (Rnd.get(100) < chance)
               						          if (xzkaknazvatpaty.getInventory().getInventoryItemCount(RewardItemId,0) < count)
										      {
										        xzkaknazvatpaty.getInventory().addItem("Reward", RewardItemId, RewardItemCount, xzkaknazvatpaty, null);
											    xzkaknazvatpaty.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442.");
											    xzkaknazvatpaty.broadcastUserInfo();
										      }
               						    else
										{
               						      if (Rnd.get(100) < chance)
               						        if (xzkaknazvat.getInventory().getInventoryItemCount(RewardItemId,0) < count)
										      xzkaknazvat.getInventory().addItem("Reward", RewardItemId, RewardItemCount, xzkaknazvat, null);
											  xzkaknazvat.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442.");
											  xzkaknazvat.broadcastUserInfo();
										}
									   }
              						   if (RewardSkill) 
              						   {
               						    L2Party party = xzkaknazvat.getParty();
               						    if (party != null && PartyDrop)
               						      for (L2PcInstance xzkaknazvatpaty : party.getPartyMembers())
										  {
               						        xzkaknazvatpaty.removeSkill(SkillTable.getInstance().getInfo(RewardSkillId, RewardSkillLvl));
               						        xzkaknazvatpaty.addSkill(SkillTable.getInstance().getInfo(RewardSkillId, RewardSkillLvl), true);
										    xzkaknazvatpaty.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u043d\u043e\u0432\u044b\u0439 \u0441\u043a\u0438\u043b.");
										  }
               						    else
										{
               						      xzkaknazvat.removeSkill(SkillTable.getInstance().getInfo(RewardSkillId, RewardSkillLvl));
               						      xzkaknazvat.addSkill(SkillTable.getInstance().getInfo(RewardSkillId, RewardSkillLvl), true);
										  xzkaknazvat.sendMessage("\u0412\u044b \u043f\u043e\u043b\u0443\u0447\u0438\u043b\u0438 \u043d\u043e\u0432\u044b\u0439 \u0441\u043a\u0438\u043b.");
										}
									   }
        return null;
    }

	public static void main(String[] args)
	{
		new NpcScript(-1, "NpcScript", "ai");
	}
}