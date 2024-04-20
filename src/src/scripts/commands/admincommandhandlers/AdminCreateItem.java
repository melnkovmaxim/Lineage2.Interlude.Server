/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;

import ru.agecold.Config;
import ru.agecold.gameserver.datatables.ItemTable;
import scripts.commands.IAdminCommandHandler;
import ru.agecold.gameserver.model.GMAudit;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.serverpackets.ItemList;
import ru.agecold.gameserver.templates.L2Item;

/**
 * This class handles following admin commands:
 * - itemcreate = show menu
 * - create_item <id> [num] = creates num items with respective id, if num is not specified, assumes 1.
 *
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminCreateItem implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_itemcreate",
		"admin_create_item",
		"admin_giveitem"
	};
	private static final int REQUIRED_LEVEL = Config.GM_CREATE_ITEM;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
				return false;
		}

		GMAudit.auditGMAction(activeChar.getName(), command, (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target"), "");

		if (command.equals("admin_itemcreate"))
		{
			AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_create_item"))
		{
			try
			{
				String val = command.substring(17);
				StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens()== 2)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					String num = st.nextToken();
					int numval = Integer.parseInt(num);
					giveItem(activeChar, null, idval, numval);
				}
				else if (st.countTokens()== 1)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					giveItem(activeChar, null, idval, 1);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendAdmResultMessage("Usage: //itemcreate <itemId> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendAdmResultMessage("Specify a valid number.");
			}
			AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_giveitem"))
		{
			try
			{
				L2Object obj = activeChar.getTarget();
				if (obj == null || !(obj.isPlayer()))
				{
					activeChar.sendAdmResultMessage("�� ���� ����� � ������.");	
					return false;
				}
					
				L2PcInstance rewdr = (L2PcInstance) obj;
				
				String val = command.substring(15);
				StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens()== 2)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					String num = st.nextToken();
					int numval = Integer.parseInt(num);
					giveItem(activeChar, rewdr, idval, numval);
				}
				else if (st.countTokens()== 1)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					giveItem(activeChar, rewdr, idval, 1);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendAdmResultMessage("Usage: ����� ������� ������ � ������ � ������� //giveitem itemId count");
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendAdmResultMessage("Specify a valid number.");
			}
		}
		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}
	
	private void giveItem(L2PcInstance admin, L2PcInstance rewdr, int itemId, int count)
	{
		L2Item template = ItemTable.getInstance().getTemplate(itemId);
		if (template == null)
		{
			admin.sendAdmResultMessage("������: ���� " + itemId + " �� ����������.");
			return;
		}
		if (count > 1 && !template.isStackable())
			count = 1;
		if (rewdr != null)
		{
			rewdr.addItem("Admin", itemId, count, admin, true);
			admin.sendAdmResultMessage("������ " + template.getName() + "(" + count + ") ������ " + rewdr.getName() + ".");
		}
		else
		{
			admin.addItem("Admin", itemId, count, admin, true);
			admin.sendAdmResultMessage("�������� " + template.getName() + "(" + count + ").");
		}
	}
}
