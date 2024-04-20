package scripts.commands.voicedcommandhandlers;

import javolution.text.TextBuilder;

import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;
import scripts.commands.IVoicedCommandHandler;

public class ModHelp implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = { "moderhelp"};

	/* (non-Javadoc)
	* @see ru.agecold.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, ru.agecold.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	*/
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{ 
		if (activeChar.isModerator())
		{
			String Moder = activeChar.getForumName();
			int rank = activeChar.getModerRank();
			
			if (command.equalsIgnoreCase("moderhelp"))
			{  
				NpcHtmlMessage nhm = NpcHtmlMessage.id(5);
				TextBuilder build = new TextBuilder("<html><body>");
				build.append("<center><font color=\"LEVEL\">������</font></center><br><br>");
				build.append("��������� <font color=\"LEVEL\">" + Moder + "</font>; rank " + rank + "<br><br>");
				build.append("C����� ������:<br>");
				build.append("<font color=66CC00>.���������</font> (��� ��������); ��������<br>");
				build.append("<font color=66CC00>.banchat</font> (.banchat Hope 120); ��� ���� � �������<br>");
				build.append("<font color=66CC00>.unbanchat</font> (.unbanchat Hope); ����� ��� ����<br>");
				build.append("<font color=66CC00>.kick</font> (����� ���� � ������); ������� ��������<br>");
				build.append("<font color=66CC00>.cleartitle</font> (����� ���� � ������); ������� �����<br>");
				switch (rank)
				{
					case 2:
						build.append("<font color=CC9900>.showstat</font> (.showstat Nick); ����� ����<br>");
						//build.append("<font color=66CC00>.showinv<font> (.showinv Nick); ������ ����<br>");
						break;
				}
				build.append("<br><br>");
				build.append("</body></html>");
				nhm.setHtml(build.toString());
				activeChar.sendPacket(nhm);
			}
		}
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}