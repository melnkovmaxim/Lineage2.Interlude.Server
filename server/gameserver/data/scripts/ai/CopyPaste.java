package ai;

import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
 
public class CopyPaste extends QuestJython
{
	public CopyPaste(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		State st = new State("Start", this); //*

		this.setInitialState(st); //*
		this.addSpawnId(29046);
		this.addAttackId(29046);
		this.addKillId(29046);
		this.addStartNpc(29046); //*
		this.addTalkId(29046); //*
	}
	
    public String onSpawn(L2NpcInstance npc) 
	{ 
		System.out.println("@@@@@@@@@@@@@@@@");
		return null; 
	} 
	
    public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet) 
	{ 
		System.out.println("#!#!#!#!#!#!#");
		return null; 
	} 
	
	public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{ 
		System.out.println("2232323#!#!#!#!#!#!#");
		return null; 
	}
	
    public String onTalk (L2NpcInstance npc, L2PcInstance talker) //*
	{ 
		System.out.println("33333333#!#!#!#!#!#!#");
		return null; 
	}
	
	public static void main (String... arguments )
	{
		new CopyPaste(-1,"CopyPaste","CopyPaste");
	}
}
