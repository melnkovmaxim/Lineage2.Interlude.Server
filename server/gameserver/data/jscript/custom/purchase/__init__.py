# -*- coding: utf-8 -*-
import sys
from javolution.text import TextBuilder
from ru.agecold import L2DatabaseFactory
from ru.agecold.gameserver.cache import HtmCache
from ru.agecold.gameserver.datatables import AugmentationData
from ru.agecold.gameserver.datatables import ItemTable
from ru.agecold.gameserver.datatables import SkillTable
from ru.agecold.gameserver.model import L2Augmentation
from ru.agecold.gameserver.model import L2ItemInstance
from ru.agecold.gameserver.model import L2Skill
from ru.agecold.gameserver.model import L2World
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.gameserver.network import SystemMessageId
from ru.agecold.gameserver.network.serverpackets import ExMailArrived
from ru.agecold.gameserver.network.serverpackets import ItemList
from ru.agecold.gameserver.network.serverpackets import ShowCalculator
from ru.agecold.gameserver.network.serverpackets import SystemMessage
from ru.agecold.gameserver.templates import L2Item
from ru.agecold.gameserver.util import Util, GiveItem
from ru.agecold.util import Log
from ru.agecold.util import Rnd
from time import gmtime, strftime

qn = "purchase"

#ИД нпц 60112
MARKET = 80009

#Запрещенные шмотки
FORBIDDEN = [6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,5588,11500,11501,11502]

PAGE_LIMIT = 10
PAGE_LIMIT_TOP = 4
SORT_LIMIT = 8
MAX_ADENA = 2147000000

#
CENCH = "CCCC33"
CPRICE = "669966"
CITEM = "bf196b"
CAUG1 = "37a6de"
CAUG2 = "1278ab"
COLOR_PRICE = "777777"
COLOR_PRICE2 = "a7f00a"
CITEM_TOP = "b0e053"
COLOR_PRICE_TOP = "777777"
CITEM_TOP2 = "f4ec1c"
COLOR_PRICE_TOP2 = "777777"

moneys = {12222: "Donate Coin", 11973: "Upgrade Coin", 11974: "Евент Коин", 14555: "Монета Убийцы", 11971: "L2TOP Coin", 12000: "Shadow Coin", 3483: "Голд Кнайт", 12019: "Darc Coin"}
sorts = {0: "Оружие", 1: "Броня", 2: "Бижутерия"}

moneys_black = {12222: "Donate Coin", 11973: "Upgrade Coin", 11974: "Евент Коин", 14555: "Монета Убийцы", 11971: "L2TOP Coin", 12000: "Shadow Coin", 3483: "Голд Кнайт", 12019: "Darc Coin"}

BLACK_MARKET = 0

#Плата за добавление ЛС, монеты удачи
AUGMENT_TAX = 0
#Плата за добавление шмоток, 4 - Агрейд, 5 - S грейд; фестивал адена
OTHER_TAX = {4: 0, 5: 0}

