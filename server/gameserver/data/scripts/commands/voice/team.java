package commands.voice;

import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IVoicedCommandHandler;
import scripts.commands.VoicedCommandHandler;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.model.L2Party;


public class team implements IVoicedCommandHandler
{
	private final static String[] VOICED_COMMANDS = {"team","ag_","ag_team"};

    public team()
    {
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
    }

	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
	{
		if (player == null)
				return false;
		else if(command.startsWith("team") && player.isGM())
		{
                    SortTeam(player);
                }
		else if(command.startsWith("ag_team") && player.isGM())
		{
                    String data = command.substring(8);

                    L2PcInstance playerteam = null;
                    
                    if(player.getTarget() != null)
                        if(player.getTarget().isPlayer())
                            playerteam = player.getTarget().getPlayer();

                    if(playerteam == null)
                    {
                        SortTeam(player);
                        return false;
                    }
                    
                    setTeam(Integer.parseInt(data), playerteam, player);
                }
		return false;
	}

        private void setTeam(int number, L2PcInstance playerteam, L2PcInstance player)
        {
                    L2Party party = playerteam.getParty();
                    
                    switch(number)
                    {
                        case 1:playerteam.setTeam(1);  
                            break;
                        case 2:playerteam.setTeam(2);
                            break;
                        case 3:playerteam.setTeam(0);
                            break;
                        case 4:
                                if(party != null)
                                {
                                    for(L2PcInstance i : party.getPartyMembers())
                                    {
                                        i.setTeam(1);
                                    }
                                }
                            break;
                        case 5:
                                if(party != null)
                                {
                                    for(L2PcInstance i : party.getPartyMembers())
                                    {
                                        i.setTeam(2);
                                    }
                                }
                            break;
                        case 6:
                                if(party != null)
                                {
                                    for(L2PcInstance i : party.getPartyMembers())
                                    {
                                        i.setTeam(0);
                                    }
                                }
                            break;
                    }
                    SortTeam(player);
        }
        
        private void SortTeam(L2PcInstance player)
        {
            if(!player.isGM())
                return;
            NpcHtmlMessage nhm = new NpcHtmlMessage(5);
            TextBuilder build = new TextBuilder("<html><body><title> Распределение команд </title><br><br>");
            build.append("<br><center><img src=\"l2ui.squaregray\" width=\"295\" height=\"1\"><br><br>");
            
            build.append("<center><table width=85% cellpadding=\"7\" >");
            build.append("<tr>");
            build.append("<td aling=center>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font color=00FF00> Команда на 1 персонажа </font> </td>");
            build.append("</tr>");
            build.append("<tr>");
            build.append("<td><a action=\"bypass -h ag_team 1\"> 1-я группа </a></td>");
            build.append("</tr>");
            build.append("<tr>");
            build.append("<td><a action=\"bypass -h ag_team 2\"> 2-я группа </a></td>");
            build.append("</tr>");
            build.append("<tr>");
            build.append("<td><a action=\"bypass -h ag_team 3\"> Убрать с группы </a></td>");
            build.append("</tr>");
            build.append("<tr>");
            build.append("<td aling=center>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font color=00FF00> Команда на всю пати </font></td>");
            build.append("</tr>");
            build.append("<tr>");
            build.append("<td><a action=\"bypass -h ag_team 4\"> 1-я группа </a></td>");
            build.append("</tr>");
            build.append("<tr>");
            build.append("<td><a action=\"bypass -h ag_team 5\"> 2-я группа </a></td>");
            build.append("</tr>");
            build.append("<tr>");
            build.append("<td><a action=\"bypass -h ag_team 6\"> Убрать с группы </a></td>");
            build.append("</tr>");
            build.append("</table></center><br>");	
            build.append("<br>");
            build.append("<img src=\"l2ui.squaregray\" width=\"295\" height=\"1\"><br>");
            build.append("</center></body></html>");
            nhm.setHtml(build.toString());
            player.sendPacket(nhm);
            build.clear();
            build = null;
            nhm = null; 
        }
        
	
	public static void main (String... arguments )
	{
		new team();
	}

    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }

}