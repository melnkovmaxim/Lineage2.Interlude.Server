# -*- coding: utf-8 -*-
import sys
from javolution.text import TextBuilder
from ru.agecold import L2DatabaseFactory
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
from ru.agecold.gameserver.network.serverpackets import SystemMessage
from ru.agecold.gameserver.templates import L2Item
from ru.agecold.util import Rnd
from ru.agecold.gameserver.util import Util
from time import gmtime, strftime

qn = "purchase"

MARKET = 65011
PAGE_LIMIT = 10
SORT_LIMIT = 8

MAX_ADENA = 2147000000

FORBIDDEN = [6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621]

#
CENCH = "CCCC33"
CPRICE = "669966"
CITEM = "993366"
CAUG1 = "333366"
CAUG2 = "006699"

moneys = {9820: "Farm Coin", 9827: "Монета Удачи", 57: "Adena"}
sorts = {0: "Оружие", 1: "Броня", 2: "Бижутерия"}

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
 
 def getIcon(self,con,iid):
   icon = "Icon.NOIMAGE"
   ext = False
   try:
     if con == None:
       ext = True
       con=L2DatabaseFactory.getInstance().getConnection()
     st = con.prepareStatement("SELECT itemIcon FROM `z_market_icons` WHERE `itemId`=? LIMIT 1");
     st.setInt(1, iid);
     rs = st.executeQuery();
     if rs.next():
       icon = rs.getString("itemIcon")
   except:
     pass
   finally:
     self.closeRes(rs)
     self.closeStat(st)
     if ext:
       self.closeCon(con)
   return icon

 def getMoneyCall(self,money):
   return moneys.get(money, str("Adena"))
    
 def getSellerName(self,con,charId):
   name = "???"
   try:
     st = con.prepareStatement("SELECT char_name FROM `characters` WHERE `Obj_Id`=? LIMIT 1");
     st.setInt(1, charId);
     rs = st.executeQuery();
     if rs.next():
       name = rs.getString("char_name")
   except:
     pass
   finally:
     self.closeRes(rs)
     self.closeStat(st)
   return name
   
 def getAugmentSkill(self, aid, lvl):
   augment = SkillTable.getInstance().getInfo(aid, 1)
   if augment == None:
     return "" 
   augName = augment.getName();
   stype = "Шанс";
   if augment.isActive():
     stype = "Актив"
   elif augment.isPassive():
     stype = "Пассив"
   augName = augName.replace("Item Skill: ", "");  
   return "<font color="+CAUG1+">Аугмент:</font> <font color="+CAUG2+">" + augName + " (" + stype + ":" + str(lvl) + "lvl)</font>";

 def getPageCount(self, con, me, itemId, augment, type2, charId):
   rowCount = 0
   pages = 0
   try:
     con=L2DatabaseFactory.getInstance().getConnection()
     if type2 >= 0:
       st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_items` WHERE `type` = ?")
       st.setInt(1, type2)
     elif itemId > 0:
       st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_items` WHERE `itemId` = ?")
       st.setInt(1, itemId)
     elif augment > 0:
       st=con.prepareStatement("SELECT COUNT(`id`) FROM `z_stock_items` WHERE `augment` = ?")
       st.setInt(1, augment)
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
   finally:
     self.closeRes(rs)
     self.closeStat(st)
   if rowCount == 0:
     return "0"
   pages = (rowCount / PAGE_LIMIT) + 1
   return str(pages)

 def error(self,action, text):
   return "<html><body> " + action + ": <br> " + text + "</body></html>"

 def transferPay(self,con,charId,itemId,enchant,augment,auhLevel,price,money):
   item = ItemTable.getInstance().getTemplate(itemId)
   if item == None:
     return False
   data = str(strftime("%Y-%m-%d", gmtime()))
   time = str(strftime("%H:%M:%S", gmtime()))
   text = TextBuilder()
   text.append("Итем: <font color=FF3399>" + item.getName() + " +" + str(enchant) + " " + self.getAugmentSkill(augment, auhLevel) +"</font><br1> был успешно продан.<br1>")
   text.append("Благодарим за сотрудничество.")
   try:
     sName = self.getSellerName(con,charId)
     st = con.prepareStatement("INSERT INTO `z_post_pos` (`id`,`tema`,`text`,`from`,`to`,`type`,`date`,`time`,`itemName`,`itemId`,`itemCount`,`itemEnch`,`augData`,`augSkill`,`augLvl`) VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
     st.setString(1, "Шмотка продана")
     st.setString(2, text.toString())
     st.setString(3, "~zzAuction#.")
     st.setString(4, sName)
     st.setInt(5, 0)
     st.setString(6, data)
     st.setString(7, time)
     st.setString(8, item.getName())
     st.setInt(9, money)
     st.setInt(10, price)
     st.setInt(11, 0)
     st.setInt(12, 0)
     st.setInt(13, 0)
     st.setInt(14, 0)
     st.execute()
     alarm = L2World.getInstance().getPlayer(sName)
     if alarm:
       alarm.sendMessage("Уведомление с аукциона: проверь почту")
       alarm.sendPacket(ExMailArrived())
     return True
   except: 
     return False
   finally:
     text.clear()  
     self.closeStat(st)
   return False
     
 def showSellItems(self,player, page, me, last, itemId, augment, type2):
   text = TextBuilder("<br><table width=300><tr><td width=36></td><td width=264>")
   if last == 1:
     text.append("Последние " + str(PAGE_LIMIT) + ":</td></tr>")
   else:
     text.append("Страница " + str(page) + ":</td></tr>")
   limit1 = (page-1) * PAGE_LIMIT
   limit2 = PAGE_LIMIT
   con = None
   try:
     con=L2DatabaseFactory.getInstance().getConnection()
     if type2 >= 0:  
       st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow FROM `z_stock_items` WHERE `type` = ? ORDER BY `id` DESC LIMIT ?, ?")
       st.setInt(1, type2)
       st.setInt(2, limit1)
       st.setInt(3, limit2)
     elif itemId > 0:
       st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow FROM `z_stock_items` WHERE `itemId` = ? ORDER BY `id` DESC LIMIT ?, ?")
       st.setInt(1, itemId)
       st.setInt(2, limit1)
       st.setInt(3, limit2)
     elif augment > 0:
       st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow FROM `z_stock_items` WHERE `augment` = ? ORDER BY `id` DESC LIMIT ?, ?")
       st.setInt(1, augment)
       st.setInt(2, limit1)
       st.setInt(3, limit2)
     elif me == 1:
       st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow FROM `z_stock_items` WHERE `ownerId` = ? ORDER BY `id` DESC LIMIT ?, ?")
       st.setInt(1, player.getObjectId())
       st.setInt(2, limit1)
       st.setInt(3, limit2)
     else:
       st=con.prepareStatement("SELECT id, itemId, enchant, augment, augLvl, price, money, ownerId, shadow FROM `z_stock_items` ORDER BY `id` DESC LIMIT ?, ?")
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
       priceB = "<font color="+CPRICE+">" + str(Util.formatAdena(rs.getInt("price"))) + " " + self.getMoneyCall(rs.getInt("money")) + "; \"" + self.getSellerName(con,ownerId) + "</font>"
       if player.getObjectId() == ownerId:
         priceB = "<table width=240><tr><td width=160><font color=666699>" + str(Util.formatAdena(rs.getInt("price"))) + " " + self.getMoneyCall(rs.getInt("money")) + ";</font></td><td align=right><button value=\"X\" action=\"bypass -h Quest purchase StockBuyItem_" + str(sId) + "\" width=25 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br1>"
       text.append("<tr><td><img src=\"" + brokeItem.getIcon() + "\" width=32 height=32></td><td><a action=\"bypass -h Quest purchase StockShowItem_" + str(sId) + "\"> <font color="+CITEM+">" + brokeItem.getName() + "</font></a><font color="+CENCH+">+" + str(rs.getInt("enchant")) + "</font> <br1> " +priceB+ " <br1>" + self.getAugmentSkill(rs.getInt("augment"), rs.getInt("augLvl")) + "</td></tr>"); 
   except: 
     pass
   finally:
     self.closeRes(rs)
     self.closeStat(st)
   text.append("</table><br>");
   if last == 1:
     text.append("<br>");
   else:        
     pages = int(self.getPageCount(con, me, itemId, augment, type2,player.getObjectId()))
     if pages >= 2:
       text.append(self.sortPages(page,pages,me,itemId,augment,type2))
   self.closeCon(con)
   htmltext = text.toString()
   text.clear()
   return htmltext

 def sortPages(self,page,pages,me,itemId,augment,type2):
   text = TextBuilder("<br>Страницы:<br1><table width=300><tr>")
   step = 1
   s = page - 3
   f = page + 3
   if page < SORT_LIMIT and s < SORT_LIMIT:
     s = 1
   if page >= SORT_LIMIT:
     text.append("<td><a action=\"bypass -h Quest purchase StockShowPage_" + str(s) + "_" + str(me) + "_" + str(itemId) + "_" + str(augment) + "_" + str(type2) + "\"> ... </a></td>") 
   for i in range(s,(pages+1)):
     al = i + 1
     if i == page:
       text.append("<td>" + str(i) + "</td>")
     else:
       if al <= pages:  
         text.append("<td><a action=\"bypass -h Quest purchase StockShowPage_" + str(i) + "_" + str(me) + "_" + str(itemId) + "_" + str(augment) + "_" + str(type2) + "\">" + str(i) + "</a></td>")
     if step == SORT_LIMIT and f < pages: 
       if al < pages:
         text.append("<td><a action=\"bypass -h Quest purchase StockShowPage_" + str(al) + "_" + str(me) + "_" + str(itemId) + "_" + str(augment) + "_" + str(type2) + "\"> ... </a></td>")
       break
     step+=1
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
   quest = player.getQuestState(qn)
   if event == "list":
     htmltext = "<html><body><table width=280><tr><td>Аукцион</td><td align=right><button value=\"Офис\" action=\"bypass -h Quest purchase office\" width=70 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td align=right><button value=\"Добавить\" action=\"bypass -h Quest purchase add\" width=70 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td align=right><button value=\"Поиск\" action=\"bypass -h Quest purchase search\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br>"
     htmltext += self.showSellItems(player, 1, 0, 0, 0, 0, -1)
     htmltext += "<br></body></html>"
     return htmltext
   elif event.startswith("StockShowPage_"):
     search = str(event.replace("StockShowPage_", ""))
     page,me,itemId,augment,type2=search.split("_")
     if page == "" or me == "" or augment == "" or itemId == "" or type2 == "":
       return self.error("Листание страниц","Ошибка запроса")
     page,me,itemId,augment,type2=int(page),int(me),int(itemId),int(augment),int(type2)
     if me == 1:
       stype = "Мои шмотки"
     htmltext = "<html><body><table width=280><tr><td>Аукцион</td><td align=right><button value=\"Офис\" action=\"bypass -h Quest purchase office\" width=70 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td align=right><button value=\"Добавить\" action=\"bypass -h Quest purchase add\" width=70 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td align=right><button value=\"Поиск\" action=\"bypass -h Quest purchase search\" width=40 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br><br1>"
     htmltext += self.showSellItems(player, page, me, 0, itemId, augment, type2)
     htmltext += "<br></body></html>"
     return htmltext
   elif event.startswith("StockShowItem_"):
     sellId = event.replace("StockShowItem_", "")
     if sellId == "":
       return self.error("Просмотр шмотки","Ошибка запроса")
     sellId=int(sellId)
     text = TextBuilder("<html><body>")
     try:
       con = L2DatabaseFactory.getInstance().getConnection();
       st = con.prepareStatement("SELECT itemId, enchant, augment, augLvl, price, money, ownerId, shadow FROM `z_stock_items` WHERE `id`=? LIMIT 1");
       st.setInt(1, sellId);
       rs = st.executeQuery();
       if rs.next():
         itemId = rs.getInt("itemId");
         brokeItem = ItemTable.getInstance().getTemplate(itemId);
         if brokeItem == None:
           return self.error("Просмотр шмотки","Ошибка запроса")
         text.append("<font color=666666>ID шмотки: " + str(itemId) + "</font> <button value=\"Найти еще\" action=\"bypass -h Quest purchase find_ 777 _ " + str(itemId) + " _ Id шмотки\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">")
         enchant = rs.getInt("enchant");
         augment = rs.getInt("augment");
         auhLevel = rs.getInt("augLvl");
         price = rs.getInt("price");
         money = rs.getInt("money");
         charId = rs.getInt("ownerId");
         shadow = rs.getInt("shadow");
         valute = self.getMoneyCall(money);
        #<
         text.append("<table width=300><tr><td><img src=\"" + brokeItem.getIcon() + "\" width=32 height=32></td><td><font color=LEVEL>" + brokeItem.getName() + " +" + str(enchant) + "</font><br></td></tr></table><br><br>")
         text.append("Продавец: " + self.getSellerName(con,charId) + "<br><br>")
         text.append(self.getAugmentSkill(augment, auhLevel) + "<br1>")
         if augment > 0:
           text.append("<button value=\"Найти еще\" action=\"bypass -h Quest purchase find_ 777 _ " + str(augment) + " _ Id аугмента\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br><br>")
         if player.getObjectId() == charId:
           text.append("<button value=\"Забрать\" action=\"bypass -h Quest purchase StockBuyItem_" + str(sellId) + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>")
         else:
           payment = player.getInventory().getItemByItemId(money);
           fprice = str(Util.formatAdena(price))
           if payment != None and payment.getCount() >= price:
             text.append("<font color=33CC00>Стоимость: " + fprice+ " " + str(valute) + "</font><br>");
             text.append("<button value=\"Купить\" action=\"bypass -h Quest purchase StockBuyItem_" + str(sellId) + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>")
           else:
             text.append("<font color=CC3333>Стоимость: " + fprice + " " + str(valute) + "</font><br>");
             text.append("<font color=999999>[Купить]</font>");
       else:
         return self.error("Просмотр шмотки","Ни найдена или уже купили")
     except:
       pass
     finally:
       self.closeRes(rs)
       self.closeStat(st)
       self.closeCon(con)
     text.append("<br><br><a action=\"bypass -h Quest purchase list\">Вернуться</a><br>");
     text.append("</body></html>");    
     htmltext = text.toString()
     text.clear()
     return htmltext
   elif event.startswith("StockBuyItem_"):
     sellId = event.replace("StockBuyItem_", "")
     if sellId == "":
       return self.error("Покупка шмотки","Ошибка запроса")
     sellId=int(sellId)
     try:
       con = L2DatabaseFactory.getInstance().getConnection();
       st = con.prepareStatement("SELECT itemId, itemName, enchant, augment, augAttr, augLvl, price, money, ownerId, shadow FROM `z_stock_items` WHERE `id`=? LIMIT 1");
       st.setInt(1, sellId);
       rs = st.executeQuery();
       if rs.next():
         itemId = rs.getInt("itemId");
         bItem = ItemTable.getInstance().getTemplate(itemId);
         if bItem == None:
           return self.error("Покупка шмотки","Ошибка запроса")
         itemName = rs.getString("itemName");
         enchant = rs.getInt("enchant");
         augment = rs.getInt("augment");
         augAttr = rs.getInt("augAttr");
         auhLevel = rs.getInt("augLvl");
         price = rs.getInt("price");
         money = rs.getInt("money");
         charId = rs.getInt("ownerId");
         shadow = rs.getInt("shadow");
         valute = self.getMoneyCall(money);
        #<  
         if player.getObjectId() == charId:
           price = 0
         if price > 0:
           payment = player.getInventory().getItemByItemId(money);
           if payment != None and payment.getCount() >= price:
             quest.takeItems(money,price)
           else:
             return self.error("Покупка шмотки","Проверьте стоимость")
        #>
         try:
           zabiraem=con.prepareStatement("DELETE FROM `z_stock_items` WHERE `id`=?")
           zabiraem.setInt(1, sellId)
           zabiraem.executeUpdate()
         except:
           return self.error("Покупка шмотки","Ошибка запроса")
         finally:
           self.closeStat(zabiraem)
        #>
         if price > 0 and not self.transferPay(con,charId,itemId,enchant,augment,auhLevel,price,money):
           return self.error("Покупка шмотки","Ошибка запроса")
        #> 
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
         action,payment = "Снято","" 
         if price > 0:
           action,payment = "Куплено","Стоимость: <font color="+CPRICE+">" + str(Util.formatAdena(price)) + " " + str(valute) + "</font>"
           alarm = L2World.getInstance().getPlayer(self.getSellerName(con,charId))
           if alarm:
             alarm.sendMessage("Уведомление с аукциона:" + bItem.getName() + " (+" + str(enchant) + ") купили!")
         htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase list\">Назад|<-</a></td></tr></table><br>"
         htmltext += action + "<br1> <font color="+CITEM+">" + bItem.getName() + "</font><font color="+CENCH+"> +" + str(enchant) + "</font><br> " + self.getAugmentSkill(augment, auhLevel) + " <br> " + payment + "</body></html>"
       else:
         return self.error("Покупка шмотки","Ни найдена или уже купили")
     except:
       pass
     finally:
       self.closeRes(rs)
       self.closeStat(st)
       self.closeCon(con)
     return htmltext
   elif event == "search":
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase list\">Назад|<-</a></td></tr></table><br>"
     htmltext += "<table width=260><tr>"
     step = 0
     for i in sorts:
       htmltext += "<td><button value=\"" + sorts[i] + "\" action=\"bypass -h Quest purchase find_ " + str(i) + " _ no _ no\" width=50 height=21 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>"
       step+=1
       if step == 4:
         step = 0
         htmltext += "</tr><tr>"
     htmltext += "</tr></table><br><table width=220 border=0><tr><td align=right><edit var=\"value\" width=150 length=\"16\"></td><td align=right><combobox width=71 var=keyword list=\"Id шмотки;Id аугмента\"></td><td><button value=\"Поиск\" action=\"bypass -h Quest purchase find_ 777 _ $value _ $keyword\" width=50 height=21 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>"
     htmltext += "<br></body></html>"
     return htmltext
   elif event.startswith("find_ "):
     search = str(event.replace("find_ ", ""))
     type2,value,ptype=search.split("_")
     type2,value,ptype=str(type2),str(value),str(ptype)
     value = str(value.replace(" ", ""))
     type2 = int(type2.replace(" ", ""))
     if value == "" or ptype == "":
       htmltext = "<html><body>Задан пустой поисковый запрос<br><br><a action=\"bypass -h Quest purchase list\">Вернуться.</a></body></html>"
       return htmltext
     if type2 != 777 and type2 >= 0:
       ptype = "Тип шмотки"
       psort = self.showSellItems(player, 1, 0, 0, 0, 0, type2)
       value = sorts.get(type2, "")
       if value == "":
         return self.error("Сортировка","Ошибка запроса")
     else:
       if not value.isdigit():
         return self.error("Сортировка","Ошибка запроса")
       if ptype == " Id шмотки":
         psort = self.showSellItems(player, 1, 0, 0, int(value), 0, -1)
       else:
         psort = self.showSellItems(player, 1, 0, 0, 0, int(value), -1)
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase search\">Назад|<-</a></td></tr></table><br>"
     htmltext += "<table><tr><td width=260><font color=336699>Поиск по:</font> <font color=CC66CC>"+ptype+":</font><font color=CC33CC>"+value+"</font></td><td align=right width=70></td></tr></table><br1>"
     htmltext += psort
     htmltext += "<br><a action=\"bypass -h Quest purchase list\">Вернуться</a><br></body></html>"
     return htmltext
   elif event == "add":
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase list\">Назад|<-</a></td></tr></table><br1>"
     htmltext += "<font color=LEVEL>Что хотите выставить на продажу?</font><br1>"
     htmltext += "Оружие:<br1>"
     htmltext += "<table width=240><tr><td><button value=\"Заточенное\" action=\"bypass -h Quest purchase enchanted_0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>"
     htmltext += "<td><button value=\"Аугментированное.\" action=\"bypass -h Quest purchase augment_1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>"
     htmltext += "</tr></table><br>--------<br1>"
     htmltext += "<button value=\"Броня (+)\" action=\"bypass -h Quest purchase enchanted_1\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
     htmltext += "<button value=\"Бижутерия (+)\" action=\"bypass -h Quest purchase enchanted_2\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"    
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
       if item.canBeEnchanted() and preench > 0 and itemType == chType and itemId not in FORBIDDEN and itemTemplate.getDuration() == -1 and itemGrade > 3 and not item.isEquipped():
         augment = ""
         if chType == 0:
           augment = "<font color=333366>Нет аугмента</font>"
         if item.isAugmented() and item.getAugmentation().getAugmentSkill() != None:
           augment = item.getAugmentation().getAugmentSkill()
           augment,level = str(augment.getId()),str(augment.getLevel())
           augment = self.getAugmentSkill(int(augment), int(level))
         htmltext += "<tr><td><img src=\""+itemTemplate.getIcon()+"\" width=32 height=32><br></td><td><a action=\"bypass -h Quest purchase step2_" + str(item.getObjectId()) +"_1\">" + itemTemplate.getName() + "</a> <font color="+CENCH+">+" + str(preench) + "</font><br1>" + augment + "</td></tr>"
     htmltext += "</table><br></body></html>"
     return htmltext
   elif event.startswith("augment_"):
     chType = int(event.replace("augment_", ""))
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase add\">Назад|<-</a></td></tr></table><br1>"
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
       if item.canBeEnchanted() and item.isAugmented() and item.getAugmentation().getAugmentSkill() != None and itemType == 0 and itemId not in FORBIDDEN and itemTemplate.getDuration() == -1 and itemGrade > 3 and not item.isEquipped():
         augment = ""
         if chType == 0:
           augment = "<font color=333366>Нет аугмента</font>"
         if item.isAugmented() and item.getAugmentation().getAugmentSkill() != None:
           augment = item.getAugmentation().getAugmentSkill()
           augment,level = str(augment.getId()),str(augment.getLevel())
           augment = self.getAugmentSkill(int(augment), int(level))
         htmltext += "<tr><td><img src=\""+itemTemplate.getIcon()+"\" width=32 height=32><br></td><td><a action=\"bypass -h Quest purchase step2_" + str(item.getObjectId()) +"_2\">" + itemTemplate.getName() + "</a> <font color="+CENCH+">+" + str(preench) + "</font><br1>" + augment + "</td></tr>"
     htmltext += "</table><br></body></html>"
     return htmltext
   elif event.startswith("step2_"):
     search = str(event.replace("step2_", ""))
     itemObjId,chType=search.split("_")
     itemObjId,chType=int(itemObjId),int(chType)
     item = player.getInventory().getItemByObjectId(itemObjId)
     if item.canBeEnchanted() and item and not item.isEquipped():
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
       if item.isAugmented() and item.getAugmentation().getAugmentSkill() != None:
         augment = item.getAugmentation().getAugmentSkill()
        #if chType == 2 and augment == None:
          #return self.error("Подробная инфа о шмотке","Ошибка запроса")
         augment,level = str(augment.getId()),str(augment.getLevel())
         augSkill = int(augment)
         augment = self.getAugmentSkill(int(augment), int(level))
       quest.set("augment",str(augSkill))
       quest.set("objId",str(itemObjId))
       quest.set("enchant",str(preench)) 
       htmltext =  "<html><body>Шаг 2.<br>Подтверждаете?<br>"
       htmltext += "<table width=300><tr><td><img src=\""+itemTemplate.getIcon()+"\" width=32 height=32></td><td><font color="+CITEM+">" + itemTemplate.getName() + "</font><font color="+CENCH+">+" + str(preench) + "</font><br>"+ augment + "</td></tr></table><br><br>"
       htmltext += "Введите желаемую цену и выберите валюту:<br>"
       mvars = "Adena;"
       for i in moneys:
         mvars+=moneys[i]+";"
       htmltext += "<table width=300><tr><td><edit var=\"price\" width=70 length=\"16\"></td><td><combobox width=100 var=type list=\"" + mvars + "\"></td></tr></table><br>"
       htmltext += "<button value=\"Продолжить\" action=\"bypass -h Quest purchase step3_ $price _ $type\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
       htmltext += "<br><a action=\"bypass -h Quest purchase add\">Вернуться.</a></body></html>"
     else :
       htmltext = "<html><body>Ошибка!<br><a action=\"bypass -h Quest purchase step1\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("step3_ "):
     search = str(event.replace("step3_ ", ""))
     price,mvar=search.split("_")
     price,mvar=str(price),str(mvar)
     price = str(price.replace(" ", ""))
     if price == "" or mvar == "":
       return self.error("Шаг 3","Ошибка запроса")
     if not price.isdigit():
       return self.error("Шаг 3","Ошибка запроса")
     price = int(price)
     if price == 0:
       return self.error("Шаг 3","Ошибка запроса")
     mvarId = 57
     for i in moneys:
       if " "+moneys[i] == mvar:
         mvarId = i
         break
     if mvarId == 57 and price > MAX_ADENA:
       self.clearVars(player)
       return self.error("Шаг 3","Максимальная цена " + str(Util.formatAdena(MAX_ADENA)) + " Adena.")
     myItem = quest.getInt("objId")
     item = player.getInventory().getItemByObjectId(myItem)
     if item:
       if item.isEquipped():
         return self.error("Шаг 3","Ошибка запроса")
       if not item.canBeEnchanted():
         return self.error("Шаг 3","Ошибка запроса")
       myEnch = quest.getInt("enchant")
       myAugm = quest.getInt("augment")
       itemId = item.getItemId()
       itemTemplate = item.getItem() 
       itemType = itemTemplate.getType2()
       preench = item.getEnchantLevel()
       if preench != myEnch:
         self.clearVars(player)
         return self.error("Шаг 3","Ошибка запроса")
       augment = "нет." 
       augEffId = 0
       augSkId = 0
       augSkLvl = 0
       if item.isAugmented() and item.getAugmentation().getAugmentSkill() != None:
         augment = item.getAugmentation().getAugmentSkill()
         augEffId = item.getAugmentation().getAugmentationId()
         augSkId,augSkLvl = str(augment.getId()),str(augment.getLevel())
         if myAugm != int(augSkId):
           self.clearVars(player)
           return self.error("Шаг 3","Ошибка запроса")
      #<
       try:  
         con2=L2DatabaseFactory.getInstance().getConnection()
         storeitem=con2.prepareStatement("INSERT INTO `z_stock_items` (`id`,`itemId`,`itemName`,`enchant`,`augment`,`augAttr`,`augLvl`,`price`,`money`,`type`,`ownerId`,`shadow`) VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?);") 
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
         storeitem.executeUpdate()
       except:
         self.clearVars(player)
         return self.error("Шаг 3","Ошибка базы данных")
       finally:
         self.closeStat(storeitem)
         self.closeCon(con2)
       player.destroyItem("zzAuction",myItem, 1, player, 1)
      #<
       self.clearVars(player)
       htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase add\">Назад|<-</a></td></tr></table><br1>"
       htmltext += "<font color="+CITEM+">" + itemTemplate.getName() + "</font><font color="+CENCH+">+" + str(preench) + "</font><br1> " + self.getAugmentSkill(int(augSkId), int(augSkLvl)) + " <br1>выставлена на продажу!</body></html>"
     else:
       self.clearVars(player)
       return self.error("Шаг 3","Ошибка запроса")
     return htmltext
   elif event == "office":
    #data = str(strftime("%Y-%m-%d", gmtime()))
    #time = str(strftime("%H:%M:%S", gmtime()))
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest purchase list\">Назад|<-</a></td></tr></table><br1>"
     htmltext += "Привет, " + player.getName() + ".<br>"
    #htmltext += "Привет, " + player.getName() + ".<br>Сейчас " + data + " " + time + "<br>"
     htmltext += "<button value=\"Мои шмотки\" action=\"bypass -h Quest purchase StockShowPage_1_1_0_0_-1\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
     htmltext += "</body></html>"
     return htmltext
   return

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   if npcId == MARKET:
     return "<html><body>На аукционе вы можете торговать заточенными и аугментированными шмотками.<br><a action=\"bypass -h Quest purchase list\">Просмотр</a><br><br>Если шмотку купили, оплата придет на почту.</body></html>"
   return    

QUEST       = Quest(-1,qn,"custom")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(MARKET)
QUEST.addTalkId(MARKET)
