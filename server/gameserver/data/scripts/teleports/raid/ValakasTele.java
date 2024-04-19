package teleports.raid;

import ru.agecold.gameserver.datatables.DoorTable;
import ru.agecold.gameserver.instancemanager.bosses.ValakasManager;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;

public class ValakasTele extends QuestJython
{
	public ValakasTele(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		State st = new State("Start", this);
		
		this.setInitialState(st);
		this.addKillId(29030);
		this.addKillId(29036);
		this.addKillId(29037);
		this.addKillId(29040);
	}
	
	@Override
	public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{ 
		DoorTable dt = DoorTable.getInstance();
		ValakasManager vm = ValakasManager.getInstance();
		if (vm.getStatus() != 1)
			return null; 
			
		switch(npc.getNpcId())
		{
			case 29030:
				dt.getDoor(24210004).openMe();
				break;
			case 29036:
				dt.getDoor(24210005).openMe();
				break;
			case 29037:
				dt.getDoor(24210006).openMe();
				break;
			case 29040:
				vm.setState(5, 0);
				break;
		}
		return null; 
	}
	//
	
	public static void main (String... arguments )
	{
		new ValakasTele(-1,"ValakasTele","ValakasTele");
	}
}
