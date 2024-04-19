package teleports.noble;

import javolution.text.TextBuilder;
import ru.agecold.gameserver.datatables.TeleportLocationTable;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.L2TeleportLocation;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
 
public class Noble extends QuestJython
{
	private static final int[] GK = {30006,30059,30080,30134,30146,30177,30233,30256,30320,30540,30576,30836,30848,30878,30899,31275,31320,31964};
	
	public Noble(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		State st = new State("Start", this); //*

		this.setInitialState(st); //*
		for (int i: GK)
		{
			this.addStartNpc(i); //*
			this.addTalkId(i); //*
		}
	}
	
    public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player) 
    {
		if (event.startsWith("adena_"))
		{
			L2ItemInstance coin = player.getInventory().getItemByItemId(57);
			if (coin == null || coin.getCount() < 1000)
				return "<html><body>Стоимость ТП = 1000 Adena</body></html>";
			player.destroyItemByItemId("Noble", 57, 1000, player, true);
			int id = Integer.valueOf(event.replace("adena_", ""));
			L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(id);
			if (list != null)
                player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
		}
		else if (event.startsWith("gatepass_"))
		{
			L2ItemInstance coin = player.getInventory().getItemByItemId(6651);
			if (coin == null || coin.getCount() < 1)
				return "<html><body>Стоимость ТП = 1 Noblesse Gate Pass</body></html>";
			player.destroyItemByItemId("Noble", 6651, 1, player, true);
			int id = Integer.valueOf(event.replace("gatepass_", ""));
			L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(id);
			if (list != null)
                player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
		}
	    else
		{
			npc.showChatWindow(player, "data/scripts/teleports/noble/" + event + ".htm");
			//return "" + event + ".htm";
		}	
    	return null; 
    } 
	
    public String onTalk (L2NpcInstance npc, L2PcInstance talker) //*
	{ 
	    int npcId = npc.getNpcId();

		//NpcHtmlMessage reply = new NpcHtmlMessage(getObjectId());
		TextBuilder replyMSG = new TextBuilder("<html><body>Для ноблов у нас отдельный сервис ;)<br>");
	    if (talker.isNoble())
		{
			replyMSG.append("<Оплата: 1 Noblesse Gate Pass.<br><a action=\"bypass -h Quest Noble " + npcId + "-8\">Другой город</a>&nbsp;<br>");
			replyMSG.append("<a action=\"bypass -h Quest Noble " + npcId + "-3\">Локации</a>&nbsp;<br>");
			replyMSG.append("<a action=\"bypass -h Quest Noble " + npcId + "-10\">7 печатей</a>&nbsp;<br><br>");
			replyMSG.append("Оплата: 1000 Adena.<br><a action=\"bypass -h Quest Noble " + npcId + "-9\">Другой город</a>&nbsp;<br>");
			replyMSG.append("<a action=\"bypass -h Quest Noble " + npcId + "-2\">Локации</a>&nbsp;<br>");
			replyMSG.append("<a action=\"bypass -h Quest Noble " + npcId + "-11\">7 печатей</a>&nbsp;<br><br>");
			replyMSG.append("<a action=\"bypass -h npc_%objectId%_Chat 0\">Вернуться</a>");
		}
		else
			replyMSG.append("Но вы не нобл :( <br><br><a action=\"bypass -h npc_%objectId%_Chat 0\">Вернуться</a>");
		replyMSG.append("</body></html>");
		//reply.setHtml(replyMSG.toString());
		//player.sendPacket(reply);
		return replyMSG.toString(); 
	}
	
	public static void main (String... arguments )
	{
		new Noble(-1,"Noble","Noble");
	}
}
