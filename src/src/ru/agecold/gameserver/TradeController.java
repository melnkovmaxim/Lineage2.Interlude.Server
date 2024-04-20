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
package ru.agecold.gameserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import ru.agecold.Config;
import ru.agecold.L2DatabaseFactory;
import ru.agecold.gameserver.datatables.ItemTable;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.L2TradeList;
import ru.agecold.mysql.Close;
import ru.agecold.mysql.Connect;
import ru.agecold.util.log.AbstractLogger;

/**
 * This class ...
 *
 * @version $Revision: 1.5.4.13 $ $Date: 2005/04/06 16:13:38 $
 */
public class TradeController
{
	private static Logger _log = AbstractLogger.getLogger(TradeController.class.getName());
	private static TradeController _instance;

	private int _nextListId;
	private Map<Integer, L2TradeList> _lists;
	private Map<Integer, L2TradeList> _listsTaskItem;
    /** Task launching the function for restore count of Item (Clan Hall) */
    public class RestoreCount implements Runnable
    {
    	private int _timer;
    	public RestoreCount(int time)
		{
    		_timer = time;
    	}
        public void run()
        {
        	try 
			{
            	restoreCount(_timer);
            	dataTimerSave(_timer);
            	ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(_timer), (long)_timer*60*60*1000);
        	}
        	catch (Throwable t) {}
        }
    }
	
	public static TradeController getInstance()
	{
		return _instance;
	}
	public static void init()
	{
		_instance = new TradeController();
	}

	private TradeController()
	{
		int dummyItemCount = 0;
		boolean LimitedItem = false;
		_lists = new FastMap<Integer, L2TradeList>();
		_listsTaskItem = new FastMap<Integer, L2TradeList>();
		Connect con = null;
		PreparedStatement statement = null;
		PreparedStatement statement1 = null;
		ResultSet rset1 = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.get();
			statement1 = con.prepareStatement("SELECT " + L2DatabaseFactory.safetyString(new String[]
					{ "shop_id", "npc_id" }) + " FROM merchant_shopids");
			rset1 = statement1.executeQuery();
			while (rset1.next())
			{
				statement = con.prepareStatement("SELECT " + L2DatabaseFactory.safetyString(new String[]
						{ "item_id", "price", "shop_id", "order", "count", "time", "currentCount" }) + " FROM merchant_buylists WHERE shop_id=? ORDER BY "
							+ L2DatabaseFactory.safetyString(new String[]
								{ "order" }) + " ASC");
				statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
				rset = statement.executeQuery();
				if (rset.next())
				{
					LimitedItem = false;
					dummyItemCount++;
					L2TradeList buy1 = new L2TradeList(rset1.getInt("shop_id"));
					int itemId = rset.getInt("item_id");
					int price = rset.getInt("price");
					int count = rset.getInt("count");
					int currentCount = rset.getInt("currentCount");
					int time = rset.getInt("time");
					L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
					if (item == null)
					{
						Close.SR(statement,rset);
						continue;
					}
					
					if(count > -1)
					{
						item.setCountDecrease(true);
						LimitedItem = true;
					}
					item.setPriceToSell(price);
					item.setTime(time);
					item.setInitCount(count);
					if(currentCount>-1)
						item.setCount(currentCount);
					else
						item.setCount(count);
						
					buy1.addItem(item);
					buy1.setNpcId(rset1.getString("npc_id"));
					try
					{
						while (rset.next())
						{
							dummyItemCount++;
							itemId = rset.getInt("item_id");
							price = rset.getInt("price");
							count = rset.getInt("count");
							time = rset.getInt("time");
							currentCount = rset.getInt("currentCount");
							L2ItemInstance item2 = ItemTable.getInstance().createDummyItem(itemId);
							if (item2 == null) 
								continue;
							if(count > -1)
							{
								item2.setCountDecrease(true);
								LimitedItem = true;
							}
							item2.setPriceToSell(price);
							item2.setTime(time);
							item2.setInitCount(count);
							if(currentCount>-1)
								item2.setCount(currentCount);
							else
								item2.setCount(count);
							buy1.addItem(item2);
						}
					} 
					catch (Exception e)
					{
						_log.warning("TradeController: Problem with buylist " + buy1.getListId() + " item " + itemId);
					}
					if(LimitedItem)
						_listsTaskItem.put(Integer.valueOf(buy1.getListId()), buy1);
					else
						_lists.put(Integer.valueOf(buy1.getListId()), buy1);
					_nextListId = Math.max(_nextListId, buy1.getListId() + 1);
				}
				Close.SR(statement,rset);
			}
			Close.SR(statement1,rset1);
			Close.SR(statement,rset);
	        /*
			 *  Restore Task for reinitialyze count of buy item
			 */
			try
			{
				int time=0;
				long savetimer=0;
				long currentMillis = System.currentTimeMillis();
				statement = con.prepareStatement("SELECT DISTINCT time, savetimer FROM merchant_buylists WHERE time <> 0 ORDER BY time");
				rset = statement.executeQuery();
				while (rset.next())
				{
					time = rset.getInt("time");
					savetimer = rset.getLong("savetimer");
					if(savetimer-currentMillis>0)
						ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(time), savetimer-System.currentTimeMillis());
					else
						ThreadPoolManager.getInstance().scheduleGeneral(new RestoreCount(time), 0);
				}
			}
			catch (Exception e)
			{
				_log.warning("TradeController: Could not restore Timer for Item count.");
				e.printStackTrace();
			}
			finally
			{
				Close.SR(statement,rset);
			}
		} 
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warning("TradeController: Buylists could not be initialized.");
			e.printStackTrace();
		}
		finally
		{
			Close.SR(statement,rset);
			Close.SR(statement1,rset1);
			Close.C(con);
		}
		_log.config("TradeController: Loaded " + _lists.size() + " Buylists and " + _listsTaskItem.size() + " Limited Buylists.");
	}

	private int parseList(String line)
	{
		int itemCreated = 0;
		StringTokenizer st = new StringTokenizer(line, ";");

		int listId = Integer.parseInt(st.nextToken());
		L2TradeList buy1 = new L2TradeList(listId);
		while (st.hasMoreTokens())
		{
			int itemId = Integer.parseInt(st.nextToken());
			int price = Integer.parseInt(st.nextToken());
			L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
			item.setPriceToSell(price);
			buy1.addItem(item);
			itemCreated++;
		}

		_lists.put(Integer.valueOf(buy1.getListId()), buy1);
		return itemCreated;
	}

	public L2TradeList getBuyList(int listId)
	{
		Integer r = Integer.valueOf(listId);
		if(_lists.get(r) != null)
				return _lists.get(r);
		return _listsTaskItem.get(r);
	}

	public List<L2TradeList> getBuyListByNpcId(int npcId)
	{
		List<L2TradeList> lists = new FastList<L2TradeList>();

		for (L2TradeList list : _lists.values())
		{
			if (list.getNpcId().startsWith("gm"))
				continue;
			if (npcId == Integer.parseInt(list.getNpcId()))
				lists.add(list);
		}
		for (L2TradeList list : _listsTaskItem.values())
		{
			if (list.getNpcId().startsWith("gm"))
				continue;
			if (npcId == Integer.parseInt(list.getNpcId()))
				lists.add(list);
		}
		return lists;
	}
	protected void restoreCount(int time)
	{
		if(_listsTaskItem==null)return;
		for (L2TradeList list : _listsTaskItem.values())
			list.restoreCount(time);
	}
	
	protected void dataTimerSave(int time)
	{
		long timerSave = System.currentTimeMillis()+(long)time*60*60*1000;
		Connect con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.get();
			statement = con.prepareStatement("UPDATE merchant_buylists SET savetimer =? WHERE time =?");
			statement.setLong(1, timerSave);
			statement.setInt(2, time);
			statement.executeUpdate();
            statement.close();
        } 
		catch (Exception e) 
		{
			_log.log(Level.SEVERE, "TradeController: Could not update Timer save in Buylist" );
		} 
		finally 
		{
			Close.CS(con,statement);
		}
	}
	public void dataCountStore()
	{
		if (_listsTaskItem==null) 
			return;
			
		int listId;
		Connect con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.get();
			for (L2TradeList list : _listsTaskItem.values())
			{
				if (list==null) 
					continue;
				listId = list.getListId();

				for (L2ItemInstance Item :list.getItems())
				{
					if (Item.getCount()<Item.getInitCount()) //needed?
					{
						statement = con.prepareStatement("UPDATE merchant_buylists SET currentCount=? WHERE item_id=? AND shop_id=?");
						statement.setInt(1, Item.getCount());
						statement.setInt(2, Item.getItemId());
						statement.setInt(3, listId);
						statement.executeUpdate();
						Close.S(statement);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "TradeController: Could not store Count Item" );
		}
		finally
		{
			Close.CS(con,statement);
		}
	}
	/**
	 * @return
	 */
	public synchronized int getNextId()
	{
		return _nextListId++;
	}

	/**
	 * @reload ofc
	 */
	public static void reload()
	{
		_instance = new TradeController();
	}
}
