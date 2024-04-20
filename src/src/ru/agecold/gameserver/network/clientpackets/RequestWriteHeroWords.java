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
package ru.agecold.gameserver.network.clientpackets;

import ru.agecold.Config;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.model.BlockList;
import ru.agecold.gameserver.model.L2World;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.serverpackets.CreatureSay;
import ru.agecold.util.TimeLogger;
import ru.agecold.util.Log;
/**
 * Format chS
 * c (id) 0xD0
 * h (subid) 0x0C
 * S the hero's words :)
 * @author -Wooden-
 *
 */
public final class RequestWriteHeroWords extends L2GameClientPacket
{
	private String _text;

	/**
	 * @param buf
	 * @param client
	 */
	@Override
	protected void readImpl()
	{
		_text = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
			
		if (!player.isHero())
			return;
			
		if(!player.isGM() && System.currentTimeMillis() - player.gCPBH() < 5000)
		{
			player.sendPacket(Static.HERO_DELAY);
			return;
		}
		player.sCPBH();
			
        if (_text.length() > Config.MAX_CHAT_LENGTH) 
     	 	_text = _text.substring(0, Config.MAX_CHAT_LENGTH); 
                
		_text = _text.replaceAll("\n", "");
		_text = _text.replace("\n", "");
        _text = _text.replace("n\\", "");
        _text = _text.replace("\r", "");
        _text = _text.replace("r\\", "");

		_text = ltrim(_text);
		_text = rtrim(_text);
		_text = itrim(_text);
		_text = lrtrim(_text);
		
		if (_text.isEmpty())
			return;
			
        if (Config.USE_CHAT_FILTER && !_text.startsWith("."))
        {
			String[] badwords = 
			{
				"���",
				"���",
				"����",
				"����",
				"����",
				"����",
				"���",
				"���",
				"���",
				"����",
				"����",
				"����",
				"����",
				"����",
				"���",
				"����",
				"����",
				"����",
				"eba",
				"ebu",
				"�����",
				"������",
				"�����",
				"����",
				"����"
			};
				
			String wordn = "";
			String wordf = "";
			String newTextt = "";
			String delims = "[ ]+";
			String[] tokens = _text.split(delims);
			for (int i = 0; i < tokens.length; i++)
			{	
				wordn = tokens[i];
				String word = wordn.toLowerCase();
				word = word.replace("a","�");
				word = word.replace("c","�");
				word = word.replace("s","�");
				word = word.replace("e","�");
				word = word.replace("k","�");
				word = word.replace("m","�");
				word = word.replace("o","�");
				word = word.replace("0","�");
				word = word.replace("x","�");
				word = word.replace("uy","��");
				word = word.replace("y","�");
				word = word.replace("u","�");
				word = word.replace("�","�");
				word = word.replace("9","�");
				word = word.replace("3","�");
				word = word.replace("z","�");
				word = word.replace("d","�");
				word = word.replace("p","�");
				word = word.replace("i","�");
				word = word.replace("ya","�");
				word = word.replace("ja","�");
				for(String pattern : badwords)
				{
					if(word.matches(".*" + pattern + ".*"))
					{
						newTextt = word.replace(pattern, "-_-");
						break;
					}
					else
						newTextt = wordn;
				}
				wordf += newTextt + " ";
			}
			_text = wordf.replace("null","");
        }
		
		CreatureSay cs = new CreatureSay(player.getObjectId(), 17, player.getName(), _text);		
		for (L2PcInstance pchar : L2World.getInstance().getAllPlayers())
			if (!BlockList.isBlocked(pchar, player))
				pchar.sendPacket(cs);
							
		Log.add(TimeLogger.getTime() + player.getName() +": " + _text, "hero_chat");	
	}

    public static String ltrim(String source) 
	{
        return source.replaceAll("^\\s+", "");
    }

    public static String rtrim(String source) 
	{
        return source.replaceAll("\\s+$", "");
    }

    public static String itrim(String source) 
	{
        return source.replaceAll("\\b\\s{2,}\\b", " ");
    }


    public static String trim(String source) 
	{
        return itrim(ltrim(rtrim(source)));
    }

    public static String lrtrim(String source){
        return ltrim(rtrim(source));
    }

	/* (non-Javadoc)
	 * @see ru.agecold.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "C.WriteHeroWords";
	}

}