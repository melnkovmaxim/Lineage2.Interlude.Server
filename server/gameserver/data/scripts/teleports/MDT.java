package teleports;

import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;

/* add to https://steve.dog */
 
public class MDT extends QuestJython
{
	private static final int GK = 30995;
	
	public MDT(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		State st = new State("Start", this); //*

		this.setInitialState(st); //*
		this.addStartNpc(GK); //*
		this.addTalkId(GK); //*
	}
	
    public String onTalk (L2NpcInstance npc, L2PcInstance talker) //*
	{ 
	    talker.teleToLocation(83400, 147943, -3404, false);
		return null;
	}
	
	public static void main (String... arguments )
	{
		new MDT(-1,"MDT","MDT");
	}
}
