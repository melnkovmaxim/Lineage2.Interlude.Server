package ai;

import ru.agecold.gameserver.instancemanager.GrandBossManager;
import ru.agecold.gameserver.model.actor.instance.L2MonsterInstance;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
 
public class FrintezzaClan extends QuestJython
{
	private static final int _undeadband_member_leader = 18335;
	private static final int _hall_keeper_captain = 18329;
	
	public FrintezzaClan(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		this.addKillId(_hall_keeper_captain);
		this.addKillId(_undeadband_member_leader);
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{ 
		L2MonsterInstance mn = null;
		GrandBossManager gb = GrandBossManager.getInstance();
		switch(npc.getNpcId())
		{
			case _hall_keeper_captain:
				mn = (L2MonsterInstance)gb.createOnePrivateEx(_hall_keeper_captain,npc.getX(),npc.getY(),npc.getX(),0);
				break;
			case _undeadband_member_leader:
				mn = (L2MonsterInstance)gb.createOnePrivateEx(_undeadband_member_leader,npc.getX(),npc.getY(),npc.getX(),0);
				break;
		}
		mn.setRunning();
		return null; 
	}
	
	public static void main (String... arguments )
	{
		new FrintezzaClan(-1,"FrintezzaClan","FrintezzaClan");
	}
}