BUTTON_DECO = "back=\"sek.cbui94\" fore=\"sek.cbui92\""

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def init_LoadGlobalData(self) :
   print "purchase: ok"
   
 def closeStat(self,st):
   if st != None:
     st.close()
   return  
     
 def closeRes(self,rs):
   if rs != None:
     rs.close()
   return  
     
 def closeCon(self,con):
   if con != None:
     con.close()
   return

 def getMoneyCall(self,money):
   return moneys.get(money, str("none"))

 def getMoneyBlackCall(self,money):
   return moneys_black.get(money, "none")

 def formatNum(self,num):
   return str(Util.formatAdena(num))

 def canBeStoredEnch(self,item):
   if item.getEnchantLevel() <= 0:
     return False
   if self.isHeroItem(item.getItemId()):
     return False
   if item.isShadowItem():
     return False
   if item.isEquipped():
     return False
   if item.getItem().getDuration() >= 0:
     return False
   if item.getItem().getCrystalType() < 4 and item.getItemId() != 6660:
     return False
   if item.getItemId() in FORBIDDEN:
     return False
   if item.getItemId() == 8190 or item.getItemId() == 8689:
     return False
   return True

 def isHeroItem(self, itemId):
   return ((itemId >= 6611 and itemId <= 6621) or (itemId >= 9388 and itemId <= 9390) or itemId == 6842)

 def canBeEnchanted(self,item):
   if self.isHeroItem(item.getItemId()):
     return False
   if item.isShadowItem():
     return False
   if item.getItemId() == 8190 or item.getItemId() == 8689:
     return False
   return True

 def getSellerName(self,con,charId):
   return self.getSellerNameA(con,charId,True)    

 def getSellerNameA(self,con,charId,online):
   player = L2World.getInstance().getPlayer(charId)
   if player != None:
     if online:
       return "<font color=3d9725>"+player.getName()+"</font>"
     else:
       return player.getName()
   name = "???"
   try:
     st = con.prepareStatement("SELECT char_name FROM `characters` WHERE `obj_Id`=? LIMIT 1");
     st.setInt(1, charId);
     rs = st.executeQuery();
     if rs.next():
       name = rs.getString("char_name")
   except:
     pass
   if rs != None:    
     rs.close()
   if st != None:    
     st.close()
   return name

 def getFingerPrints(self, player):
   return player.getFingerPrints()

 def getAugmentSkill(self, aid, lvl, full):
   augment = SkillTable.getInstance().getInfo(aid, lvl)
   if augment == None:
     return ""
   if not augment.isAugment():
     return "" 
   augName = augment.getName();
   stype = "Шанс";
   ttype = "";
   power = ":" + str(int(augment.getPower())) + "power";
   if augment.isActive():
     stype = "Актив"
     if augment.isAuraSkill():
       stype = "Массовый";
   elif augment.isPassive():
     stype = "Пассив"
     power = "";
   if augment.getPower() == 0:
     power = ""
   if full:
     ttype = "<br><font color=3eace3>" + augment.getAugInfo() + "</font>"
   augName = augName.replace("Item Skill: ", "");  
   return "<font color="+CAUG1+">LS:</font> <font color="+CAUG2+">" + augName + " "+ str(lvl) +"Lvl(" + stype + power + ")</font>" + ttype + "";

 def isAugment(self, skillId):
   return (skillId >= 3080 and skillId <= 3299)

 def getPageCount(self, con, me, itemId, augment, type2, charId, name, ench, black):
   rowCount = 0
   pages = 0
   try:
     if black == 1:
       st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_black` WHERE `type` = ?")
       st.setInt(1, 50)
     elif type2 >= 0:
       st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_items` WHERE `type` = ?")
       st.setInt(1, type2)
     elif itemId > 0:
       st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_items` WHERE `itemId` = ?")
       st.setInt(1, itemId)
     elif augment > 0:
       st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_items` WHERE `augment` = ?")
       st.setInt(1, augment)
     elif name != "":
       if ench == 0:
         st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_items` WHERE `itemName` LIKE ?")
         st.setString(1, "%"+ name +"%")
       else:
         st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_items` WHERE `itemName` LIKE ? AND `enchant` = ?")
         st.setString(1, "%"+ name +"%")
         st.setInt(2, ench)
     elif ench > 0:
       st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_items` WHERE `enchant` = ?")
       st.setInt(1, ench)
     elif me == 1:
       st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_items` WHERE `ownerId` = ?")
       st.setInt(1, charId)
     else:
       st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_items` WHERE `id` > 0")
     rs=st.executeQuery()
     if (rs.next()):
       rowCount = rs.getInt(1)
   except :
     pass
   if rs != None:   
     rs.close()
   if st != None:   
     st.close()
   if rowCount == 0:
     return "0"
   pages = (rowCount / PAGE_LIMIT) + 1
   return str(pages)

 def error(self,action, text):
   return "<html><body> " + action + ": <br> " + text + "</body></html>"

 def getMainMenu(self):
   if BLACK_MARKET == 0:  
     return "<table width=280><tr><td></td><td align=right><button value=\"Офис\" action=\"bypass -h Quest purchase office\" width=70 height=17 "+BUTTON_DECO+"></td><td align=right><button value=\"Добавить\" action=\"bypass -h Quest purchase add\" width=70 height=17 "+BUTTON_DECO+"></td><td align=right><button value=\"Поиск\" action=\"bypass -h Quest purchase search\" width=40 height=17 "+BUTTON_DECO+"></td></tr></table>"
   return "<table width=280><tr><td><button value=\"Черный рынок\" action=\"bypass -h Quest purchase black_market\" width=70 height=17 "+BUTTON_DECO+"></td><td align=right><button value=\"Офис\" action=\"bypass -h Quest purchase office\" width=70 height=17 "+BUTTON_DECO+"></td><td align=right><button value=\"Добавить\" action=\"bypass -h Quest purchase add\" width=70 height=17 "+BUTTON_DECO+"></td><td align=right><button value=\"Поиск\" action=\"bypass -h Quest purchase search\" width=40 height=17 "+BUTTON_DECO+"></td></tr></table>"

 def transferPay(self,con,charId,itemId,enchant,augattr,augment,auhLevel,price,money,owner):
   item = ItemTable.getInstance().getTemplate(money)
   if item == None:
     return False
   result = GiveItem.insertItem(con, charId, money, price, 0, 0, 0, 0, "INVENTORY", owner)
   return result

 #def transferPay(self,con,charId,itemId,enchant,augment,auhLevel,price,money):
 #  item = ItemTable.getInstance().getTemplate(itemId)
 #  if item == None:
 #    return False
 #  data = str(strftime("%Y-%m-%d", gmtime()))
 #  time = str(strftime("%H:%M:%S", gmtime()))
 #  text = TextBuilder()
 #  text.append("Итем: <font color=FF3399>" + item.getName() + " +" + str(enchant) + " " + self.getAugmentSkill(augment, auhLevel, False) +"</font><br1> был успешно продан.<br1>")
 #  text.append("Благодарим за сотрудничество.")
 #  try:
 #    sName = self.getSellerNameA(con,charId,False)
 #    st = con.prepareStatement("INSERT INTO `z_post_pos` (`id`,`tema`,`text`,`from`,`to`,`type`,`date`,`time`,`itemName`,`itemId`,`itemCount`,`itemEnch`,`augData`,`augSkill`,`augLvl`) VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
 #    st.setString(1, "Шмотка продана")
 #    st.setString(2, text.toString())
 #    st.setString(3, "~zzAuction#.")
 #    st.setString(4, sName)
 #    st.setInt(5, 0)
 #    st.setString(6, data)
 #    st.setString(7, time)
 #    st.setString(8, item.getName())
 #    st.setInt(9, money)
 #    st.setInt(10, price)
 #    st.setInt(11, 0)
 #    st.setInt(12, 0)
 #    st.setInt(13, 0)
 #    st.setInt(14, 0)
 #    st.execute()
 #    alarm = L2World.getInstance().getPlayer(charId)
 #    if alarm:
 #      alarm.sendMessage("Уведомление с аукциона: проверь почту")
 #      alarm.sendPacket(ExMailArrived())
 #    return True
 #  except:
 #    return False
 #  finally:
 #    text.clear()
 #    if st != None:
 #      st.close()
 #  return False

 def getTopItems(self,player):
   return ""  
   text = TextBuilder("<br><table width=300><tr><td width=36></td><td width=264>")
   text.append("<font color=" + CITEM_TOP + ">***** Лучшие предложения *****</font></td></tr>")
   con = None
   rs = None
   st = None
   try:
     con=L2DatabaseFactory.getInstance().getConnection()
     st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow FROM `z_stock_items` WHERE `top` IN (?,?) ORDER BY RAND() LIMIT ?")
     st.setInt(1, 1)
     st.setInt(2, 3)
     st.setInt(3, PAGE_LIMIT_TOP)
     rs=st.executeQuery()
     while (rs.next()):
       sId = rs.getInt("id")
       itmId = rs.getInt("itemId")
       ownerId = rs.getInt("ownerId")
       brokeItem = ItemTable.getInstance().getTemplate(itmId)
       if brokeItem == None:
         continue
       priceB = "<font color="+COLOR_PRICE_TOP+">" + self.formatNum(rs.getInt("price")) + " " + self.getMoneyCall(rs.getInt("money")) + "; \"" + self.getSellerName(con,ownerId) + "</font>"
       if player.getObjectId() == ownerId:
         priceB = "<table width=240><tr><td width=160><font color=" + COLOR_PRICE_TOP + ">" + self.formatNum(rs.getInt("price")) + " " + self.getMoneyCall(rs.getInt("money")) + ";</font></td><td align=right><button value=\"X\" action=\"bypass -h Quest purchase StockBuyItem_" + str(sId) + "_ 1 _ 1\" width=25 height=15 "+BUTTON_DECO+"></td></tr></table><br1>"
       text.append("<tr><td><img src=\"" + brokeItem.getIcon() + "\" width=32 height=32></td><td><a action=\"bypass -h Quest purchase StockShowItem_" + str(sId) + "_1\"> <font color="+CITEM_TOP+">" + brokeItem.getName() + "</font></a><font color="+CENCH+">+" + str(rs.getInt("enchant")) + "</font> <br1> " +priceB+ " <br1>" + self.getAugmentSkill(rs.getInt("augment"), rs.getInt("augLvl"), False) + "</td></tr>"); 
   except:
     pass
   if rs != None:    
     rs.close()
   if st != None:   
     st.close()
   text.append("</table><img src=\"sek.cbui176\" width=310 height=1>");
   if con != None:
     con.close()
   return text.toString()
     
 def showSellItems(self,player, page, me, last, itemId, augment, type2, name, ench, black):
   text = TextBuilder("<br><table width=300><tr><td width=7></td><td width=36></td><td width=280>")
   if last == 1:
     text.append("Последние " + str(PAGE_LIMIT) + ":</td></tr>")
   else:
     text.append("Страница " + str(page) + ":</td></tr>")
   limit1 = (page-1) * PAGE_LIMIT
   limit2 = PAGE_LIMIT
  #if page == 1 and black == 0:
  #  limit2 -= PAGE_LIMIT_TOP
   con = None
   rs = None
   st = None
   try:
     con=L2DatabaseFactory.getInstance().getConnection()
     if black == 1:
       st=con.prepareStatement("SELECT id, itemId, total, count, price, money, ownerId, shadow FROM `z_stock_black` WHERE `type` = ? ORDER BY `id` DESC LIMIT ?, ?")
       st.setInt(1, 50)
       st.setInt(2, limit1)
       st.setInt(3, limit2)
     elif type2 >= 0:
       st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow, top FROM `z_stock_items` WHERE `type` = ? ORDER BY `id` DESC LIMIT ?, ?")
       st.setInt(1, type2)
       st.setInt(2, limit1)
       st.setInt(3, limit2)
     elif itemId > 0:
       st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow, top FROM `z_stock_items` WHERE `itemId` = ? ORDER BY `id` DESC LIMIT ?, ?")
       st.setInt(1, itemId)
       st.setInt(2, limit1)
       st.setInt(3, limit2)
     elif augment > 0:
       st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow, top FROM `z_stock_items` WHERE `augment` = ? ORDER BY `id` DESC LIMIT ?, ?")
       st.setInt(1, augment)
       st.setInt(2, limit1)
       st.setInt(3, limit2)
     elif name != "":
       if ench == 0:
         st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow, top FROM `z_stock_items` WHERE `itemName` LIKE ? ORDER BY `id` DESC LIMIT ?, ?")
         st.setString(1, "%"+ name +"%")
         st.setInt(2, limit1)
         st.setInt(3, limit2)
       else:
         st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow, top FROM `z_stock_items` WHERE `itemName` LIKE ? AND `enchant` = ? ORDER BY `id` DESC LIMIT ?, ?")
         st.setString(1, "%"+ name +"%")
         st.setInt(2, ench)
         st.setInt(3, limit1)
         st.setInt(4, limit2)
     elif ench > 0:
       st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow, top FROM `z_stock_items` WHERE `enchant` = ? ORDER BY `id` DESC LIMIT ?, ?")
       st.setInt(1, ench)
       st.setInt(2, limit1)
       st.setInt(3, limit2)
     elif me == 1:
       st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow, top FROM `z_stock_items` WHERE `ownerId` = ? ORDER BY `id` DESC LIMIT ?, ?")
       st.setInt(1, player.getObjectId())
       st.setInt(2, limit1)
       st.setInt(3, limit2)
     else:
       st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow, top FROM `z_stock_items` ORDER BY `id` DESC LIMIT ?, ?")
       st.setInt(1, limit1)
       st.setInt(2, limit2)
     rs=st.executeQuery()
     while (rs.next()):
       sId = rs.getInt("id")
       itmId = rs.getInt("itemId")
       ownerId = rs.getInt("ownerId")
       brokeItem = ItemTable.getInstance().getTemplate(itmId)
       if brokeItem == None:
         continue
       if black == 1:
         priceB = "<font color="+COLOR_PRICE+">" + self.formatNum(rs.getInt("count")) + "/" + self.formatNum(rs.getInt("price")) + " " + self.getMoneyBlackCall(rs.getInt("money")) + "; \"" + self.getSellerName(con,ownerId) + "</font>"
         if player.getObjectId() == ownerId:
           priceB = "<table width=240><tr><td width=160><font color=" + COLOR_PRICE + ">" + self.formatNum(rs.getInt("count")) + "/" + self.formatNum(rs.getInt("price")) + " " + self.getMoneyBlackCall(rs.getInt("money")) + ";</font></td><td align=right><button value=\"X\" action=\"bypass -h Quest purchase StockBuyItem_" + str(sId) + "_ " + str(rs.getInt("total")) + " _ 50\" width=25 height=15 "+BUTTON_DECO+"></td></tr></table><br1>"
         text.append("<tr><td></td><td><img src=\"" + brokeItem.getIcon() + "\" width=32 height=32></td><td><a action=\"bypass -h Quest purchase StockShowItem_" + str(sId) + "_50\"> <font color="+CITEM+">" + self.formatNum(rs.getInt("total")) + " " + brokeItem.getName() + "</font></a> <br1> " +priceB+ "</td></tr>"); 
       else:
         clr_sel = ""
         clr_item = CITEM
         clr_price = COLOR_PRICE
         if rs.getInt("top") >= 2:
           clr_sel = "<img src=\"sek.cbui176\" width=7 height=32>"
           clr_item = CITEM_TOP2
           clr_price = COLOR_PRICE_TOP2
         priceB = "<font color="+clr_price+">" + self.formatNum(rs.getInt("price")) + " " + self.getMoneyCall(rs.getInt("money")) + "; " + self.getSellerName(con,ownerId) + "</font>"
         if player.getObjectId() == ownerId:
           priceB = "<table width=240><tr><td width=160><font color=" + clr_price + ">" + self.formatNum(rs.getInt("price")) + " " + self.getMoneyCall(rs.getInt("money")) + ";</font></td><td align=right><button value=\"X\" action=\"bypass -h Quest purchase StockBuyItem_" + str(sId) + "_ 1 _ 1\" width=25 height=15 "+BUTTON_DECO+"></td></tr></table><br1>"
         text.append("<tr><td>" + clr_sel + "</td><td><img src=\"" + brokeItem.getIcon() + "\" width=32 height=32></td><td><a action=\"bypass -h Quest purchase StockShowItem_" + str(sId) + "_1\"> <font color="+clr_item+">" + brokeItem.getName() + "</font></a><font color="+CENCH+">+" + str(rs.getInt("enchant")) + "</font> <br1> " +priceB+ " <br1>" + self.getAugmentSkill(rs.getInt("augment"), rs.getInt("augLvl"), False) + "</td></tr>"); 
   except:
     pass
   if rs != None:    
     rs.close()
   if st != None:   
     st.close()
   text.append("</table><br>");
   if last == 1:
     text.append("<br>");
   else:        
     pages = int(self.getPageCount(con, me, itemId, augment, type2,player.getObjectId(), name, ench, black))
     if pages >= 2:
       text.append(self.sortPages(page,pages,me,itemId,augment,type2, black, name, ench))
   if con != None:
     con.close()
   return text.toString()

 def sortPages(self,page,pages,me,itemId,augment,type2, black, name, ench):
   text = TextBuilder("<br>Страницы:<br1><table width=300><tr>")
   step = 1
   s = page - 3
   f = page + 3
   if page < SORT_LIMIT and s < SORT_LIMIT:
     s = 1
   if page >= SORT_LIMIT:
     text.append("<td><a action=\"bypass -h Quest purchase StockShowPage_" + str(s) + "_" + str(me) + "_" + str(itemId) + "_" + str(augment) + "_" + str(type2) + "_" + str(black) + "_" + str(name) + "_" + str(ench) + "\"> ... </a></td>") 
   for i in range(s,(pages+1)):
     al = i + 1
     if i == page:
       text.append("<td>" + str(i) + "</td>")
     else:
       if al <= pages:  
         text.append("<td><a action=\"bypass -h Quest purchase StockShowPage_" + str(i) + "_" + str(me) + "_" + str(itemId) + "_" + str(augment) + "_" + str(type2) + "_" + str(black) + "_" + str(name) + "_" + str(ench) + "\">" + str(i) + "</a></td>")
     if step == SORT_LIMIT and f < pages: 
       if al < pages:
         text.append("<td><a action=\"bypass -h Quest purchase StockShowPage_" + str(al) + "_" + str(me) + "_" + str(itemId) + "_" + str(augment) + "_" + str(type2) + "_" + str(black) + "_" + str(name) + "_" + str(ench) + "\"> ... </a></td>")
       break
     step+=1
  #<
   if page == 1 and pages == 2:
     text.append("<td><a action=\"bypass -h Quest purchase StockShowPage_" + str(pages) + "_" + str(me) + "_" + str(itemId) + "_" + str(augment) + "_" + str(type2) + "_" + str(black) + "_" + str(name) + "_" + str(ench) + "\">" + str(pages) + "</a></td>")
  #<   
   text.append("</tr></table><br>")
   htmltext = text.toString()
   text.clear()
   return htmltext
   
 def clearVars(self,player):
   quest = player.getQuestState(qn)
   quest.unset("objId")
   quest.unset("augment")
   quest.unset("enchant")
   return

 def onAdvEvent (self,event,npc,player):
   try:  
     if player.getActiveTradeList() != None:
       player.cancelActiveTrade()
       return self.error("Просмотр шмотки","Ошибка запроса; 0x101")
     if player.isTransactionInProgress() or player.getActiveWarehouse() != None:
       return self.error("Просмотр шмотки","Ошибка запроса; 0x102")
     if player.getPrivateStoreType() != 0:
       return self.error("Просмотр шмотки","Ошибка запроса; 0x103")
     if player.getActiveEnchantItem() != None:
       player.setActiveEnchantItem(None)
       return self.error("Просмотр шмотки","Ошибка запроса; 0x104")
   except:
     return self.error("Просмотр шмотки","Ошибка запроса; 1x101")
  #<  
   quest = player.getQuestState(qn)
   if event == "list":
     htmltext = "<html><body>" + self.getMainMenu() + "<br>"
     htmltext += self.showSellItems(player, 1, 0, 0, 0, 0, -1, "", 0, 0)
     htmltext += "<br></body></html>"
     return htmltext
   elif event.startswith("StockShowPage_"):
     search = str(event.replace("StockShowPage_", ""))
     page,me,itemId,augment,type2,black,name,ench=search.split("_")
     if page == "" or me == "" or augment == "" or itemId == "" or type2 == "" or black == "" or ench == "":
       return self.error("Листание страниц","Ошибка запроса")
     page,me,itemId,augment,type2,black,ench=int(page),int(me),int(itemId),int(augment),int(type2),int(black),int(ench)
     if me == 1:
       stype = "Мои шмотки"
     htmltext = "<html><body>" + self.getMainMenu() + "<br><br1>"
     htmltext += self.showSellItems(player, page, me, 0, itemId, augment, type2, name, ench, black)
     htmltext += "<br></body></html>"
     return htmltext
   elif event.startswith("StockShowItem_"):
     #sellId = event.replace("StockShowItem_", "")
     search = str(event.replace("StockShowItem_", ""))
     sellId,black=search.split("_")
     if sellId == "" or black == "":
       return self.error("Просмотр шмотки","Ошибка запроса")
     try:
       black=int(black)
       sellId=int(sellId)
     except:
       sellId = -30    
     if sellId == -30:
       return self.error("Просмотр шмотки","Ошибка запроса")
     text = TextBuilder("<html><body>")
     try:
       con = L2DatabaseFactory.getInstance().getConnection();
       if black == 50:
         st = con.prepareStatement("SELECT itemId, total, count, price, money, ownerId, shadow FROM `z_stock_black` WHERE `id`=? LIMIT 1");
       else:
         st = con.prepareStatement("SELECT itemId, enchant, augment, augLvl, price, money, ownerId, shadow FROM `z_stock_items` WHERE `id`=? LIMIT 1");
       st.setInt(1, sellId);
       rs = st.executeQuery();
       if rs.next():
         itemId = rs.getInt("itemId");
         brokeItem = ItemTable.getInstance().getTemplate(itemId);
         if brokeItem == None:
           return self.error("Просмотр шмотки","Ошибка запроса")
         money = rs.getInt("money");
         if black == 50:
           enchant = rs.getInt("total");
           augment = rs.getInt("count");
           valute = self.getMoneyBlackCall(money);
         else:
           enchant = rs.getInt("enchant");
           augment = rs.getInt("augment");
           auhLevel = rs.getInt("augLvl");
           valute = self.getMoneyCall(money);
           text.append("<font color=666666>ID шмотки: " + str(itemId) + "</font> <button value=\"Найти еще\" action=\"bypass -h Quest purchase find_ 777 _ " + str(itemId) + " _ Id шмотки\" width=70 height=15 "+BUTTON_DECO+">")
         price = rs.getInt("price");
         charId = rs.getInt("ownerId");
         shadow = rs.getInt("shadow");
        #<
         if black == 50:
           text.append("<table width=300><tr><td><img src=\"" + brokeItem.getIcon() + "\" width=32 height=32></td><td><font color=LEVEL>" + self.formatNum(enchant) + " " + brokeItem.getName() + "</font><br></td></tr></table><br><br>")
         else:
           text.append("<table width=300><tr><td><img src=\"" + brokeItem.getIcon() + "\" width=32 height=32></td><td><font color=LEVEL>" + brokeItem.getName() + " +" + str(enchant) + "</font><br></td></tr></table><br><br>")
         text.append("Продавец: " + self.getSellerName(con,charId) + "<br><br>")
         if black != 50:
           text.append(self.getAugmentSkill(augment, auhLevel, True) + "<br1>")
           if augment > 0:
             text.append("<button value=\"Найти еще\" action=\"bypass -h Quest purchase find_ 777 _ " + str(augment) + " _ Id аугмента\" width=70 height=15 "+BUTTON_DECO+"><br><br>")
         if player.getObjectId() == charId:
           if black == 50:
             #text.append("<font color=33CC00>Курс обмена: <br1>" + self.formatNum(augment)) + " " + self.getMoneyBlackCall(itemId) + " = " + fprice+ " " + str(valute) + "</font><br>");
             text.append("<button value=\"Забрать\" action=\"bypass -h Quest purchase StockBuyItem_" + str(sellId) + "_ " + str(rs.getInt("total")) + " _ 50\" width=60 height=15 "+BUTTON_DECO+"><br>")
           else:
             #text.append("<font color=33CC00>Стоимость: " + fprice+ " " + str(valute) + "</font><br>");
             text.append("<button value=\"Забрать\" action=\"bypass -h Quest purchase StockBuyItem_" + str(sellId) + "_ 1 _ 1\" width=60 height=15 "+BUTTON_DECO+"><br>")
         else:
           payment = player.getInventory().getItemByItemId(money);
           fprice = self.formatNum(price)
           if payment != None and payment.getCount() >= price:
             if black == 50:
               player.sendPacket(ShowCalculator(4393))
               text.append("<font color=33CC00>Курс обмена: <br1>" + self.formatNum(augment) + " " + self.getMoneyBlackCall(itemId) + " = " + fprice+ " " + str(valute) + "</font><br>");
               text.append("Сколько вы хотите купить " + self.getMoneyBlackCall(itemId) + "?<br><edit var=\"count\" width=70 length=\"16\"><br><button value=\"Купить\" action=\"bypass -h Quest purchase StockBuyItem_" + str(sellId) + "_ $count _ 50\" width=60 height=15 "+BUTTON_DECO+"><br>")
             else:
               text.append("<font color=33CC00>Стоимость: " + fprice+ " " + str(valute) + "</font><br>");
               text.append("<button value=\"Купить\" action=\"bypass -h Quest purchase StockBuyItem_" + str(sellId) + "_ 1 _ 1\" width=60 height=15 "+BUTTON_DECO+"><br>")
           else:
             text.append("<font color=CC3333>Стоимость: " + fprice + " " + str(valute) + "</font><br>");
             text.append("<font color=999999>[Купить]</font>");
       else:
         if rs != None:
           rs.close()
         if st != None:
           st.close()
         if con != None:
           con.close()
         return self.error("Просмотр шмотки","Ни найдена или уже купили")
     except:
       pass
     if rs != None:
       rs.close()
     if st != None:
       st.close()
     if con != None:
       con.close()
     if black == 50:
       text.append("<br><br><a action=\"bypass -h Quest purchase black_market\">Вернуться</a><br>");
     else:
       text.append("<br><br><a action=\"bypass -h Quest purchase list\">Вернуться</a><br>");
     text.append("</body></html>");    
     htmltext = text.toString()
     text.clear()
     return htmltext
   elif event.startswith("StockBuyItem_"):
     #sellId = event.replace("StockBuyItem_", "")
     search = str(event.replace("StockBuyItem_", ""))
     sellId,count,black=search.split("_")
     if sellId == "" or count == "" or black == "":
       return self.error("Просмотр шмотки","Ошибка запроса1")
     count = str(count.replace(" ", ""))
     black = str(black.replace(" ", ""))
     try:
       count=int(count)
       black=int(black)
       sellId=int(sellId)
     except:
       sellId = -30    
     if sellId == -30:
       return self.error("Просмотр шмотки","Ошибка запроса2")
     try:
       con = L2DatabaseFactory.getInstance().getConnection();
       if black == 50:
         st = con.prepareStatement("SELECT itemId, itemName, total, count, price, money, ownerId, shadow FROM `z_stock_black` WHERE `id`=? LIMIT 1");
       else:
         st = con.prepareStatement("SELECT itemId, itemName, enchant, augment, augAttr, augLvl, price, money, ownerId, shadow FROM `z_stock_items` WHERE `id`=? LIMIT 1");
       st.setInt(1, sellId);
       rs = st.executeQuery();
       if rs.next():
         itemId = rs.getInt("itemId");
         bItem = ItemTable.getInstance().getTemplate(itemId);
         if bItem == None:
           return self.error("Покупка шмотки","Ошибка запроса3")
         money = rs.getInt("money");
         charId = rs.getInt("ownerId");
         itemName = rs.getString("itemName");
         if black == 50:
           enchant = rs.getInt("total");
           augment = rs.getInt("count");
           auhLevel = 0
           valute = self.getMoneyBlackCall(money);
           #print "" +str(count) + " > " +str(count) + " or " +str(count) + " < " +str(augment)
           if count > enchant or count < augment:
             return self.error("Покупка шмотки","Ошибка запроса4.<br>Количество не может превышать предложение или быть меньше предложения.")
           if count % augment != 0 and player.getObjectId() != charId:
             return self.error("Покупка шмотки","Ошибка запроса5.<br>Количество должно быть кратно предложению.")
           price = rs.getInt("price") * (count / augment);
         else:
           enchant = rs.getInt("enchant");
           augment = rs.getInt("augment");
           augAttr = rs.getInt("augAttr");
           auhLevel = rs.getInt("augLvl");
           valute = self.getMoneyCall(money);
           price = rs.getInt("price");
           if count != 1:
             return self.error("Покупка шмотки","Ошибка запроса5.1")
         shadow = rs.getInt("shadow");
        #<  
         if price <= 0:
           return self.error("Покупка шмотки","Ошибка запроса5.2")
         if player.getObjectId() == charId:
           price = 0
           if black == 50:
             count = enchant    
         if price > 0:
           payment = player.getInventory().getItemByItemId(money);
           if payment != None and payment.getCount() >= price:
             quest.takeItems(money,price)
           else:
             if rs != None:
               rs.close()
             if st != None:
               st.close()
             if con != None:
               con.close()
             return self.error("Покупка шмотки","Проверьте стоимость6")
        #>
         try:
           upd = False  
           if black == 50:
             if count == enchant or enchant - count <= 0:
               zabiraem=con.prepareStatement("DELETE FROM `z_stock_black` WHERE `id`=?")
             else:
               upd = True
               zabiraem=con.prepareStatement("UPDATE `z_stock_black` SET `total` = `total` - ? WHERE `id`=?")
           else:
             zabiraem=con.prepareStatement("DELETE FROM `z_stock_items` WHERE `id`=?")
           if upd:
             zabiraem.setInt(1, count)
             zabiraem.setInt(2, sellId)
           else:
             zabiraem.setInt(1, sellId)
           zabiraem.executeUpdate()
         except:
           if rs != None:
             rs.close()
           if st != None:
             st.close()
           if zabiraem != None:
             zabiraem.close()
           if con != None:
             con.close()
           return self.error("Покупка шмотки","Ошибка запроса7")
         zabiraem.close()
        #> self,con,charId,itemId,enchant,augment,auhLevel,price,money
         try:  
           if price > 0 and not self.transferPay(con,charId,0,0,0,0,0,price,money,player.getObjectId()):
             if rs != None:
               rs.close()
             if st != None:
               st.close()
             if con != None:
               con.close()
             return self.error("Покупка шмотки","Ошибка запроса8")
         except:
           if rs != None:
             rs.close()
           if st != None:
             st.close()
           if con != None:
             con.close()
           return self.error("Покупка шмотки","Ошибка запроса8.1")
        #>
         if black == 50:
           lot = bItem.getName() + ", itemId " + str(itemId) + ", count " + str(count) + " [Price: " + str(money) + " " + str(price) + "]"
           quest.giveItems(itemId, count)
         else:
           lot = bItem.getName() + ", itemId " + str(itemId) + ", enchant " + str(enchant) + ", augment " + str(augment) + ", auhLevel " + str(auhLevel) + "[Price: " + str(money) + " " + str(price) + "]"      
           item = player.getInventory().addItem("zzAuction", itemId, 1, player, player.getTarget())
           if enchant != 0:
             item.setEnchantLevel(enchant)
           if augAttr != 0:
             item.setAugmentation(L2Augmentation(item, augAttr, augment, auhLevel, True))
           smsg = SystemMessage(SystemMessageId.EARNED_S2_S1_S)
           smsg.addItemName(itemId)
           smsg.addNumber(1)
           player.sendPacket(smsg)
           player.sendPacket(ItemList(player, True))
         receiver = "PlayerId: " + str(charId) + ""
         action,payment = "Снято","" 
         if price > 0:
           action,payment = "Куплено","Стоимость: <font color="+CPRICE+">" + self.formatNum(price) + " " + str(valute) + "</font>"
           alarm = L2World.getInstance().getPlayer(charId)
           if alarm:
             receiver = self.getFingerPrints(alarm)
             if black == 50:
               alarm.sendCritMessage("Уведомление с аукциона:" + bItem.getName() + " (+" + str(enchant) + ") купили!")
             else:
               alarm.sendCritMessage("Уведомление с аукциона: +" + self.formatNum(enchant) + " " + bItem.getName() + " купили!")
        #>
         if price > 0:
           if black == 50:
             Log.add(lot + ": " + receiver + " -> " + self.getFingerPrints(player), "item/auction/purchased_black")
           else:
             Log.add(lot + ": " + receiver + " -> " + self.getFingerPrints(player), "item/auction/purchased")
         else:
           if black == 50:
             Log.add(lot + ":" + self.getFingerPrints(player), "item/auction/delete_black")
           else:
             Log.add(lot + ":" + self.getFingerPrints(player), "item/auction/delete")
        #>
         if black == 50:
           htmltext = "<html><body><table><tr><td width=260>Черный рынок</td><td align=right width=70><a action=\"bypass -h Quest purchase black_market\">Назад|<-</a></td></tr></table><br>"
           htmltext += action + "<br1> <font color="+CITEM+">" + self.formatNum(count) + " " + bItem.getName() + "</font><br> " + payment + "</body></html>"
         else:
           htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase list\">Назад|<-</a></td></tr></table><br>"
           htmltext += action + "<br1> <font color="+CITEM+">" + bItem.getName() + "</font><font color="+CENCH+"> +" + str(enchant) + "</font><br> " + self.getAugmentSkill(augment, auhLevel, False) + " <br> " + payment + "</body></html>"
       else:
         if rs != None:
           rs.close()
         if st != None:
           st.close()
         if con != None:
           con.close()
         return self.error("Покупка шмотки","Ни найдена или уже купили")
     except:
       pass
     if rs != None:
       rs.close()
     if st != None:
       st.close()
     if con != None:
       con.close()
     return htmltext
   elif event == "search":
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase list\">Назад|<-</a></td></tr></table><br>"
     htmltext += "<table width=260><tr>"
     step = 0
     for i in sorts:
       htmltext += "<td><button value=\"" + sorts[i] + "\" action=\"bypass -h Quest purchase find_ " + str(i) + " _ no _ no\" width=50 height=21 "+BUTTON_DECO+"></td>"
       step+=1
       if step == 4:
         step = 0
         htmltext += "</tr><tr>"
     htmltext += "</tr></table><br>"
     htmltext += "<table width=220 border=0><tr><td align=right><edit var=\"value\" width=150 length=\"16\"></td><td align=right><combobox width=71 var=keyword list=\"Название;Id шмотки;Id аугмента;Заточка\"></td>"
     htmltext += "<td><button value=\"Поиск\" action=\"bypass -h Quest purchase find_ 777 _ $value _ $keyword\" width=50 height=21 "+BUTTON_DECO+"></td></tr></table>"
     #htmltext += "<br>" + HtmCache.getInstance().getHtm("data/html/auction_speed_dial.htm") + ""
     htmltext += "<br></body></html>"
     return htmltext
   elif event.startswith("find_ "):
     search = str(event.replace("find_ ", ""))
     type2,value,ptype=search.split("_")
     type2,value,ptype=str(type2),str(value),str(ptype)
     value_in = value
     value = str(value.replace(" ", ""))
     type2 = int(type2.replace(" ", "")) 
     if value == "" or ptype == "":
       htmltext = "<html><body>Задан пустой поисковый запрос<br><br><a action=\"bypass -h Quest purchase list\">Вернуться.</a></body></html>"
       return htmltext
     if type2 != 777 and type2 >= 0:
       ptype = "Тип шмотки"
       psort = self.showSellItems(player, 1, 0, 0, 0, 0, type2, "", 0, 0)
       value = sorts.get(type2, "")
       if value == "":
         return self.error("Сортировка","Ошибка запроса")
     else:
       if ptype != " Название":  
         try:
           intval = int(value)
         except:
           value = "qwe" 
         if not value.isdigit():
           return self.error("Сортировка","Ошибка запроса")
       else:
         value = value_in[1:]
         value = value[:-1]
         if not Util.isValidStringAZ(value):
           return self.error("Сортировка","Строка поиска содержит запрещенные символы.")
       if ptype == " Id шмотки":
         psort = self.showSellItems(player, 1, 0, 0, intval, 0, -1, "", 0, 0)
       elif ptype == " Заточка":
         psort = self.showSellItems(player, 1, 0, 0, 0, 0, -1, "", intval, 0)
       elif ptype == " Название":
         ench = 0
         name = value
         if value.count(" ") > 0:
           ench = 0
           name = ""
           try:
             for k in value.split(" "):
               if k.isdigit(): 
                 ench = int(k)
               else:
                 name += " " + k
             name = name[1:]
           except:
             ench = 0
             name = value
         psort = self.showSellItems(player, 1, 0, 0, 0, 0, -1, name, ench, 0)
       else:
         psort = self.showSellItems(player, 1, 0, 0, 0, intval, -1, "", 0, 0)
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase search\">Назад|<-</a></td></tr></table><br>"
     htmltext += "<table><tr><td width=260><font color=336699>Поиск по:</font> <font color=CC66CC>"+ptype+":</font><font color=CC33CC>"+value+"</font></td><td align=right width=70></td></tr></table><br1>"
     htmltext += psort
     htmltext += "<br><a action=\"bypass -h Quest purchase list\">Вернуться</a><br></body></html>"
     return htmltext
   elif event == "add":
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase list\">Назад|<-</a></td></tr></table><br1>"
     htmltext += "<font color=LEVEL>Что хотите выставить на продажу?</font><br1>"
     htmltext += "Оружие:<br1>"
     htmltext += "<table width=240><tr><td><button value=\"Заточенное\" action=\"bypass -h Quest purchase enchanted_0\" width=90 height=15 "+BUTTON_DECO+"></td>"
     htmltext += "<td><button value=\"Аугментированное.\" action=\"bypass -h Quest purchase augment_1\" width=90 height=15 "+BUTTON_DECO+"></td>"
     htmltext += "</tr></table><br>--------<br1>"
     htmltext += "<button value=\"Броня (+)\" action=\"bypass -h Quest purchase enchanted_1\" width=90 height=15 "+BUTTON_DECO+"><br>"
     htmltext += "<button value=\"Бижутерия (+)\" action=\"bypass -h Quest purchase enchanted_2\" width=90 height=15 "+BUTTON_DECO+"><br>"    
     htmltext += "</body></html>"
     return htmltext
   elif event.startswith("enchanted_"):
     chType = int(event.replace("enchanted_", ""))
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase add\">Назад|<-</a></td></tr></table><br1>"
     htmltext += "Шаг 1.<br>Выберите шмотку:<br><br><table width=300>"
     self.clearVars(player)
     if chType > 3: # каг?
       return self.error("Добавление шмоток","Ошибка запроса")
     for item in player.getInventory().getItems():
       itemTemplate = item.getItem()
       itemId = itemTemplate.getItemId()
       itemType = itemTemplate.getType2()
       itemGrade = itemTemplate.getCrystalType()
       preench = item.getEnchantLevel()
       if self.canBeStoredEnch(item) and itemType == chType:
         augment = ""
         if chType == 0:
           augment = "<font color=333366>Нет аугмента</font>"
         if item.isAugmented() and item.getAugmentation().getSkill() != None:
           augment = item.getAugmentation().getSkill()
           augment,level = str(augment.getId()),str(augment.getLevel())
           augment = self.getAugmentSkill(int(augment), int(level), False)
         htmltext += "<tr><td><img src=\"" + itemTemplate.getIcon() + "\" width=32 height=32><br></td><td><a action=\"bypass -h Quest purchase step2_" + str(item.getObjectId()) +"_1\">" + itemTemplate.getName() + "</a> <font color="+CENCH+">+" + str(preench) + "</font><br1>" + augment + "</td></tr>"
     htmltext += "</table><br></body></html>"
     return htmltext
   elif event.startswith("augment_"):
     chType = int(event.replace("augment_", ""))
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase add\">Назад|<-</a></td></tr></table><br1>"
     norefund = ""
     if AUGMENT_TAX > 0:
       norefund = "*Оплата не возвращается!"
       htmltext += "<font color=LEVEL>Стоимость услуги " + str(AUGMENT_TAX) + " Coin Of Luck*</font><br1>"
     htmltext += "Шаг 1.<br>Выберите шмотку:<br><br><table width=300>"
     self.clearVars(player)
     if chType != 1: # каг?
       return self.error("Добавление шмоток","Ошибка запроса")
     for item in player.getInventory().getItems():
       itemTemplate = item.getItem()
       itemId = itemTemplate.getItemId()
       itemType = itemTemplate.getType2()
       itemGrade = itemTemplate.getCrystalType()
       preench = item.getEnchantLevel()
       if self.canBeEnchanted(item) and item.isAugmented() and item.getAugmentation().getSkill() != None and itemType == 0 and itemId not in FORBIDDEN and itemTemplate.getDuration() == -1 and itemGrade > 3 and not item.isEquipped():
         augment = ""
         if chType == 0:
           augment = "<font color=333366>Нет аугмента</font>"
         if item.isAugmented() and item.getAugmentation().getSkill() != None:
           augment = item.getAugmentation().getSkill()
           augment,level = str(augment.getId()),str(augment.getLevel())
           augment = self.getAugmentSkill(int(augment), int(level), False)
         htmltext += "<tr><td><img src=\"" + itemTemplate.getIcon() + "\" width=32 height=32><br></td><td><a action=\"bypass -h Quest purchase step2_" + str(item.getObjectId()) +"_2\">" + itemTemplate.getName() + "</a> <font color="+CENCH+">+" + str(preench) + "</font><br1>" + augment + "</td></tr>"
     htmltext += "</table><br>" + norefund + "</body></html>"
     return htmltext
   elif event.startswith("step2_"):
     search = str(event.replace("step2_", ""))
     itemObjId,chType=search.split("_")
     itemObjId,chType=int(itemObjId),int(chType)
     item = player.getInventory().getItemByObjectId(itemObjId)
     cost = 0
     augcost = 0
     if item and self.canBeEnchanted(item) and not item.isEquipped():
       itemId = item.getItemId()
       itemTemplate = item.getItem() 
       itemType = itemTemplate.getType2()
       preench = item.getEnchantLevel()
       if chType == 1 and preench == 0:
         return self.error("Подробная инфа о шмотке","Ошибка запроса")
       augment = ""
       if chType == 2:
         augment = "<font color=333366>Нет аугмента</font>"
       augSkill = 0  
       if item.isAugmented() and item.getAugmentation().getSkill() != None:
         augcost = AUGMENT_TAX
         augment = item.getAugmentation().getSkill()
        #if chType == 2 and augment == None:
          #return self.error("Подробная инфа о шмотке","Ошибка запроса")
         augment,level = str(augment.getId()),str(augment.getLevel())
         augSkill = int(augment)
         augment = self.getAugmentSkill(int(augment), int(level), True)
       quest.set("augment",str(augSkill))
       quest.set("objId",str(itemObjId))
       quest.set("enchant",str(preench))
       pench = "<font color="+CENCH+">+" + str(preench) + "</font>"
       if chType == 50:
         pench = "" 
       htmltext =  "<html><body>Шаг 2.<br>Параметры:<br>"
       htmltext += "<table width=300><tr><td><img src=\"" + itemTemplate.getIcon() + "\" width=32 height=32></td><td><font color="+CITEM+">" + itemTemplate.getName() + "</font>" + pench + "<br>"+ augment + "</td></tr></table><br><br>"
       if chType == 50:
         htmltext += "Введите количество, сколько всего вы хотите продать, но не более <font color="+CENCH+">" + str(item.getCount()) + "</font> штук:<br1>"
         htmltext += "<edit var=\"total\" width=70 length=\"16\"><br>"
         htmltext += "Введите желаемую цену за определенное количество и выберите валюту:<br>"
       else:
         htmltext += "Введите желаемую цену:<br>"
       mvars = ""
       if chType == 50:
         for i in moneys_black:
           if i == itemId:
             continue    
           mvars+=moneys_black[i]+";"
         htmltext += "<table width=300><tr><td><edit var=\"count\" width=70 length=\"16\"></td><td> штук продаете за </td><td><edit var=\"price\" width=70 length=\"16\"></td></tr>"
         htmltext += "<tr><td></td><td></td><td><combobox width=100 var=type list=\"" + mvars + "\"></td></tr></table><br>"
         htmltext += "<button value=\"Продолжить\" action=\"bypass -h Quest purchase step22_ $price _ $type _ " + str(chType) + " _ $total _ $count\" width=70 height=15 "+BUTTON_DECO+"><br>"
       else:
         mvars = ""
         for i in moneys:
           mvars+=moneys[i]+";"
         htmltext += "<table width=300><tr><td><edit var=\"price\" width=70 length=\"16\"></td><td><combobox width=100 var=type list=\"" + mvars + "\"></td></tr></table><br>"
         htmltext += "<button value=\"Продолжить\" action=\"bypass -h Quest purchase step22_ $price _ $type _ " + str(chType) + " _ 1 _ 1\" width=70 height=15 "+BUTTON_DECO+"><br>"
         if augcost > 0:
           htmltext += "<br><font color=LEVEL>Стоимость услуги " + str(AUGMENT_TAX) + " Coin Of Luck*</font>*Сумма не возвращается!<br>"
         else:
           cost = OTHER_TAX[item.getItem().getCrystalType()]
         if cost > 0:    
           htmltext += "<br><font color=LEVEL>Стоимость услуги " + str(cost) + " Festival Adena*</font>*Сумма не возвращается!<br>"
       htmltext += "<br><a action=\"bypass -h Quest purchase add\">Вернуться.</a></body></html>"
     else :
       htmltext = "<html><body>Ошибка!<br><a action=\"bypass -h Quest purchase step1\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("step22_"):
     search = str(event.replace("step22_ ", ""))
     price,mvar,chType,chCount,chPrice=search.split("_")
     price,mvar,chType,chCount,chPrice=str(price),str(mvar),str(chType),str(chCount),str(chPrice)
     if price == "" or mvar == "" or chType == "" or chCount == "":
       return self.error("Шаг 2.2","Ошибка запроса1")
     price = str(price.replace(" ", ""))
     chType = str(chType.replace(" ", ""))
     chCount = str(chCount.replace(" ", ""))
     chPrice = str(chPrice.replace(" ", ""))
     #top = str(top.replace(" ", ""))
     if not price.isdigit() or not chType.isdigit() or not chCount.isdigit() or not chPrice.isdigit():
       return self.error("Шаг 2.2","Ошибка запроса2")
     try:
       price = int(price)
       chType = int(chType)
       chCount = int(chCount)
       chPrice = int(chPrice)
     except:
       price = 0
       chType = -3
       chCount = 0
       chPrice = 0
     if price == 0 or chPrice == 0 or chCount == 0 or chType == -3:
       return self.error("Шаг 2.2","Ошибка запроса3")
     if chType == 50:
       mvarId = 0
       for i in moneys_black:
         if " "+moneys_black[i] == mvar[:-1]:
           mvarId = i
           break
     else:    
       mvarId = 0
       for i in moneys:
         if " "+moneys[i] == mvar[:-1]:
           mvarId = i
           break
     if mvarId == 0:
       return self.error("Шаг 2.2","Ошибка запроса4" + mvar)
     if mvarId == 57 and price > MAX_ADENA:
       self.clearVars(player)
       return self.error("Шаг 2.2","Максимальная цена " + self.formatNum(MAX_ADENA) + " Adena.")
     myItem = quest.getInt("objId")
     item = player.getInventory().getItemByObjectId(myItem)
     if item and self.canBeEnchanted(item) and not item.isEquipped():
       itemId = item.getItemId()
       itemTemplate = item.getItem() 
       itemType = itemTemplate.getType2()
       preench = item.getEnchantLevel()
       if chType == 1 and preench == 0:
         return self.error("Подробная инфа о шмотке","Ошибка запроса")
       cost = 0
       augtax = False
       augment = ""
       if chType == 2:
         augment = "<font color=333366>Нет аугмента</font>"
       augSkill = 0  
       if item.isAugmented() and item.getAugmentation().getSkill() != None:
         augment = item.getAugmentation().getSkill()
        #if chType == 2 and augment == None:
          #return self.error("Подробная инфа о шмотке","Ошибка запроса")
         augment,level = str(augment.getId()),str(augment.getLevel())
         augSkill = int(augment)
         augment = self.getAugmentSkill(int(augment), int(level), True)
         augtax = True
       quest.set("augment",str(augSkill))
       quest.set("objId",str(myItem))
       quest.set("enchant",str(preench))
       pench = "<font color="+CENCH+">+" + str(preench) + "</font>"
       if chType == 50:
         if chCount % chPrice != 0:
           return self.error("Шаг 2.2","Ошибка запроса4.5<br>Количество должно быть кратно предложению,<br1> тоесть делиться без остатка.")
         pench = "" 
       htmltext =  "<html><body>Шаг 2.2.<br>Подтверждаете?<br>"
       htmltext += "<table width=300><tr><td><img src=\"" + itemTemplate.getIcon() + "\" width=32 height=32></td><td><font color="+CITEM+">" + itemTemplate.getName() + "</font>" + pench + "<br>"+ augment + "</td></tr></table><br><br>"
       if chType == 50:
         htmltext += "Количество, сколько всего вы хотите продать:<br1>"
         htmltext += "<font color="+CENCH+">" + str(chCount) + "</font><br>"
         htmltext += "Желаемая цена за определенное количество и валюта:<br>"
       else:
         htmltext += "Желаемая цена:<br>"
       mvars = ""
       if chType == 50:
         htmltext += "<font color="+COLOR_PRICE2+">" + str(chPrice) + " штук продаете по " + str(price) + " " + mvar[:-1] + "</font><br>"
         htmltext += "<button value=\"Продолжить\" action=\"bypass -h Quest purchase step3_ " + str(price) + " _ " + str(mvarId) + " _ " + str(chType) + " _ " + str(chCount) + " _ " + str(chPrice) + "\" width=70 height=15 "+BUTTON_DECO+"><br>"
       else:
         htmltext += "<table width=210><tr><td width=35><img src=\"" + itemTemplate.getIcon() + "\" width=32 height=32></td><td align=left> <font color="+COLOR_PRICE2+">" + str(price) + " " + mvar[:-1] + "</font></td></tr></table><br>"
         #htmltext += "<button value=\"Продолжить\" action=\"bypass -h Quest purchase step3_ 1 _ 1 _ 1 _ 1 _ 1 _ " + top + "\" width=70 height=15 "+BUTTON_DECO+"><br>"
         htmltext += "<button value=\"Продолжить\" action=\"bypass -h Quest purchase step3_ " + str(price) + " _ " + str(mvarId) + " _ " + str(chType) + " _ 1 _ 1\" width=70 height=15 "+BUTTON_DECO+"><br>"
         #htmltext += "<button value=\"Продолжить\" action=\"bypass -h Quest purchase step3_ " + str(price) + " _ " + mvar + " _ " + str(chType) + " _ 1 _ 1 _ " + top + "\" width=70 height=15 "+BUTTON_DECO+"><br>"
         if augtax and AUGMENT_TAX > 0:    
           htmltext += "<br><font color=LEVEL>Стоимость услуги " + str(AUGMENT_TAX) + " Coin Of Luck*</font>*Сумма не возвращается!<br>"
         else:
           cost = OTHER_TAX[item.getItem().getCrystalType()]
         if cost > 0:    
           htmltext += "<br><font color=LEVEL>Стоимость услуги " + str(cost) + " Festival Adena*</font>*Сумма не возвращается!<br>"
       htmltext += "<br><font color=LEVEL>Если все данные верны, то нажмите кнопку \"Продолжить\"</font>.<br>"
       htmltext += "<br><a action=\"bypass -h Quest purchase add\">Вернуться.</a></body></html>"
     else :
       htmltext = "<html><body>Ошибка!<br><a action=\"bypass -h Quest purchase step1\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("step3_ "):
     search = str(event.replace("step3_ ", ""))
     price,mvar,chType,chCount,chPrice=search.split("_")
     price,mvar,chType,chCount,chPrice=str(price),str(mvar),str(chType),str(chCount),str(chPrice)
     if price == "" or mvar == "" or chType == "" or chCount == "":
       self.clearVars(player)
       return self.error("Шаг 3","Ошибка запроса1")
     price = str(price.replace(" ", ""))
     chType = str(chType.replace(" ", ""))
     chCount = str(chCount.replace(" ", ""))
     chPrice = str(chPrice.replace(" ", ""))
     if not price.isdigit() or not chType.isdigit() or not chCount.isdigit() or not chPrice.isdigit():
       self.clearVars(player)
       return self.error("Шаг 3","Ошибка запроса2")
     try:
       price = int(price)
       chType = int(chType)
       chCount = int(chCount)
       chPrice = int(chPrice)
     except:
       price = 0
       chType = -3
       chCount = 0
       chPrice = 0
     if price == 0 or chPrice == 0 or chCount == 0 or chType == -3:
       self.clearVars(player)
       return self.error("Шаг 3","Ошибка запроса3")
     mvarId = 0
     valute = mvar.replace(" ", "")
     if chType == 50:
      #for i in moneys_black:
      #  if " "+moneys_black[i] == mvar[:-1] or moneys_black[i].replace(" ", "") == valute:
      #    mvarId = i
      #    break
       try:
         mvarId = int(mvar)
       except:
         mvarId = 0
     else:
       try:
         mvarId = int(mvar)
       except:
         mvarId = 0
     if mvarId == 0:
       self.clearVars(player)
       return self.error("Шаг 3","Ошибка запроса4" + str(mvar))
     if mvarId == 57 and price > MAX_ADENA:
       self.clearVars(player)
       return self.error("Шаг 3","Максимальная цена " + self.formatNum(MAX_ADENA) + " Adena.")
     myItem = quest.getInt("objId")
     item = player.getInventory().getItemByObjectId(myItem)
     if item:
       if item.isEquipped():
         return self.error("Шаг 3","Ошибка запроса5")
       if not self.canBeEnchanted(item):
         return self.error("Шаг 3","Ошибка запроса6")
       if item.getCount() < chCount:
         return self.error("Шаг 3","Ошибка запроса6.1")
       if chPrice > chCount:
         return self.error("Шаг 3","Ошибка запроса6.2")
       myEnch = quest.getInt("enchant")
       myAugm = quest.getInt("augment")
       itemId = item.getItemId()
       itemTemplate = item.getItem() 
       itemType = itemTemplate.getType2() 
       preench = item.getEnchantLevel()
       if preench != myEnch:
         self.clearVars(player)
         return self.error("Шаг 3","Ошибка запроса7")
       augment = "нет." 
       augEffId = 0
       augSkId = 0
       augSkLvl = 0
       cost = 0
       augtax = False
       if item.isAugmented() and item.getAugmentation().getSkill() != None:
         augment = item.getAugmentation().getSkill()
         augEffId = item.getAugmentation().getAugmentationId()
         augSkId,augSkLvl = str(augment.getId()),str(augment.getLevel())
         if myAugm != int(augSkId):
           self.clearVars(player)
           return self.error("Шаг 3","Ошибка запроса8")
        #<payment
         if quest.getQuestItemsCount(4037) < AUGMENT_TAX:
           return self.error("Шаг 3","Ошибка запроса9<br> Стоимость услуги: " + str(AUGMENT_TAX) + " Coin Of Luck.")
         augtax = True
       else:
         try:  
           cost = OTHER_TAX[item.getItem().getCrystalType()]
         except:
           cost = 0  
       if cost > 0 and quest.getQuestItemsCount(6673) < cost:
         return self.error("Шаг 3","Ошибка запроса9<br> Стоимость услуги: " + str(cost) + " Festival Adena.")
        #<
       if chType == 50:
         itemType = 50
      #<
       try:  
         con=L2DatabaseFactory.getInstance().getConnection()
         if chType == 50:
           storeitem=con.prepareStatement("INSERT INTO `z_stock_black` (`id`,`itemId`,`itemName`,`total`,`count`,`price`,`money`,`type`,`ownerId`,`shadow`) VALUES (NULL,?,?,?,?,?,?,?,?,?);")
           storeitem.setInt(1,itemId)
           storeitem.setString(2, itemTemplate.getName())
           storeitem.setInt(3, chCount)
           storeitem.setInt(4, chPrice)
           storeitem.setInt(5, price)
           storeitem.setInt(6, mvarId)
           storeitem.setInt(7, itemType)
           storeitem.setInt(8, player.getObjectId())
           storeitem.setInt(9, 0)
           storeitem.executeUpdate()
         else:
           storeitem=con.prepareStatement("INSERT INTO `z_stock_items` (`id`,`itemId`,`itemName`,`enchant`,`augment`,`augAttr`,`augLvl`,`price`,`money`,`type`,`ownerId`,`shadow`,`top`) VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?,?);")
           storeitem.setInt(1,itemId)
           storeitem.setString(2, itemTemplate.getName())
           storeitem.setInt(3, preench)
           storeitem.setInt(4, int(augSkId))
           storeitem.setInt(5, int(augEffId))
           storeitem.setInt(6, int(augSkLvl))
           storeitem.setInt(7, price)
           storeitem.setInt(8, mvarId)
           storeitem.setInt(9, itemType)
           storeitem.setInt(10, player.getObjectId())
           storeitem.setInt(11, 0)
           storeitem.setInt(12, 0)
           storeitem.executeUpdate()
       except:
         if storeitem != None:
           storeitem.close()
         if con != None:
           con.close()
         self.clearVars(player)
         return self.error("Шаг 3","Ошибка базы данных")
       if storeitem != None:
         storeitem.close()
       if con != None:
         con.close()
       if augtax and AUGMENT_TAX > 0:
         quest.takeItems(4037, AUGMENT_TAX)
       if cost > 0:
         quest.takeItems(6673, cost)
       if chType == 50:
         quest.takeItems(itemId, chCount)
       else:  
         player.destroyItem("zzAuction", myItem, chCount, player, 1)
      #<
       self.clearVars(player)
       if chType == 50:
         lot = itemTemplate.getName() + ", itemId " + str(itemId) + ", total " + str(chCount) + ", count " + str(chPrice) + ", [Price: " + str(mvarId) + " " + str(price) + "]"
         Log.add(lot + ": " + self.getFingerPrints(player), "item/auction/add_black")
         htmltext = "<html><body><table><tr><td width=260>Черный рынок</td><td align=right width=70><a action=\"bypass -h Quest purchase black_market\">Назад|<-</a></td></tr></table><br1>"
         htmltext += "<font color="+CITEM+">Вы выставили: " + self.formatNum(chCount) + " " + itemTemplate.getName() + "</font><br1>"
         htmltext += "по <font color="+COLOR_PRICE+">" + self.formatNum(chPrice) + " штук за " + self.formatNum(price) + " " + self.getMoneyBlackCall(mvarId) + ".<br></body></html>"
       else:
         lot = itemTemplate.getName() + ", itemId " + str(itemId) + ", enchant " + str(preench) + ", augment " + str(augSkId) + ", auhLevel " + str(augSkLvl) + ", [Price: " + str(mvarId) + " " + str(price) + "]"
         Log.add(lot + ": " + self.getFingerPrints(player), "item/auction/add")
         htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase add\">Назад|<-</a></td></tr></table><br1>"
         htmltext += "<font color="+CITEM+">" + itemTemplate.getName() + "</font><font color="+CENCH+">+" + str(preench) + "</font><br1> " + self.getAugmentSkill(int(augSkId), int(augSkLvl), False) + " <br1>выставлена на продажу!<br><br>"
         htmltext += "Дополнительные параметры:<br>Скрыть ник: <font color="+COLOR_PRICE2+">нет</font></body></html>"
     else:
       self.clearVars(player)
       return self.error("Шаг 3","Ошибка запроса99")
     return htmltext
   elif event == "office":
    #data = str(strftime("%Y-%m-%d", gmtime()))
    #time = str(strftime("%H:%M:%S", gmtime()))
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase list\">Назад|<-</a></td></tr></table><br1>"
     htmltext += "Привет, " + player.getName() + ".<br>"
    #htmltext += "Привет, " + player.getName() + ".<br>Сейчас " + data + " " + time + "<br>"
     htmltext += "<button value=\"Мои шмотки\" action=\"bypass -h Quest purchase StockShowPage_1_1_0_0_-1_0__0\" width=70 height=15 "+BUTTON_DECO+"><br>"
     htmltext += "</body></html>"
     return htmltext
   elif event == "office_black":
    #data = str(strftime("%Y-%m-%d", gmtime()))
    #time = str(strftime("%H:%M:%S", gmtime()))
     htmltext = "<html><body><table><tr><td width=260>Черный рынок</td><td align=right width=70><a action=\"bypass -h Quest purchase black_market\">Назад|<-</a></td></tr></table><br1>"
     htmltext += "Привет, " + player.getName() + ".<br>"
    #htmltext += "Привет, " + player.getName() + ".<br>Сейчас " + data + " " + time + "<br>"
     htmltext += "<button value=\"Мои шмотки\" action=\"bypass -h Quest purchase StockShowPage_1_1_0_0_-1_1__0\" width=70 height=15 "+BUTTON_DECO+"><br>"
     htmltext += "</body></html>"
     return htmltext
   elif event == "black_market":
     htmltext = "<html><body><table width=280><tr><td>Черный рынок</td><td align=right><button value=\"Офис\" action=\"bypass -h Quest purchase office_black\" width=50 height=17 "+BUTTON_DECO+"></td><td align=right><button value=\"Добавить\" action=\"bypass -h Quest purchase add_black\" width=70 height=17 "+BUTTON_DECO+"></td><td align=right><button value=\"Аукцион\" action=\"bypass -h Quest purchase list\" width=50 height=17 "+BUTTON_DECO+"></td></tr></table><br>"
     htmltext += self.showSellItems(player, 1, 0, 0, 0, 0, -1, "", 0, 1)
     htmltext += "<br></body></html>"
     return htmltext
   elif event.startswith("add_black"):
     htmltext = "<html><body><table><tr><td width=260>Черный рынок</td><td align=right width=70><a action=\"bypass -h Quest purchase black_market\">Назад|<-</a></td></tr></table><br1>"
     htmltext += "Шаг 1.<br>Выберите товар:<br><br><table width=300>"
     self.clearVars(player)
     for item in player.getInventory().getItems():
       if not item.getItemId() in moneys_black.keys():#self.getMoneyBlackCall(item.getItemId()) == "none":
         continue
       htmltext += "<tr><td><img src=\"" + item.getItem().getIcon() + "\" width=32 height=32><br></td><td><a action=\"bypass -h Quest purchase step2_" + str(item.getObjectId()) +"_50\">" + item.getItem().getName() + " (" + self.formatNum(item.getCount()) + ")</a></td></tr>"
     htmltext += "</table><br></body></html>"
     return htmltext
   else:
     self.clearVars(player)
     return self.error("404","Ошибка запроса56")
   return

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   if npc.getNpcId() == MARKET:
     htmltext = "<html><body>" + self.getMainMenu() + "<br>"
     #htmltext += self.getTopItems(player)
     htmltext += self.showSellItems(player, 1, 0, 0, 0, 0, -1, "", 0, 0)
     htmltext += "<br></body></html>"
     return htmltext
   return 

QUEST       = Quest(-1,qn,"custom")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(MARKET)
QUEST.addTalkId(MARKET)
