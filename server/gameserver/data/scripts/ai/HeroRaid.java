package ai;

import ru.agecold.gameserver.model.L2Party;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.gameserver.Announcements;
import ru.agecold.util.Rnd;
@SuppressWarnings("all")
public class HeroRaid extends QuestJython
{
	//РБ которого необходимо убить для получения геройства
    private final static int HeroRaid = 25163;
	//Шанс получения геройства
    private final static int chance_hero = 100;
	//На сколько дней выдавать геройство
	//1 - на 1 день выдаётся
	//0 - до релога
	private final static int hero_day = 1;
	//Давать хиро последнему добившему или всей пати
	// 0 - только добившему
	// 1 - всей пати + добившему (если без пати)
	// 2 - только всей пати (тольк тем кто в пати)
	private final static int hero_to_all_party = 1;	
	
	public HeroRaid(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		this.addKillId(HeroRaid);
	}

	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{ 	
		int SWITCH = 0;
		Announcements annons = new Announcements();
		L2Party party = killer.getParty();
		if (Rnd.get(100) < chance_hero)
			{
				if(hero_to_all_party == 0)
					if(!killer.isHero())
					{
							annons.announceToAll("Хиро рб убил '"+killer.getName()+"'");
							annons.announceToAll("поздравляем его!");
							killer.setHero(hero_day);
							killer.sendCritMessage("Поздравляю вас!");
							killer.sendCritMessage("Вы добили Хиро РБ!");
							killer.sendCritMessage("Вы получаете статус героя на 1 день");
					}
					else
					{
						annons.announceToAll("Хиро рб убил "+killer.getName()+".");
						annons.announceToAll("Но он и так герой :)");
						killer.sendCritMessage("Ты и так герой!");
					}
					if(hero_to_all_party == 1)
						{
							if (party != null)
							{
								for (L2PcInstance member : party.getPartyMembers())
								{
									if (!member.isHero())
									{
										if(member.isInsideRadius(killer, 2000, false, false) && !member.isDead())
										{
											member.setHero(hero_day);
											member.sendCritMessage("Поздравляю вас!");
											member.sendCritMessage("Ваша группа убила Хиро РБ!");
											member.sendCritMessage("Вы получаете статус героя на 1 день");
											SWITCH = 0;
										}
									}
									else
										{
											SWITCH = 1;
											member.sendCritMessage("Ты и так герой!");
										}
								}
								switch(SWITCH)
								{
									case 0: 
										{
											annons.announceToAll("Хиро рб убил "+killer.getName());
											annons.announceToAll("поздравляем его и его группу!");
											break;
										}
									case 1:
										{
											annons.announceToAll("Хиро рб убил "+killer.getName()+".");
											annons.announceToAll("Но он уже хиро");
											annons.announceToAll("Поздравляем его группу!");
											break;
										}
								}
							}
							else
								if(!killer.isHero())
									{
										annons.announceToAll("Хиро рб убил "+killer.getName());
										annons.announceToAll("поздравляем его!");
										killer.setHero(hero_day);
										killer.sendCritMessage("Поздравляю вас!");
										killer.sendCritMessage("Вы добили Хиро РБ!");
										killer.sendCritMessage("Вы получаете статус героя на 1 день");									
									}
								else
									{
										annons.announceToAll("Хиро рб убил "+killer.getName()+".");
										annons.announceToAll("Но он и так Герой :)");
										killer.sendCritMessage("Ты и так герой!");
									}
						}
						if(hero_to_all_party == 2)
							if(!killer.isHero())
								{
									annons.announceToAll("Хиро рб убил "+killer.getName());
									annons.announceToAll("поздравляем его!");
									killer.setHero(hero_day);
									killer.sendCritMessage("Поздравляю вас!");
									killer.sendCritMessage("Вы добили Хиро РБ!");
									killer.sendCritMessage("Вы получаете статус героя на 1 день");
								}
							else
								{
									annons.announceToAll("Хиро рб убил "+killer.getName()+".");
									annons.announceToAll("Но он и так герой :)");
									killer.sendCritMessage("Ты и так герой!");
								}
			}		
		return null; 
	}






	public static void main(String... arguments )
	{
		new HeroRaid(-1, "HeroRaid", "HeroRaid");
	}
}
