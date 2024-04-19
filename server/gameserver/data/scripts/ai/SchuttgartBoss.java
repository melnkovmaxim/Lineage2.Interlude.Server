package ai;

import ru.agecold.Config;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import scripts.autoevents.schuttgart.Schuttgart;
 
public class SchuttgartBoss extends QuestJython
{
	public SchuttgartBoss(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		this.addKillId(Config.SCH_BOSS);
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{ 
		Schuttgart.getEvent().notifyBossDie(killer);
		return null; 
	}
	
	public static void main (String... arguments )
	{
		new SchuttgartBoss(-1,"SchuttgartBoss","SchuttgartBoss");
	}
}
