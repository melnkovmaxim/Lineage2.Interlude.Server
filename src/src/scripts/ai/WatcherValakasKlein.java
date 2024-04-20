package scripts.ai;

import ru.agecold.Config;
import ru.agecold.gameserver.instancemanager.GrandBossManager;
import ru.agecold.gameserver.instancemanager.bosses.ValakasManager;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.templates.L2NpcTemplate;

public class WatcherValakasKlein extends L2NpcInstance
{
	private static String htmPath = "data/html/teleporter/";
	
	public WatcherValakasKlein(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
    public void onBypassFeedback(L2PcInstance player, String command)
    {
		//System.out.println("##->" + command + "<-##");
		if(command.equalsIgnoreCase("enterLair"))
		{
			if (player.getItemCount(7267) >= 1 || !(Config.NOEPIC_QUESTS))
				player.teleToLocation(183920, -115544, -3294);
			else
				showChatWindow(player, htmPath + getNpcId() + "-8.htm");
			return;
		}
		else if(command.equalsIgnoreCase("checkLair"))
		{
			ValakasManager vm = ValakasManager.getInstance();
			switch(vm.getStatus())
			{
				case 1:
				case 5:
					int cnt = GrandBossManager.getInstance().getZone(213004, -114890, -1635).getPlayersCount();
					if(cnt < 50)
						showChatWindow(player, htmPath + getNpcId() + "-3.htm");
					else if(cnt < 100)
						showChatWindow(player, htmPath + getNpcId() + "-4.htm");
					else if(cnt < 150)
						showChatWindow(player, htmPath + getNpcId() + "-5.htm");
					else if(cnt < 200)
						showChatWindow(player, htmPath + getNpcId() + "-6.htm");
					else
						showChatWindow(player, htmPath + getNpcId() + "-7.htm");
					break;
				default:
					player.sendHtmlMessage("����� �������� �����:", "������ �� �� ������ ����� � ��� �������.");
					break;
			}
		}
		else if(command.startsWith("Chat"))
		{
			showChatWindow(player, htmPath + getNpcId() + "-" + Integer.parseInt(command.substring(4).trim()) + ".htm");
			return;
		}
		else
			super.onBypassFeedback(player, command);
        player.sendActionFailed();
    }
}
