package scripts.commands.voicedcommandhandlers;

import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IVoicedCommandHandler;

public class ModKick implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = { "kick"};

	/* (non-Javadoc)
	* @see ru.agecold.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, ru.agecold.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	*/
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{ 
		if (activeChar.isModerator())
		{
			String Moder = activeChar.getForumName();
			
			if (command.equalsIgnoreCase("kick"))
			{  
				L2Object mtarget = activeChar.getTarget();
				if (mtarget != null)
				{
					L2PcInstance targetPlayer = null;
					if (mtarget.isPlayer())
						targetPlayer = (L2PcInstance)mtarget;
					else
					{
						activeChar.sendModerResultMessage("������ ������� �_�");
						return false;
					}	
					
					if (targetPlayer.getPrivateStoreType() != 0)
					{
						activeChar.sendModerResultMessage(targetPlayer.getName() + " ������ �� ����");
						targetPlayer.sendMessage("* * * * * * * * * * * * * *"); 
						targetPlayer.sendMessage("��� ������ "+activeChar.getName()+"("+Moder+")");
						targetPlayer.sendMessage("* * * * * * * * * * * * * *"); 
						targetPlayer.kick();
						//
						activeChar.logModerAction(Moder, "������ "+targetPlayer.getName());
					}
				}
			}
		}
		return true;
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}