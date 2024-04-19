# -*- coding: utf-8 -*-
import sys
from ru.agecold import Config
from ru.agecold import L2DatabaseFactory
from ru.agecold.gameserver.datatables import AugmentationData
from ru.agecold.gameserver.datatables import ItemTable
from ru.agecold.gameserver.datatables import SkillTable
from ru.agecold.gameserver.idfactory import IdFactory
from ru.agecold.gameserver.model import L2Augmentation
from ru.agecold.gameserver.model import L2World
from ru.agecold.gameserver.model import L2ItemInstance
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.gameserver.network import SystemMessageId
from ru.agecold.gameserver.network.serverpackets import StatusUpdate
from ru.agecold.gameserver.network.serverpackets import ItemList
from ru.agecold.gameserver.network.serverpackets import SystemMessage
from ru.agecold.gameserver.network.serverpackets import ExShowQuestMark
from ru.agecold.gameserver.network.serverpackets import ExMailArrived
from ru.agecold.gameserver.network.serverpackets import PlaySound
from ru.agecold.gameserver.templates import L2EtcItemType
from ru.agecold.gameserver.templates import L2Item
from time import gmtime, strftime

qn = "q8782_ExpressPost"

#нпц
POSTMAN = 80009

#id марки
POSTMARK = 11971

#цены
PISMO = 1 # отправка письма
POSILKA = 10 # отправка посылки

#показ сообщений на страницу
LIMIT = 25
#показ шмота на страницу
LIMITT = 15

RAZDELY = [1,2]#,3,4,5,6,7,8,9,10,11,12,13,14,15]
#отображение названий разделов
trans = {1: "Оружие", 2: "Броня"}#, 3: "Ресурсы", 4: "Рецепты", 5: "Книжки", 6: "Патроны", 7: "7 печатей", 8: "Скроллы", 9: "Кристаллы", 10: "Зелья", 11: "Тату", 12: "Стрелы", 13: "СА кри", 14: "Удочки", 15: "Квестовые", 16: "Life Stone"}
# sorts не трогать!!
sorts = {1: "Weapon", 2: "Armor"}#, 3: "Resource", 4: "Recipe", 5: "Spellbook", 6: "Soulshot", 7: "AncientAdena", 8: "Scroll", 9: "Crystal", 10: "Potion", 11: "Tatoo", 12: "Arrow", 13: "SoulCrystal", 14: "Rod", 15: "QuestItems", 16: "LifeStone"}
#
CENCH = "CCCC33"
CPRICE = "669966"
CITEM = "993366"
CAUG1 = "333366"
CAUG2 = "006699"

class Quest (JQuest) :
 
 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def init_LoadGlobalData(self) :
   print "q8782_ExpressPost (Comercial) loaded."
   return

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
 
 def onAdvEvent (self,event,npc,player):
   st = player.getQuestState(qn)
   if event == "pisma":
     st.unset("stranic")
     htmltext = "pismo.htm"
     return htmltext
   elif event == "posilki":
     st.unset("sellitem")
     st.unset("lcount")
     htmltext = "posilka.htm"
     return htmltext
   elif event == "home":
     htmltext = "<html><body>Почтальон:<br>"
     htmltext += "<table width=270><tr><td><a action=\"bypass -h Quest q8782_ExpressPost inn\">Письма.</a><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Посылки.</a><br></td>"
     htmltext += "</tr></table><br>"
     htmltext += "Стоимость отправки письма: " + str(PISMO) + " L2TOP.<br1>"
     htmltext += "Стоимость отправки посылки: " + str(POSILKA) + " L2TOP.<br>"
     htmltext += "</body></html>"
     return htmltext
   elif event == "inn":
     name = str(st.getPlayer().getName())
     self.startQuestTimer("innn",500,None,player)
     htmltext = "<html><body>Загрузка "+name+"...<br1><img src=\"sek.cbui176\" width=10 height=3></body></html>"
     return htmltext
   elif event == "innn":
     name = str(st.getPlayer().getName())
     self.startQuestTimer("innnn",500,None,player)
     htmltext = "<html><body>Загрузка "+name+"...<br1><img src=\"sek.cbui176\" width=60 height=3></body></html>"
     return htmltext
   elif event == "innnn":
     name = str(st.getPlayer().getName())
     self.startQuestTimer("innnnn",500,None,player)
     htmltext = "<html><body>Загрузка "+name+"...<br1><img src=\"sek.cbui176\" width=110 height=3></body></html>"
     return htmltext
   elif event == "innnnn":
     name = str(st.getPlayer().getName())
     self.startQuestTimer("in",300,None,player)
     htmltext = "<html><body>Загрузка "+name+"...<br1><img src=\"sek.cbui176\" width=160 height=3></body></html>"
     return htmltext
   elif event == "in":
     name = str(st.getPlayer().getName())
     htmltext = "<html><body><table width=280><tr><td>Почта:</td><td align=right><table width=220 border=0><tr><td align=right><edit var=\"search\" width=90 length=\"16\"></td><td align=right><combobox width=51 var=keyword list=\"Тема;Автор\"></td><td><button value=\"Поиск\" action=\"bypass -h Quest q8782_ExpressPost find_ $search _ $keyword\" width=30 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></td></tr></table><br>"
     htmltext += "<table width=280><tr><td>"
     htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost in\">Входящие</a><br1>"
     htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost out\">Отправленные</a><br>"
     htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost adrbook\">Адресная книга</a>"
     htmltext += "</td><td align=right>"
     htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost pisma\">Написать письмо</a><br>"
     htmltext += "</td></table><br>"
     htmltext += "Входящие"
     htmltext += "<table width=300><tr><td></td><td>Автор</td><td>Тема</td><td>Дата</td></tr>"
     con=L2DatabaseFactory.getInstance().getConnection()
     inbox=con.prepareStatement("SELECT * FROM `z_post_in` WHERE `to`=?  ORDER BY `date` DESC, `time` DESC LIMIT 0,?")
     inbox.setString(1, name)
     inbox.setInt(2, int(LIMIT))
     rs=inbox.executeQuery()
     while (rs.next()) :
       pid=rs.getInt("id")  
       tema=rs.getString("tema")
       sender=rs.getString("from")
       data=rs.getString("date")
       ptype=rs.getInt("type")
       time=rs.getString("time")
       try :
         cdata = str(strftime("%Y-%m-%d", gmtime()))
         if cdata == data:
           data = "Сегодня "+time+""
         if ptype == 1:
           htmltext += "<tr><td width=16><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost inshow_"+str(pid)+"\">"+str(tema)+"</a></td><td>"+str(data)+"</td></tr>"
         else:
           htmltext += "<tr><td width=16><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost inshow_"+str(pid)+"\"><font color=CC00FF>"+str(tema)+"</font></a></td><td>"+str(data)+"</td></tr>"
       except :
         try : inbox.close()
         except : pass
     try :
       con.close()
     except :
       pass
     htmltext += "</table><br><br>"
     cona=L2DatabaseFactory.getInstance().getConnection()
     getcount=cona.prepareStatement("SELECT COUNT(`to`) FROM `z_post_in` WHERE `to`=?")
     getcount.setString(1,name)
     rsc=getcount.executeQuery()
     if (rsc.next()):
       rows  = rsc.getInt(1)
       vsego = int((rows/LIMIT)+1)
       if vsego > 2:
         htmltext += "Страницы: "
         st.set("stranic",str(vsego))
         prvsego = range(1,vsego)
         for i in prvsego:
           htmltext += ""
           htmltext += " <a action=\"bypass -h Quest q8782_ExpressPost str_1_" + str(i) +"\">"+str(i)+"</a><font color=>  </font>"
         else:
           htmltext += ""
     rsc.close()
     getcount.close()
     cona.close()
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost home\">Вернуться.</a>"
     htmltext += "</body></html>"
     return htmltext
   elif event == "out":
     name = str(st.getPlayer().getName())
     htmltext = "<html><body><table width=280><tr><td>Почта:</td><td align=right></td></tr></table><br>"
     htmltext += "<table width=280><tr><td>"
     htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost in\">Входящие</a><br1>"
     htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost out\">Отправленные</a><br>"
     htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost adrbook\">Адресная книга</a>"
     htmltext += "</td><td align=right>"
     htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost pisma\">Написать письмо</a><br>"
     htmltext += "</td></table><br>"
     htmltext += "Отправленные:"
     htmltext += "<table width=300><tr><td></td><td>Адресат</td><td>Тема</td><td>Дата</td></tr>"
     con=L2DatabaseFactory.getInstance().getConnection()
     inbox=con.prepareStatement("SELECT * FROM `z_post_out` WHERE `from`=?  ORDER BY `date` DESC, `time` DESC LIMIT 0,?")
     inbox.setString(1, name)
     inbox.setInt(2, int(LIMIT))
     rs=inbox.executeQuery()
     while (rs.next()) :
       pid=rs.getInt("id")  
       tema=rs.getString("tema")
       sender=rs.getString("to")
       data=rs.getString("date")
       ptype=rs.getInt("type")
       time=rs.getString("time")
       try :
         cdata = str(strftime("%d %m %Y", gmtime()))
         if cdata == data:
           data = time
         htmltext += "<tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost outshow_"+str(pid)+"\">"+str(tema)+"</a></td><td>"+str(data)+"</td></tr>"
       except :
         try : inbox.close()
         except : pass
     try :
       con.close()
     except :
       pass
     htmltext += "</table><br><br>"
     cona=L2DatabaseFactory.getInstance().getConnection()
     getcount=cona.prepareStatement("SELECT COUNT(`from`) FROM `z_post_in` WHERE `from`=?")
     getcount.setString(1,name)
     rsc=getcount.executeQuery()
     if (rsc.next()):
       rows  = rsc.getInt(1)
       vsego = int((rows/LIMIT)+1)
       if vsego > 2:
         htmltext += "Страницы: "
         st.set("stranic",str(vsego))
         prvsego = range(1,vsego)
         for i in prvsego:
           htmltext += ""
           htmltext += " <a action=\"bypass -h Quest q8782_ExpressPost str_2_" + str(i) +"\">"+str(i)+"</a><font color=>  </font>"
         else:
           htmltext += ""
     rsc.close()
     getcount.close()
     cona.close()
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a>"
     htmltext += "</body></html>"
     return htmltext
   elif event.startswith("inshow_"):
     sid = int(event.replace("inshow_", ""))
     con=L2DatabaseFactory.getInstance().getConnection()
     show=con.prepareStatement("SELECT * FROM `z_post_in` WHERE `id`=?")
     show.setInt(1, sid)
     rs=show.executeQuery()
     while (rs.next()) :
       pid=rs.getInt("id")
       tema=rs.getString("tema")
       text=rs.getString("text")
       sender=rs.getString("from")
       data=rs.getString("date")
       time=rs.getString("time")
       ptype=rs.getInt("type")
       try :
         cdata = str(strftime("%d %m %Y", gmtime()))
         if cdata == data:
           pdata = time
         else:
           pdata = str(""+data+" ("+time+")")  
         htmltext = "<html><body><table width=290><tr><td align=left>"+str(tema)+"</td><td align=right><a action=\"bypass -h Quest q8782_ExpressPost in\">Входящие|x</a></td></tr></table><br>"
         htmltext += "<table width=300><tr><td align=left>От: "+str(sender)+"</td><td align=right>"+str(pdata)+"</td><td width=55 align=right><button value=\"Ответить\" action=\"bypass -h Quest q8782_ExpressPost fpform_"+str(sender)+"\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br>"
         htmltext += "<img src=\"sek.cbui355\" width=300 height=2><br>"+str(text)+"<br><br><img src=\"sek.cbui355\" width=300 height=2><br>"
         htmltext += "<table width=280><tr><td align=left><button value=\"Доб. контакт\" action=\"bypass -h Quest q8782_ExpressPost adrbook_ "+str(sender)+" _ +\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td align=right><button value=\"Удалить письмо\" action=\"bypass -h Quest q8782_ExpressPost del_1_"+str(pid)+"\" width=85 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br>"
         htmltext += "<br><a action=\"bypass -h Quest q8782_ExpressPost in\">Назад.</a></body></html>"
         #
         if ptype == 0:
           incon=L2DatabaseFactory.getInstance().getConnection()
           updatein=incon.prepareStatement("UPDATE z_post_in SET type=1 WHERE id=?")
           updatein.setInt(1, pid)
           try :
             updatein.executeUpdate()
             updatein.close()
             incon.close()
           except :
             try : incon.close()
             except : pass
       except :
         try : show.close()
         except : pass
     try :
       con.close()
     except :
       pass
     return htmltext
   elif event.startswith("outshow_"):
     oid = int(event.replace("outshow_", ""))
     con=L2DatabaseFactory.getInstance().getConnection()
     show=con.prepareStatement("SELECT * FROM `z_post_out` WHERE id=?")
     show.setInt(1, oid)
     rs=show.executeQuery()
     while (rs.next()) :
       pid=rs.getString("id")
       tema=rs.getString("tema")
       text=rs.getString("text")
       sender=rs.getString("to")
       date=rs.getString("date")
       time=rs.getString("time")
       try :
         cdata = str(strftime("%d %m %Y", gmtime()))
         if cdata == date:
           pdata = time
         else:
           pdata = str(""+date+" ("+time+")")  
         htmltext = "<html><body><table width=260><tr><td align=left>"+str(tema)+"</td><td align=right><a action=\"bypass -h Quest q8782_ExpressPost out\">Отправленные|x</a></td></tr></table><br>"
         htmltext += "<table width=300><tr><td align=left>Кому: "+str(sender)+"</td><td align=right>"+str(pdata)+"</td></tr></table><br>"
         htmltext += "<img src=\"sek.cbui355\" width=300 height=2><br>"+str(text)+"<br><br><img src=\"sek.cbui355\" width=300 height=2><br>"
         htmltext += "<table width=280><tr><td align=left></td><td align=right><button value=\"Удалить письмо\" action=\"bypass -h Quest q8782_ExpressPost del_2_"+str(pid)+"\" width=85 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br>"
         htmltext += "<br><a action=\"bypass -h Quest q8782_ExpressPost out\">Назад.</a></body></html>"
       except :
         try : show.close()
         except : pass
     try :
       con.close()
     except :
       pass
     return htmltext
   elif event == "pis_char":
     if st.getQuestItemsCount(POSTMARK) < PISMO:
       htmltext = "<html><body>К сожалению, у вас не хватает почтовых марок.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a></body></html>"
       return htmltext
     htmltext = "<html><body>Кому:<br>"
     htmltext += "<edit var=\"target\" width=200 length=\"16\">"
     htmltext += "Тема:<br>"
     htmltext += "<edit var=\"tema\" width=200 length=\"16\">"
     htmltext += "Текст:"
     htmltext += "<multiedit var=\"text\" width=280 height=70><br>"
     htmltext += "<button value=\"Отправить\" action=\"bypass -h Quest q8782_ExpressPost send_ $target _ $tema _ $text\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("send_ "):
     if st.getQuestItemsCount(POSTMARK) < PISMO:
       htmltext = "<html><body>К сожалению, у вас не хватает почтовых марок.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a></body></html>"
       return htmltext
     search = str(event.replace("send_ ", ""))
     target,tema,text=search.split("_")
    #<
     target = str(target.replace(" ", ""))
     tema = str(tema.replace(" ", ""))
     ctext = str(text.replace(" ", ""))
     if target == "":
       htmltext = "<html><body>Вы не ввели имя получателя.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a></body></html>"
       return htmltext
     if tema == "":
       tema = "Без темы"
     if ctext == "":
       text = " "
    #<
     cona=L2DatabaseFactory.getInstance().getConnection()
     getcount=cona.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE char_name=?")
     getcount.setString(1,target)
     rsc=getcount.executeQuery()
     if (rsc.next()):
       if rsc.getInt(1) == 0:
         htmltext = "<html><body>Перс с ником "+target+" не найден.<br>"
         htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
         return htmltext
     rsc.close()
     getcount.close()
     cona.close()
    #< 
     name = str(st.getPlayer().getName())
     data = str(strftime("%Y-%m-%d", gmtime()))
     time = str(strftime("%H:%M:%S", gmtime()))
     uniq = IdFactory.getInstance().getNextId()
    #>
     ins=L2DatabaseFactory.getInstance().getConnection()
     sendin=ins.prepareStatement("INSERT INTO z_post_in VALUES (?,?,?,?,?,?,?,?)") 
     sendin.setInt(1, uniq)
     sendin.setString(2, tema)
     sendin.setString(3, text)
     sendin.setString(4, name)
     sendin.setString(5, target)
     sendin.setInt(6, int(0))
     sendin.setString(7, data)
     sendin.setString(8, time)
     try :
       sendin.executeUpdate()
       sendin.close()
       ins.close()
     except :
       try : ins.close()
       except : pass
    #>
     ins=L2DatabaseFactory.getInstance().getConnection()
     sendin=ins.prepareStatement("INSERT INTO z_post_out VALUES (?,?,?,?,?,?,?,?)") 
     sendin.setInt(1, uniq)
     sendin.setString(2, tema)
     sendin.setString(3, text)
     sendin.setString(4, name)
     sendin.setString(5, target)
     sendin.setInt(6, int(0))
     sendin.setString(7, data)
     sendin.setString(8, time)
     try :
       sendin.executeUpdate()
       sendin.close()
       ins.close()
     except :
       try : ins.close()
       except : pass
    #>
     alarm = L2World.getInstance().getPlayer(target)
     if alarm:
       alarm.sendPacket(SystemMessage.sendString("Вам было отправлено письмо!"))
       alarm.sendPacket(ExMailArrived())
       alarm.sendPacket(PlaySound("ItemSound.quest_finish"))
    #>
     st.takeItems(POSTMARK,PISMO)
     htmltext = "<html><body>Почтальон:<br>"
     htmltext += "Ваше письмо для "+target+" ("+tema+") отправлено.<br><br>"
     htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a><br>"
     htmltext += "</body></html>"
     return htmltext
   elif event.startswith("del_"):
     search = str(event.replace("del_", ""))
     ptype,pid=search.split("_")
     ptype,pid=int(ptype),int(pid)
    #>
     con=L2DatabaseFactory.getInstance().getConnection()
     if ptype == 1:
       sql=con.prepareStatement("DELETE FROM z_post_in WHERE id=?")
     else:
       sql=con.prepareStatement("DELETE FROM z_post_out WHERE id=?")
     delete=sql 
     delete.setInt(1, pid)
     try :
       delete.executeUpdate()
       delete.close()
       con.close()
     except :
       try : con.close()
       except : pass
    #>
     htmltext = "<html><body>Почтальон:<br>"
     htmltext += "Удалено.<br><br>"
     if ptype == 1:
       htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a><br>"
     else:
       htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost out\">Вернуться.</a><br>"
     htmltext += "</body></html>"
     return htmltext
   elif event.startswith("str_"):
     search = str(event.replace("str_", ""))
     iotype,ioid=search.split("_")
     iotype,ioid=int(iotype),int(ioid)
     name = str(st.getPlayer().getName())
     strCount = st.getInt("stranic")
     strList = int(ioid - 1)
     ones = {1: 0, 2: LIMIT} 
     one = ones.get(ioid, int(LIMIT*strList))
     two = LIMIT
     if iotype == 1:
       htmltext = "<html><body>Почтальон:<br>"
       htmltext += "Входящие"
       htmltext += "<table width=300><tr><td></td><td>Автор</td><td>Тема</td><td>Дата</td></tr>"
       con=L2DatabaseFactory.getInstance().getConnection()
       inbox=con.prepareStatement("SELECT * FROM `z_post_in` WHERE `to`=?  ORDER BY `date` DESC, `time` DESC LIMIT ?,?")
       inbox.setString(1, name)
       inbox.setInt(2, one)
       inbox.setInt(3, two)
       rs=inbox.executeQuery()
       while (rs.next()) :
         pid=rs.getInt("id")  
         tema=rs.getString("tema")
         sender=rs.getString("from")
         data=rs.getString("date")
         ptype=rs.getInt("type")
         time=rs.getString("time")
         try :
           cdata = str(strftime("%d %m %Y", gmtime()))
           if cdata == data:
             data = time
           if ptype == 1:
             htmltext += "<tr><td width=16><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost inshow_"+str(pid)+"\">"+str(tema)+"</a></td><td>"+str(data)+"</td></tr>"
           else:
             htmltext += "<tr><td width=16><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost inshow_"+str(pid)+"\"><font color=CC00FF>"+str(tema)+"</font></a></td><td>"+str(data)+"</td></tr>"
         except :
           try : inbox.close()
           except : pass
       try :
         con.close()
       except :
         pass
     elif iotype == 2:
       htmltext = "<html><body>Почтальон:<br>"
       htmltext += "Отправленные:"
       htmltext += "<table width=300><tr><td></td><td>Адресат</td><td>Тема</td><td>Дата</td></tr>"
       con=L2DatabaseFactory.getInstance().getConnection()
       inbox=con.prepareStatement("SELECT * FROM `z_post_out` WHERE `from`=?  ORDER BY `date` DESC, `time` DESC LIMIT ?,?")
       inbox.setString(1, name)
       inbox.setInt(2, one)
       inbox.setInt(3, two)
       rs=inbox.executeQuery()
       while (rs.next()) :
         pid=rs.getInt("id")  
         tema=rs.getString("tema")
         sender=rs.getString("from")
         data=rs.getString("date")
         ptype=rs.getInt("type")
         try :
           htmltext += "<tr><td><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost outshow_"+str(pid)+"\">"+str(tema)+"</a></td><td>"+str(data)+"</td></tr>"
         except :
           try : inbox.close()
           except : pass
       try :
         con.close()
       except :
         pass
     elif iotype == 3:
       htmltext = "<html><body>Почтальон:<br>"
       htmltext += "Входящие"
       htmltext += "<table width=300><tr><td></td><td>Автор</td><td>Тема</td><td>Дата</td></tr>"
       con=L2DatabaseFactory.getInstance().getConnection()
       inbox=con.prepareStatement("SELECT * FROM `z_post_pos` WHERE `to`=?  ORDER BY `date` DESC, `time` DESC LIMIT 0,?")
       inbox.setString(1, name)
       inbox.setInt(2, int(LIMIT))
       rs=inbox.executeQuery()
       while (rs.next()) :
         pid=rs.getInt("id")  
         tema=rs.getString("tema")
         sender=rs.getString("from")
         data=rs.getString("date")
         ptype=rs.getInt("type")
         time=rs.getString("time")
         try :
           cdata = str(strftime("%d %m %Y", gmtime()))
           if cdata == data:
             data = time
           if ptype == 1:
             htmltext += "<tr><td width=16><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost inshow_"+str(pid)+"\">"+str(tema)+"</a></td><td>"+str(data)+"</td></tr>"
           else:
             htmltext += "<tr><td width=16><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost inshow_"+str(pid)+"\"><font color=CC00FF>"+str(tema)+"</font></a></td><td>"+str(data)+"</td></tr>"
         except :
           try : inbox.close()
           except : pass
       try :
         con.close()
       except :
         pass
     elif iotype == 4:
       ownerId = str(st.getPlayer().getObjectId())
       htmltext = "<html><body>Почтальон:<br>"
       htmltext += "Список друзей:"
       htmltext += "<table width=300><tr><td></td></tr>"
       con=L2DatabaseFactory.getInstance().getConnection()
       inbox=con.prepareStatement("SELECT * FROM `character_friends` WHERE `char_id`=?  ORDER BY `friend_name` LIMIT 0,?")
       inbox.setString(1, ownerId)
       inbox.setInt(2, int(LIMIT))
       rs=inbox.executeQuery()
       while (rs.next()) :
         target=rs.getString("friend_name")  
         try :
           if ptype == 1:
             htmltext += "<tr><td><a action=\"bypass -h Quest q8782_ExpressPost fpform_"+str(target)+"\">"+str(target)+"</a></td></tr>"
           else:
             htmltext += "<tr><td><a action=\"bypass -h Quest q8782_ExpressPost fposform_"+str(target)+"\">"+str(target)+"</a></td></tr>"      
         except :
           try : inbox.close()
           except : pass
       try :
         con.close()
       except :
         pass
     elif iotype == 5:
       clanId = str(st.getPlayer().getClanId())
       htmltext = "<html><body>Почтальон:<br>"
       htmltext += "Список сокланов:"
       htmltext += "<table width=300><tr><td></td></tr>"
       con=L2DatabaseFactory.getInstance().getConnection()
       inbox=con.prepareStatement("SELECT * FROM `characters` WHERE `clanid`=?  ORDER BY `clanid` LIMIT 0,?")
       inbox.setString(1, clanId)
       inbox.setInt(2, int(LIMIT))
       rs=inbox.executeQuery()
       while (rs.next()) :
         target=rs.getString("char_name")  
         try :
           if ptype == 1:
             htmltext += "<tr><td><a action=\"bypass -h Quest q8782_ExpressPost fpform_"+str(target)+"\">"+str(target)+"</a></td></tr>"
           else:
             htmltext += "<tr><td><a action=\"bypass -h Quest q8782_ExpressPost cposform_"+str(target)+"\">"+str(target)+"</a></td></tr>"      
         except :
           try : inbox.close()
           except : pass
       try :
         con.close()
       except :
         pass
     elif iotype == 6:
       name = str(st.getPlayer().getName())
       htmltext = "<html><body>Почта:<table width=220 border=0><tr><td align=right><edit var=\"nick\" width=90 length=\"16\"></td><td align=right><combobox width=15 var=act list=\"+;-\"></td><td><button value=\"Ок\" action=\"bypass -h Quest q8782_ExpressPost adrbook_ $nick _ $act\" width=30 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>"
       htmltext += "<br>Адресная книга"
       htmltext += "<table width=300><tr><td></td></tr>"
       con=L2DatabaseFactory.getInstance().getConnection()
       inbox=con.prepareStatement("SELECT * FROM `z_post_adrbook` WHERE `name`=?  ORDER BY `friend` LIMIT 0,?")
       inbox.setString(1, name)
       inbox.setInt(2, int(LIMIT))
       rs=inbox.executeQuery()
       while (rs.next()) :
         target=rs.getString("friend")  
         try :
           htmltext += "<tr><td><a action=\"bypass -h Quest q8782_ExpressPost fpform_"+str(target)+"\">"+str(target)+"</a></td></tr>"    
         except :
           try : inbox.close()
           except : pass
       try :
         con.close()
       except :
         pass
     htmltext += "</table><br>Страницы: "
     prvsego = range(1,strCount)
     for i in prvsego:
       htmltext += "<a action=\"bypass -h Quest q8782_ExpressPost str_"+str(iotype)+"_" + str(i) +"\">"+str(i)+"</a><font color=>  </font>"
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost home\">Вернуться.</a><br>"
     htmltext += "</body></html>"
     return htmltext
   elif event == "send_pos":
     if st.getQuestItemsCount(POSTMARK) < POSILKA:
       htmltext = "<html><body>К сожذлению, у вас не хватает почтовых марок.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
       return htmltext
     st.unset("count")
     st.unset("stranic")
     st.unset("sellitem")
     st.unset("grade")
     st.unset("type")
     htmltext = "<html><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><img src=\"L2UI.SquareBlank\" width=260 height=2><br1>"   
     htmltext += "<table width=256><tr><td width=128><center><font color=37ADFF></font>Приветствую, вы хотите отправить?<br>"
    #for i in RAZDELY:
      #razdel = trans.get(i, str(""))  
      #htmltext += "<button value=\""+str(razdel)+"\" action=\"bypass -h Quest q8782_ExpressPost wtss_"+str(i)+"\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1>"
     htmltext += "<button value=\"Пушки с ЛС\" action=\"bypass -h Quest q8782_ExpressPost augment\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1>"
     htmltext += "</center></td></tr></table><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center>"
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     return htmltext
   elif event == "augment":
     htmltext = "<html><body><table><tr><td width=260>Аукцион</td><td align=right width=70><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Назад|<-</a></td></tr></table><br1>"
     htmltext += "Шаг 1.<br>Выберите шмотку:<br><br><table width=300>"
     for item in player.getInventory().getItems():
       itemTemplate = item.getItem()
       itemId = itemTemplate.getItemId()
       itemType = itemTemplate.getType2()
       itemGrade = itemTemplate.getCrystalType()
       preench = item.getEnchantLevel()
       if item.canBeEnchanted() and item.isAugmented() and item.getAugmentation().getAugmentSkill() != None and itemType == 0 and not item.isEquipped():
         augment = item.getAugmentation().getAugmentSkill()
         augment,level = str(augment.getId()),str(augment.getLevel())
         augment = self.getAugmentSkill(int(augment), int(level))
         htmltext += "<tr><td><img src=\""+itemTemplate.getIcon()+"\" width=32 height=32><br></td><td><a action=\"bypass -h Quest q8782_ExpressPost posonenext_" + str(item.getObjectId()) +"\">" + itemTemplate.getName() + "</a> <font color="+CENCH+">+" + str(preench) + "</font><br1>" + augment + "</td></tr>"
     htmltext += "</table><br></body></html>"
     return htmltext
   elif event.startswith("wtss_"):
     wts = int(event.replace("wtss_", ""))
     razdel = trans.get(wts, str(""))
     htmltext = "<html><body>Почтальон:Посылка:<br>"+str(razdel)+":<br><table width=300>"
     SPWEAPONS = ["Sword","Blunt","Dagger","Bow","Etc","Pole","Fist","Dual Sword","Dual Fist","Big Sword","Big Blunt"]
     SPARMOR = ["Shield","Light","Heavy","Magic","None"]
     sorts = {1: SPWEAPONS, 2: SPARMOR}#, 3: "Material", 4: "Receipe", 5: "Spellbook", 6: "Shot", 7: "Money", 8: EWA, 9: CRY, 10: "Potion", 11: TATOO, 12: "Arrow", 13: SACRY, 14: "Rod", 15: QUESTITEMS, 16: LIFESTONE}
     wtsType = sorts.get(wts, str(""))
     #trouble = [8,9,11,13,15]  
     for Item in player.getInventory().getItems():
       itemTemplate = Item.getItem()
       idtest = Item.getItemId()
       itype = str(Item.getItemType())
       if not Item.isEquipped() and idtest != 57 and itype in wtsType:
         cnt = Item.getCount()
         count = str(cnt)
         grade = itemTemplate.getCrystalType()   
         if grade == 1:
           pgrade = str("[D]")
         elif grade == 2:
           pgrade = str("[C]")
         elif grade == 3:
           pgrade = str("[B]")
         elif grade == 4:
           pgrade = str("[A]")
         elif grade == 5:
           pgrade = str("[S]")
         else:
           pgrade = str("")
         if Item.getEnchantLevel() == 0:
           enchant = str("")
         else:
           enchant = " +"+str(Item.getEnchantLevel())+""
         chk = int(count)
         count = (count > 1 and str(count)) or str("")
         if wts == 7:
           s = str(cnt)
           for i in xrange(len(s)-3,s[0] in ('+','-'),-3):
             s=s[:i]+','+s[i:]
           count = "<font color=00CCFF>"+str(s)+"</font>"
         if chk == 1:
           htmltext += "<tr><td><img src=\""+itemTemplate.getIcon()+"\" width=32 height=32></td><td><a action=\"bypass -h Quest q8782_ExpressPost posonenext_" + str(Item.getObjectId()) +"\">" + itemTemplate.getName() + ""+str(pgrade)+" " + enchant + "</a></td></tr>"
         else:
           htmltext += "<tr><td><img src=\""+itemTemplate.getIcon()+"\" width=32 height=32></td><td><a action=\"bypass -h Quest q8782_ExpressPost posmore_" + str(Item.getObjectId()) +"\">"+str(count)+" " + itemTemplate.getName() + ""+str(pgrade)+" " + enchant + "</a></td></tr>"
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("posonenext_"):
     itemObjId = int(event.replace("posonenext_", ""))
     obj = str(itemObjId)
     if st.getQuestItemsCount(POSTMARK) < POSILKA:
       htmltext = "<html><body>К сожалению, у вас не хватает почтовых марок.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
       return htmltext
     item = player.getInventory().getItemByObjectId(itemObjId)
     if item and item.isAugmented() and item.getAugmentation().getAugmentSkill() != None and not item.isEquipped():
       itemId = item.getItemId()
       itemTemplate = item.getItem() 
       itemType = itemTemplate.getType2()
       preench = item.getEnchantLevel()
       augment = item.getAugmentation().getAugmentSkill()
       augment,level = str(augment.getId()),str(augment.getLevel())
       augment = self.getAugmentSkill(int(augment), int(level))
       st.set("sellitem",str(itemObjId))
       st.set("enchant",str(preench)) 
       htmltext =  "<html><body>Шаг 2.<br>Подтверждаете?<br>"
       htmltext += "<table width=300><tr><td><img src=\""+itemTemplate.getIcon()+"\" width=32 height=32></td><td><font color="+CITEM+">" + itemTemplate.getName() + "</font><font color="+CENCH+">+" + str(preench) + "</font><br>"+ augment + "</td></tr></table><br><br>"
       htmltext += "Отправить...<br1>"
       htmltext += "<br><a action=\"bypass -h Quest q8782_ExpressPost friend_2\">Другу</a>"
       htmltext += "<br><a action=\"bypass -h Quest q8782_ExpressPost clan_2\">Соклану</a>"
       htmltext += "<br><a action=\"bypass -h Quest q8782_ExpressPost posone_" + str(item.getObjectId()) +"\">Персонажу</a>"
       htmltext += "<br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     else :
       htmltext = "<html><body>Ошибка!<br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("posmorenext_"):
     search = str(event.replace("posmorenext_ ", ""))
     itemCount,itemObjId=search.split(" _ ")
     itemCount,itemObjId=int(itemCount),int(itemObjId)
     obj = str(itemObjId)
     if st.getQuestItemsCount(POSTMARK) < POSILKA:
       htmltext = "<html><body>К сожалению, у вас не хватает почтовых марок.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a></body></html>"
       return htmltext
     Item = st.getPlayer().getInventory().getItemByObjectId(itemObjId)
     itemTemplate = Item.getItem()
     if Item and Item.isAugmented() and Item.getAugmentation().getAugmentSkill() != None and itemTemplate.getDuration() == -1 and not Item.isEquipped() and not Item.isAugmented() and Item.isTradeable():
       grade = itemTemplate.getCrystalType()
       igrade = str(itemTemplate.getCrystalType())
       itype = str(Item.getItemType())
       idtest = Item.getItemId()
       st.set("sellitem",obj)
       st.set("lcount",str(itemCount))
       grades = {1: "d", 2: "c", 3: "b", 4: "a", 5: "s"}
       pgrade = grades.get(grade, str(""))
       enchant = (Item.getEnchantLevel() > 0 and " +"+str(Item.getEnchantLevel())+"") or str("")
       htmltext =  "<html><body>Почтальон:<br>Заполните форму:<br>"
       htmltext += "<table width=300><tr><td><img src=\"" + itemTemplate.getIcon() + "\" width=32 height=32></td><td><font color=LEVEL>" + itemTemplate.getName() + " " + enchant + "</font><img src=\"symbol.grade_"+str(pgrade)+"\" width=16 height=16><br></td></tr></table><br><br>"
       htmltext += "Количество: "+str(itemCount)+"<br>"
       htmltext += "Получатель:<br>"
       htmltext += "<br><a action=\"bypass -h Quest q8782_ExpressPost friend_2\">Другу</a>"
       htmltext += "<br><a action=\"bypass -h Quest q8782_ExpressPost clan_2\">Соклану</a>"
       htmltext += "<br><a action=\"bypass -h Quest q8782_ExpressPost posmoreend_"+str(itemCount)+"_"+str(Item.getObjectId())+"\">Персонажу</a>"
       htmltext += "<br><br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     else :
       htmltext = "<html><body>Этот предмет нельзя отправить!<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("posone_"):
     itemObjId = int(event.replace("posone_", ""))
     obj = str(itemObjId)
     if st.getQuestItemsCount(POSTMARK) < POSILKA:
       htmltext = "<html><body>К сожалению, у вас не хватает почтовых марок.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
       return htmltext
     item = player.getInventory().getItemByObjectId(itemObjId)
     if item and item.canBeEnchanted() and item.isAugmented() and item.getAugmentation().getAugmentSkill() != None and not item.isEquipped():
       itemId = item.getItemId()
       itemTemplate = item.getItem() 
       itemType = itemTemplate.getType2()
       preench = item.getEnchantLevel()
       augSkill = 0  
       augment = item.getAugmentation().getAugmentSkill()
       augment,level = str(augment.getId()),str(augment.getLevel())
       augSkill = int(augment)
       augment = self.getAugmentSkill(int(augment), int(level))
       htmltext =  "<html><body>Шаг 3.<br>Кому<br>"
       htmltext += "<table width=300><tr><td><img src=\""+itemTemplate.getIcon()+"\" width=32 height=32></td><td><font color="+CITEM+">" + itemTemplate.getName() + "</font><font color="+CENCH+">+" + str(preench) + "</font><br>"+ augment + "</td></tr></table><br><br>"
       htmltext += "Получатель:"
       htmltext += "<edit var=\"target\" width=200 length=\"16\"><br>"
       htmltext += "Тема:"
       htmltext += "<edit var=\"tema\" width=200 length=\"16\"><br>"
       htmltext += "Текст:"
       htmltext += "<multiedit var=\"text\" width=280 height=70><br>"
       htmltext += "<br><br>Безопасность***:<br>"
       htmltext += "Ключевое слово:"
       htmltext += "<edit var=\"key\" width=200 length=\"16\"><br>"
       htmltext += "Coin of Luck:"
       htmltext += "<edit var=\"col\" width=50 length=\"5\">"
       htmltext += "Blue Eva:"
       htmltext += "<edit var=\"blueeva\" width=50 length=\"5\">"
       htmltext += "Gold Golem:"
       htmltext += "<edit var=\"goldgolem\" width=50 length=\"5\"><br><br>"
       htmltext += "<button value=\"Отправить\" action=\"bypass -h Quest q8782_ExpressPost possend_ $target _ $tema _ $text _ $key _ $col _ $blueeva _ $goldgolem\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
       htmltext += "<br><br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a><br>"
       htmltext += "*** указываем, сколько получатель должен заплатить за посылку или ключевое слово.</body></html>"
     else :
       htmltext = "<html><body>Ошибка!<br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("posmore_"):
     itemObjId = int(event.replace("posmore_", ""))
     obj = str(itemObjId)
     if st.getQuestItemsCount(POSTMARK) < POSILKA:
       htmltext = "<html><body>К сожалению, у вас не хватает почтовых марок.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
       return htmltext
     Item = st.getPlayer().getInventory().getItemByObjectId(itemObjId)
     itemTemplate = Item.getItem()
     if Item and Item.isAugmented() and Item.getAugmentation().getAugmentSkill() != None and itemTemplate.getDuration() == -1 and not Item.isEquipped() and not Item.isAugmented() and Item.isTradeable():
       cnt = Item.getCount()
       count = str(cnt)
       grade = itemTemplate.getCrystalType()
       igrade = str(itemTemplate.getCrystalType())
       itype = str(Item.getItemType())
       idtest = Item.getItemId()
       grades = {1: "d", 2: "c", 3: "b", 4: "a", 5: "s"}
       pgrade = grades.get(grade, str(""))
       enchant = (Item.getEnchantLevel() > 0 and " +"+str(Item.getEnchantLevel())+"") or str("")
       st.set("sellitem",obj)
       htmltext =  "<html><body>Почтальон:<br>Установите количество:<br>"
       htmltext += "<table width=300><tr><td><img src=\"" + itemTemplate.getIcon() + "\" width=32 height=32></td><td><font color=LEVEL>" + itemTemplate.getName() + " " + enchant + "</font><img src=\"symbol.grade_"+str(pgrade)+"\" width=16 height=16><br></td></tr></table><br><br>"
       htmltext += "Всего: "+count+"<br>"
       htmltext += "Укажите количество: <br>"
       htmltext += "<edit var=\"cols\" width=200 length=\"16\"><br>"
       htmltext += "<button value=\"Ok\" action=\"bypass -h Quest q8782_ExpressPost posmorenext_ $cols _ "+str(obj)+"\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
       htmltext += "<br><br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     else :
       htmltext = "<html><body>Этот предмет нельзя отправить!<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("posmoreend_"):
     search = str(event.replace("posmoreend_", ""))
     itemCount,itemObjId=search.split("_")
     itemCount,itemObjId=int(itemCount),int(itemObjId)
     obj = str(itemObjId)
     if st.getQuestItemsCount(POSTMARK) < POSILKA:
       htmltext = "<html><body>К сожалению, у вас не хватает почтовых марок.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a></body></html>"
       return htmltext
     Item = st.getPlayer().getInventory().getItemByObjectId(itemObjId)
     itemTemplate = Item.getItem()
     if Item and Item.isAugmented() and Item.getAugmentation().getAugmentSkill() != None and itemTemplate.getDuration() == -1 and not Item.isEquipped() and not Item.isAugmented() and Item.isTradeable():
       grade = itemTemplate.getCrystalType()
       igrade = str(itemTemplate.getCrystalType())
       itype = str(Item.getItemType())
       idtest = Item.getItemId()
       st.set("sellitem",obj)
       st.set("lcount",str(itemCount))
       grades = {1: "d", 2: "c", 3: "b", 4: "a", 5: "s"}
       pgrade = grades.get(grade, str(""))
       enchant = (Item.getEnchantLevel() > 0 and " +"+str(Item.getEnchantLevel())+"") or str("")
       htmltext =  "<html><body>Почтальон:<br>Заполните форму:<br>"
       htmltext += "<table width=300><tr><td><img src=\"" + itemTemplate.getIcon() + "\" width=32 height=32></td><td><font color=LEVEL>" + itemTemplate.getName() + " " + enchant + "</font><img src=\"symbol.grade_"+str(pgrade)+"\" width=16 height=16><br></td></tr></table><br><br>"
       htmltext += "Количество: "+str(itemCount)+"<br>"
       htmltext += "Получатель:<br>"
       htmltext += "<edit var=\"target\" width=200 length=\"16\">"
       htmltext += "Тема:<br>"
       htmltext += "<edit var=\"tema\" width=200 length=\"16\">"
       htmltext += "Текст:"
       htmltext += "<multiedit var=\"text\" width=280 height=70><br>"
       htmltext += "<br><br>Опции зищиты***:<br>"
       htmltext += "Ключевое слово:<br>"
       htmltext += "<edit var=\"key\" width=50 length=\"16\">"
       htmltext += "Coin of Luck:<br>"
       htmltext += "<edit var=\"col\" width=50 length=\"16\">"
       htmltext += "Blue Eva:<br>"
       htmltext += "<edit var=\"blueeva\" width=50 length=\"16\">"
       htmltext += "Gold Golem:<br>"
       htmltext += "<edit var=\"goldgolem\" width=200 length=\"16\"><br><br>"
       htmltext += "<button value=\"Отправить\" action=\"bypass -h Quest q8782_ExpressPost possend_ $target _ $tema _ $text _ $key _ $col _ $blueeva _ $goldgolem\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
       htmltext += "<br><br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a><br>"
       htmltext += "*** указываем, сколько получатель должен заплатить за посылку или ключевое слово.</body></html>"
     else :
       htmltext = "<html><body>Этот предмет нельзя отправить!<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("possend_ "):
    #htmltext = "<html><body>Перерыв 5 минут.</body></html>"
    #return htmltext
     search = str(event.replace("possend_ ", ""))
     target,tema,text,key,col,blueeva,goldgolem=search.split("_")
     target,tema,text,key,col,blueeva,goldgolem=str(target),str(tema),str(text),str(key),str(col),str(blueeva),str(goldgolem)
    #<
     target = str(target.replace(" ", ""))
     tema = str(tema.replace(" ", ""))
     ctext = str(text.replace(" ", ""))
     key = str(key.replace(" ", ""))
     col = str(col.replace(" ", ""))
     blueeva = str(blueeva.replace(" ", ""))
     goldgolem = str(goldgolem.replace(" ", ""))
     if target == "":
       htmltext = "<html><body>Вы не ввели имя получателя.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
       return htmltext
     if tema == "":
       tema = "Без темы"
     if ctext == "":
       text = " "
     if key == "":
       key = "0"
     if col == "":
       col = "0"
     if blueeva == "":
       blueeva = "0"
     if goldgolem == "":
       goldgolem = "0"
    #<
     con = None
     try:
       con=L2DatabaseFactory.getInstance().getConnection()
       stm=con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE char_name=?")
       stm.setString(1,target)
       rsc=stm.executeQuery()
       if (rsc.next()):
         if rsc.getInt(1) == 0:
           htmltext = "<html><body>Перс с ником "+target+" не найден.<br>"
           htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
           self.closeCon(con)
           return htmltext
     except:
       pass 
     finally:
       self.closeRes(rsc)
       self.closeStat(stm)
    #< 
     name = str(st.getPlayer().getName())
     data = str(strftime("%Y-%m-%d", gmtime()))
     time = str(strftime("%H:%M:%S", gmtime()))
     uniq = IdFactory.getInstance().getNextId()
     if st.getQuestItemsCount(POSTMARK) < POSILKA:
       htmltext = "<html><body>К сожалению, у вас не хватает почтовых марок.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
       return htmltext
     itemObjId = st.getInt("sellitem")
     itemCount = st.getInt("lcount")
     myEnch = st.getInt("enchant")
    #>
     Item = st.getPlayer().getInventory().getItemByObjectId(itemObjId)
     itemTemplate = Item.getItem()
     if Item and Item.canBeEnchanted() and Item.isAugmented() and Item.getAugmentation().getAugmentSkill() != None and itemTemplate.getDuration() == -1 and not Item.isEquipped():
       itemId = Item.getItemId()
       itemName=str(itemTemplate.getName())
       count = str(itemCount)
       count = (itemCount > 1 and str(itemCount)) or str("")
       enchLvl=Item.getEnchantLevel()
       if enchLvl != myEnch:
         htmltext = "<html><body>К сожалению, у вас не хватает почтовых марок.<br>"
         htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
         return htmltext
       enchant = (enchLvl > 0 and " +"+str(enchLvl)+"") or str("")
       augment = Item.getAugmentation().getAugmentSkill()
       augEffId = Item.getAugmentation().getAugmentationId()
       augSkId,augSkLvl = str(augment.getId()),str(augment.getLevel())
      #>
       try:
         stm = con.prepareStatement("INSERT INTO `z_post_pos` (`id`,`tema`,`text`,`from`,`to`,`type`,`date`,`time`,`itemName`,`itemId`,`itemCount`,`itemEnch`,`augData`,`augSkill`,`augLvl`,`key`,`col`,`blueeva`,`goldgolem`) VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
         stm.setString(1, tema)
         stm.setString(2, text)
         stm.setString(3, name)
         stm.setString(4, target)
         stm.setInt(5, 0)
         stm.setString(6, data)
         stm.setString(7, time)
         stm.setString(8, itemName)
         stm.setInt(9, int(itemId))
         stm.setInt(10, 1)
         stm.setInt(11, int(enchLvl))
         stm.setInt(12, int(augEffId))
         stm.setInt(13, int(augSkId))
         stm.setInt(14, int(augSkLvl))
         stm.setString(15, key)
         stm.setString(16, col)
         stm.setString(17, blueeva)
         stm.setString(18, goldgolem)
         stm.execute()
         alarm = L2World.getInstance().getPlayer(sName)
         if alarm:
           alarm.sendMessage("Вам была отправлена посылка!")
           alarm.sendPacket(ExMailArrived())
           alarm.sendPacket(PlaySound("ItemSound.quest_finish"))
       except: 
         pass
       finally: 
         self.closeStat(stm)
         self.closeCon(con)
      #>
       st.takeItems(POSTMARK,POSILKA)
       player.destroyItem("q8782_ExpressPost",itemObjId, 1, player, 0)
       htmltext = "<html><body>Почтальон:<br>Вашa посылка <font color=LEVEL>"+str(count)+" "+itemName+""+str(enchant)+"</font> для "+target+" ("+tema+") отправлена.<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a>"
      #>
       alarm = L2World.getInstance().getPlayer(target)
       if alarm:
         alarm.sendPacket(SystemMessage.sendString("Вам была отправлена посылка!"))
         alarm.sendPacket(ExMailArrived())
         alarm.sendPacket(PlaySound("ItemSound.quest_finish"))
    #>
     else :
       htmltext = "<html><body>Этот предмет нельзя отправить!<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     return htmltext
   elif event == "in_pos":
     name = str(st.getPlayer().getName())
     htmltext = "<html><body>Почтальон:<br>"
     htmltext += "Входящие"
     htmltext += "<table width=300><tr><td></td><td></td><td></td></tr>"
     con=L2DatabaseFactory.getInstance().getConnection()
     inbox=con.prepareStatement("SELECT * FROM `z_post_pos` WHERE `to`=?  ORDER BY `date` DESC, `time` DESC LIMIT 0,?")
     inbox.setString(1, name)
     inbox.setInt(2, int(LIMIT))
     rs=inbox.executeQuery()
     while (rs.next()) :
       pid=rs.getInt("id")  
       tema=rs.getString("tema")
       sender=rs.getString("from")
       data=rs.getString("date")
       ptype=rs.getInt("type")
       time=rs.getString("time")
       try :
         cdata = str(strftime("%Y-%m-%d", gmtime()))
         if cdata == data:
           data = time[:5]
         if ptype == 1:
           htmltext += "<tr><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost inposshow_"+str(pid)+"\">"+str(tema)+"</a></td><td>"+str(data)+"</td></tr>"
         else:
           htmltext += "<tr><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost inposshow_"+str(pid)+"\"><font color=CC00FF>"+str(tema)+"</font></a></td><td>"+str(data)+"</td></tr>"
       except :
         try : inbox.close()
         except : pass
     try :
       con.close()
     except :
       pass
     htmltext += "</table><br><br>"
     cona=L2DatabaseFactory.getInstance().getConnection()
     getcount=cona.prepareStatement("SELECT COUNT(`to`) FROM `z_post_pos` WHERE `to`=?")
     getcount.setString(1,name)
     rsc=getcount.executeQuery()
     if (rsc.next()):
       rows  = rsc.getInt(1)
       vsego = int((rows/LIMIT)+1)
       if vsego > 1:
         htmltext += "Страницы: "
         st.set("stranic",str(vsego))
         prvsego = range(1,vsego)
         for i in prvsego:
           htmltext += ""
           htmltext += " <a action=\"bypass -h Quest q8782_ExpressPost str_3_" + str(i) +"\">"+str(i)+"</a><font color=>  </font>"
         else:
           htmltext += ""
     rsc.close()
     getcount.close()
     cona.close()
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a>"
     htmltext += "</body></html>"
     return htmltext
   elif event == "out_pos":
     name = str(st.getPlayer().getName())
     htmltext = "<html><body>Почтальон:<br>"
     htmltext += "Отправленные"
     htmltext += "<table width=300><tr><td>Кому</td><td>Тема</td><td>Дата</td></tr>"
     con=L2DatabaseFactory.getInstance().getConnection()
     inbox=con.prepareStatement("SELECT * FROM `z_post_pos` WHERE `from`=?  ORDER BY `date` DESC, `time` DESC LIMIT 0,?")
     inbox.setString(1, name)
     inbox.setInt(2, int(LIMIT))
     rs=inbox.executeQuery()
     while (rs.next()) :
       pid=rs.getInt("id")  
       tema=rs.getString("tema")
       sender=rs.getString("to")
       data=rs.getString("date")
       ptype=rs.getInt("type")
       aug=rs.getInt("augData")
       time=rs.getString("time")
       try :
         if len(tema) > 25:  
           tema = tema[:25]+"..."  
         cdata = str(strftime("%Y-%m-%d", gmtime()))
         if cdata == data:
           data = time[:5]
         if aug != 0:
           htmltext += "<tr><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost outposshow_"+str(pid)+"\">"+str(tema)+"</a></td><td>"+str(data)+"</td></tr>"
         else:
           htmltext += "<tr><td><font color=666666>"+str(sender)+"</td><td>"+str(tema)+"</td><td>"+str(data)+"</font></td></tr>"
       except :
         try : inbox.close()
         except : pass
     try :
       con.close()
     except :
       pass
     htmltext += "</table><br><br>"
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a>"
     htmltext += "</body></html>"
     return htmltext
   elif event.startswith("outposshow_"):
     sid = int(event.replace("outposshow_", ""))
     con=L2DatabaseFactory.getInstance().getConnection()
     show=con.prepareStatement("SELECT * FROM `z_post_pos` pos WHERE `id`=?")
     show.setInt(1, sid)
     rs=show.executeQuery()
     while (rs.next()) :
       pid=rs.getInt("id")
       tema=rs.getString("tema")
       text=rs.getString("text")
       sender=rs.getString("to")
       data=rs.getString("date")
       time=rs.getString("time")
       ptype=rs.getInt("type")
      #>
       itemName=rs.getString("itemName")
       itemId=rs.getInt("itemId")
       itemCount=rs.getInt("itemCount")
       itemEnch=rs.getInt("itemEnch")
       skill=rs.getInt("augSkill")
       attributes=rs.getInt("augData")
       try :
         item = ItemTable.getInstance().getTemplate(itemId)
         if item == None:
           htmltext = "<html><body>Посылка не найдена.<br>"
           htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in_pos\">Вернуться.</a></body></html>"
           return htmltext
         enchant = (itemEnch > 0 and " +"+str(itemEnch)+"") or str("")
         cdata = str(strftime("%d %m %Y", gmtime()))
         count = (itemCount > 1 and str(itemCount)) or str("")
         if cdata == data:
           pdata = time
         else:
           pdata = str(""+data+" ("+time+")")
         if skill != 0:
           pskill = SkillTable.getInstance().getInfo(skill, 1)
           skillname = pskill.getName()
         else:
           skillname = "нет."
         htmltext = "<html><body><table width=290><tr><td align=left>"+str(tema)+"</td><td align=right><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Посылки|x</a></td></tr></table><br>"
         htmltext += "<table width=300><tr><td align=left>Кому: "+str(sender)+"</td><td align=right>"+str(pdata)+"</td><td width=80 align=right><button value=\"Доб. контакт\" action=\"bypass -h Quest q8782_ExpressPost adrbook_ "+str(sender)+" _ +\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br>"
         htmltext += "<table width=300><tr><td width=32><img src=\"" + item.getIcon() + "\" width=32 height=32></td><td align=left><font color=LEVEL>"+str(count)+" "+str(itemName)+" "+str(enchant)+"</font></td></tr></table><br>"
         htmltext += "Аугмент: "+skillname+"<br>"
         htmltext += "<img src=\"sek.cbui355\" width=100 height=2><br>"
         htmltext += ""+str(text)+"<br><br><img src=\"sek.cbui355\" width=300 height=2><br><br>"
         htmltext += "<table width=280><tr><td align=left><button value=\"Забрать\" action=\"bypass -h Quest q8782_ExpressPost getoutpos_ "+str(pid)+"\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br>"
         htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost out_pos\">Назад.</a></body></html>"
       except :
         try : show.close()
         except : pass
     try :
       con.close()
     except :
       pass
     return htmltext
   elif event.startswith("inposshow_"):
     sid = int(event.replace("inposshow_", ""))
     con=L2DatabaseFactory.getInstance().getConnection()
     show=con.prepareStatement("SELECT * FROM `z_post_pos` pos WHERE `id`=?")
     show.setInt(1, sid)
     rs=show.executeQuery()
     while (rs.next()) :
       pid=rs.getInt("id")
       tema=rs.getString("tema")
       text=rs.getString("text")
       sender=rs.getString("from")
       data=rs.getString("date")
       time=rs.getString("time")
       ptype=rs.getInt("type")
      #>
       itemName=rs.getString("itemName")
       itemId=rs.getInt("itemId")
       itemCount=rs.getInt("itemCount")
       itemEnch=rs.getInt("itemEnch")
       skill=rs.getInt("augSkill")
       attributes=rs.getInt("augData")
       alvl=rs.getInt("augLvl")
       key=rs.getString("key")
       col=rs.getInt("col")
       blueeva=rs.getInt("blueeva")
       goldgolem=rs.getInt("goldgolem")
       try :
         item = ItemTable.getInstance().getTemplate(itemId)
         if item == None:
           htmltext = "<html><body>Посылка не найдена.<br>"
           htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in_pos\">Вернуться.</a></body></html>"
           return htmltext
         enchant = (itemEnch > 0 and " +"+str(itemEnch)+"") or str("")
         cdata = str(strftime("%d %m %Y", gmtime()))
         count = (itemCount > 1 and str(itemCount)) or str("")
         if cdata == data:
           pdata = time
         else:
           pdata = str(""+data+" ("+time+")")
         if skill > 0:
           pskill = SkillTable.getInstance().getInfo(skill, alvl)
           if pskill.isPassive():
             skilltype = "[ Passive:"+str(alvl)+"lvl ]"
           elif pskill.isChance():
             skilltype = "[ Chance:"+str(alvl)+"lvl ]"
           else:
             skilltype = "[ Active:"+str(alvl)+"lvl ]"
             if pskill.getPower() > 0:
               skilltype += " {power: "+str(pskill.getPower())+"}"
           skillname = pskill.getName()+" "+skilltype
           skillname = skillname.replace("Item Skill: ","")
         else:
           skillname = "НЕТ АУГМЕНТА! Честно-Честно!"
         htmltext = "<html><body><table width=290><tr><td align=left>"+str(tema)+"</td><td align=right><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Посылки|x</a></td></tr></table><br>"
         htmltext += "<table width=300><tr><td align=left>От: <font color=FFCC33>"+str(sender)+"</font></td><td align=right>"+str(pdata)+"</td><td width=80 align=right><button value=\"Доб. контакт\" action=\"bypass -h Quest q8782_ExpressPost adrbook_ "+str(sender)+" _ +\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br>"
         htmltext += "<table width=300><tr><td width=32><img src=\"" + item.getIcon() + "\" width=32 height=32></td><td align=left><font color=LEVEL>"+str(count)+" "+str(itemName)+" "+str(enchant)+"</font></td></tr></table><br>"
         htmltext += "<br><font color=FF0099>Аугмент: "+skillname+"</font><br>"
         htmltext += "<img src=\"sek.cbui355\" width=100 height=2><br>"
         htmltext += ""+str(text)+"<br><br><img src=\"sek.cbui355\" width=300 height=2><br><br>"
         ccol,ceva,cgolem=0,0,0
         if col > 0:
           if st.getQuestItemsCount(4037) < col:
             colorcol = "<font color=ff2a00>"+str(col)+"</font>"
             ccol = 20
           else:
             colorcol = "<font color=7fff00>"+str(col)+"</font>"
             ccol = 1
           htmltext += "(Защита) Coin Of Luck: "+str(colorcol)+"<br>"
         if blueeva > 0:
           if st.getQuestItemsCount(4355) < blueeva:
             coloreva = "<font color=ff2a00>"+str(blueeva)+"</font>"
             ceva = 20
           else:
             coloreva = "<font color=7fff00>"+str(blueeva)+"</font>"
             ceva = 1
           htmltext += "(Защита) Blue Eva: "+str(coloreva)+"<br>"
         if goldgolem > 0:
           if st.getQuestItemsCount(5962) < goldgolem:
             colorgolem = "<font color=ff2a00>"+str(goldgolem)+"</font>"
             cgolem = 20
           else:
             colorgolem = "<font color=7fff00>"+str(goldgolem)+"</font>"
             cgolem = 1
           htmltext += "(Защита) Gold Golem: "+str(colorgolem)+"<br>"
         fullchk = ccol + ceva + cgolem
         if (col != "" or blueeva != "" or goldgolem != "") and fullchk <= 3:
           if key == "0" or key == "":
             htmltext += "<table width=280><tr><td align=left><button value=\"Забрать\" action=\"bypass -h Quest q8782_ExpressPost getpos_ "+str(pid)+" _ hej4kol\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br>"
           else:
             htmltext += "Ключ: <edit var=\"key\" width=200 length=\"16\"><br>"
             htmltext += "<table width=280><tr><td align=left><button value=\"Забрать\" action=\"bypass -h Quest q8782_ExpressPost getpos_ "+str(pid)+" _ $key\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br>"
         htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in_pos\">Назад.</a></body></html>"
        #>
         if ptype == 0:
           incon=L2DatabaseFactory.getInstance().getConnection()
           updatein=incon.prepareStatement("UPDATE z_post_pos SET type=1 WHERE id=?")
           updatein.setInt(1, pid)
           try :
             updatein.executeUpdate()
             updatein.close()
             incon.close()
           except :
             try : incon.close()
             except : pass
       except :
         try : show.close()
         except : pass
     try :
       con.close()
     except :
       pass
     return htmltext
   elif event.startswith("getpos_ "):
     search = str(event.replace("getpos_ ", ""))
     pid,key=search.split("_")
     pid,key=str(pid),str(key)
    #<
     pid = str(pid.replace(" ", ""))
     key = str(key.replace(" ", ""))
     if key == "":
       htmltext = "<html><body>Вы не указали кодовое слово.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in_pos\">Вернуться.</a></body></html>"
       return htmltext
    #<
     pid,key=int(pid),str(key)
    #<
     cona=L2DatabaseFactory.getInstance().getConnection()
     getcount=cona.prepareStatement("SELECT COUNT(id) FROM z_post_pos WHERE id=?")
     getcount.setInt(1, pid)
     rsc=getcount.executeQuery()
     if (rsc.next()):
       if rsc.getInt(1) == 0:
         htmltext = "<html><body>Посылка не найдена или автор отозвал посылку!<br><br><a action=\"bypass -h Quest q8782_ExpressPost in_pos\">Вернуться.</a>"
         htmltext += "</body></html>"
         return htmltext
     rsc.close()
     getcount.close()
     cona.close()
    #< 
     con=L2DatabaseFactory.getInstance().getConnection()
     show=con.prepareStatement("SELECT * FROM `z_post_pos` pos WHERE `id`=?")
     show.setInt(1, pid)
     rs=show.executeQuery()
     while (rs.next()) :
       pid=rs.getInt("id")
       tema=rs.getString("tema")
       text=rs.getString("text")
       sender=rs.getString("from")
       data=rs.getString("date")
       time=rs.getString("time")
       ptype=rs.getInt("type")
      #>
       itemName=rs.getString("itemName")
       itemId=rs.getInt("itemId")
       itemCount=rs.getInt("itemCount")
       itemEnch=rs.getInt("itemEnch")
       skill=rs.getInt("augSkill")
       attributes=rs.getInt("augData")
       alvl=rs.getInt("augLvl")
       skey=rs.getString("key")
       scol=rs.getInt("col")
       seva=rs.getInt("blueeva")
       sgolem=rs.getInt("goldgolem")
       try :
         if key != "hej4kol" and key != skey:
           htmltext = "<html><body>Вы ввели непpарильное ключевое слово.<br>"
           htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in_pos\">Вернуться.</a></body></html>"
           return htmltext
         if scol > 0:
           if st.getQuestItemsCount(4037) < scol:
             htmltext = "(Защита) Coin Of Luck: <font color=ff2a00>"+str(scol)+"</font><br>"
             return htmltext
           else:
             st.takeItems(4037,scol)
             name = str(st.getPlayer().getName())
             data = str(strftime("%Y-%m-%d", gmtime()))
             time = str(strftime("%H:%M:%S", gmtime()))
             uniq = IdFactory.getInstance().getNextId()
             tema = "Оплата "+itemName+": "+str(scol)+" Coin Of Luck"
             ins=L2DatabaseFactory.getInstance().getConnection()
             sendin=ins.prepareStatement("INSERT INTO z_post_pos VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)") 
             sendin.setInt(1, uniq)
             sendin.setString(2, tema)
             sendin.setString(3, tema)
             sendin.setString(4, name)
             sendin.setString(5, sender)
             sendin.setInt(6, int(0))
             sendin.setString(7, data)
             sendin.setString(8, time)
             sendin.setString(9, "Coin Of Luck")
             sendin.setInt(10, 4037)
             sendin.setInt(11, scol)
             sendin.setInt(12, 0)
             sendin.setInt(13, 0)
             sendin.setInt(14, 0)
             sendin.setInt(15, 0)
             sendin.setString(16, "")
             sendin.setString(17, "")
             sendin.setString(18, "")
             sendin.setString(19, "")
             try :
               sendin.executeUpdate()
               sendin.close()
               ins.close()
             except :
               try : ins.close()
               except : pass
            #<
             alarm = L2World.getInstance().getPlayer(sender)
             if alarm:
               alarm.sendPacket(SystemMessage.sendString("Пришла оплата "+str(scol)+" Coin Of Luck за "+itemName+" от "+str(name)+"!"))
               alarm.sendPacket(ExMailArrived())
               alarm.sendPacket(PlaySound("ItemSound.quest_finish"))         
         if seva > 0:
           if st.getQuestItemsCount(4355) < seva:
             htmltext = "(Защита) Blue Eva: <font color=ff2a00>"+str(seva)+"</font><br>"
             return htmltext
           else:
             st.takeItems(4355,seva)
             name = str(st.getPlayer().getName())
             data = str(strftime("%Y-%m-%d", gmtime()))
             time = str(strftime("%H:%M:%S", gmtime()))
             uniq = IdFactory.getInstance().getNextId()
             tema = "Оплата "+itemName+": "+str(seva)+" Blue Eva"
             ins=L2DatabaseFactory.getInstance().getConnection()
             sendin=ins.prepareStatement("INSERT INTO z_post_pos VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)") 
             sendin.setInt(1, uniq)
             sendin.setString(2, tema)
             sendin.setString(3, tema)
             sendin.setString(4, name)
             sendin.setString(5, sender)
             sendin.setInt(6, int(0))
             sendin.setString(7, data)
             sendin.setString(8, time)
             sendin.setString(9, "Blue Eva")
             sendin.setInt(10, 4355)
             sendin.setInt(11, seva)
             sendin.setInt(12, 0)
             sendin.setInt(13, 0)
             sendin.setInt(14, 0)
             sendin.setInt(15, 0)
             sendin.setString(16, "")
             sendin.setString(17, "")
             sendin.setString(18, "")
             sendin.setString(19, "")
             try :
               sendin.executeUpdate()
               sendin.close()
               ins.close()
             except :
               try : ins.close()
               except : pass
            #<
             alarm = L2World.getInstance().getPlayer(sender)
             if alarm:
               alarm.sendPacket(SystemMessage.sendString("Пришла оплата "+str(seva)+" Blue Eva за "+itemName+" от "+str(name)+"!"))
               alarm.sendPacket(ExMailArrived())
               alarm.sendPacket(PlaySound("ItemSound.quest_finish"))
         if sgolem > 0:
           if st.getQuestItemsCount(5962) < sgolem:
             htmltext = "(Защита) Gold Golem: <font color=ff2a00>"+str(sgolem)+"</font><br>"
             return htmltext
           else:
             st.takeItems(5962,sgolem)
             name = str(st.getPlayer().getName())
             data = str(strftime("%Y-%m-%d", gmtime()))
             time = str(strftime("%H:%M:%S", gmtime()))
             uniq = IdFactory.getInstance().getNextId()
             tema = "Оплата "+itemName+": "+str(sgolem)+" Gold Golem"
             ins=L2DatabaseFactory.getInstance().getConnection()
             sendin=ins.prepareStatement("INSERT INTO z_post_pos VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)") 
             sendin.setInt(1, uniq)
             sendin.setString(2, tema)
             sendin.setString(3, tema)
             sendin.setString(4, name)
             sendin.setString(5, sender)
             sendin.setInt(6, int(0))
             sendin.setString(7, data)
             sendin.setString(8, time)
             sendin.setString(9, "Gold Golem")
             sendin.setInt(10, 5962)
             sendin.setInt(11, sgolem)
             sendin.setInt(12, 0)
             sendin.setInt(13, 0)
             sendin.setInt(14, 0)
             sendin.setInt(15, 0)
             sendin.setString(16, "")
             sendin.setString(17, "")
             sendin.setString(18, "")
             sendin.setString(19, "")
             try :
               sendin.executeUpdate()
               sendin.close()
               ins.close()
             except :
               try : ins.close()
               except : pass
            #<
             alarm = L2World.getInstance().getPlayer(sender)
             if alarm:
               alarm.sendPacket(SystemMessage.sendString("Пришла оплата "+str(sgolem)+" Gold Golem за "+itemName+" от "+str(name)+"!"))
               alarm.sendPacket(ExMailArrived())
               alarm.sendPacket(PlaySound("ItemSound.quest_finish"))
         item = player.getInventory().addItem("Quest", itemId, itemCount, player, player.getTarget())
         if itemEnch != 0:
           item.setEnchantLevel(itemEnch)
         if skill != 0:
           item.setAugmentation(L2Augmentation(item, attributes, skill, alvl, True))
         smsg = SystemMessage(SystemMessageId.EARNED_S2_S1_S)
         smsg.addItemName(itemId)
         smsg.addNumber(itemCount)
         player.sendPacket(smsg)
         player.sendPacket(ItemList(player, False))
         statusUpdate = StatusUpdate(player.getObjectId())
         statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad())
         player.sendPacket(statusUpdate)
       except :
         try : show.close()
         except : pass
     try :
       con.close()
     except :
       pass
    #>
     con=L2DatabaseFactory.getInstance().getConnection()
     zabiraem=con.prepareStatement("DELETE FROM z_post_pos WHERE id=?")
     zabiraem.setInt(1, pid)
     try :
       zabiraem.executeUpdate()
       zabiraem.close()
       con.close()
     except :
       try : con.close()
       except : pass
    #>   
     htmltext = "<html><body>Получите - распишитесь.<br><br><a action=\"bypass -h Quest q8782_ExpressPost in_pos\">Вернуться.</a>"
     htmltext += "</body></html>"
     return htmltext
   elif event.startswith("getoutpos_ "):
     pid = int(event.replace("getoutpos_ ", ""))
    #>
     cona=L2DatabaseFactory.getInstance().getConnection()
     getcount=cona.prepareStatement("SELECT COUNT(id) FROM z_post_pos WHERE id=?")
     getcount.setInt(1, pid)
     rsc=getcount.executeQuery()
     if (rsc.next()):
       if rsc.getInt(1) == 0:
         htmltext = "<html><body>Посылка не найдена!<br><br><a action=\"bypass -h Quest q8782_ExpressPost out_pos\">Вернуться.</a>"
         htmltext += "</body></html>"
         return htmltext
     rsc.close()
     getcount.close()
     cona.close()
    #> 
     con=L2DatabaseFactory.getInstance().getConnection()
     show=con.prepareStatement("SELECT * FROM `z_post_pos` pos WHERE `id`=?")
     show.setInt(1, pid)
     rs=show.executeQuery()
     while (rs.next()) :
       pid=rs.getInt("id")
       tema=rs.getString("tema")
       text=rs.getString("text")
       sender=rs.getString("from")
       data=rs.getString("date")
       time=rs.getString("time")
       ptype=rs.getInt("type")
      #>
       itemName=rs.getString("itemName")
       itemId=rs.getInt("itemId")
       itemCount=rs.getInt("itemCount")
       itemEnch=rs.getInt("itemEnch")
       skill=rs.getInt("augSkill")
       attributes=rs.getInt("augData")
       try :
         item = player.getInventory().addItem("Quest", itemId, itemCount, player, player.getTarget())
         if itemEnch != 0:
           item.setEnchantLevel(itemEnch)
         if skill != 0:
           item.setAugmentation(L2Augmentation(item, attributes, skill, 10, True))
         smsg = SystemMessage(SystemMessageId.EARNED_S2_S1_S)
         smsg.addItemName(itemId)
         smsg.addNumber(itemCount)
         player.sendPacket(smsg)
         player.sendPacket(ItemList(player, False))
         statusUpdate = StatusUpdate(player.getObjectId())
         statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad())
         player.sendPacket(statusUpdate)
       except :
         try : show.close()
         except : pass
     try :
       con.close()
     except :
       pass
    #>
     con=L2DatabaseFactory.getInstance().getConnection()
     zabiraem=con.prepareStatement("DELETE FROM z_post_pos WHERE id=?")
     zabiraem.setInt(1, pid)
     try :
       zabiraem.executeUpdate()
       zabiraem.close()
       con.close()
     except :
       try : con.close()
       except : pass
    #>   
     htmltext = "<html><body>Получите - распишитесь.<br><br><a action=\"bypass -h Quest q8782_ExpressPost out_pos\">Вернуться.</a>"
     htmltext += "</body></html>"
     return htmltext
   elif event.startswith("friend_"):
     ptype = int(event.replace("friend_", ""))
     ownerId = str(st.getPlayer().getObjectId())
     htmltext = "<html><body>Почтальон:<br>"
     htmltext += "Список друзей:"
     htmltext += "<table width=300><tr><td></td></tr>"
     con=L2DatabaseFactory.getInstance().getConnection()
     inbox=con.prepareStatement("SELECT * FROM `character_friends` WHERE `char_id`=?  ORDER BY `friend_name` LIMIT 0,?")
     inbox.setString(1, ownerId)
     inbox.setInt(2, int(LIMIT))
     rs=inbox.executeQuery()
     while (rs.next()) :
       target=rs.getString("friend_name")  
       try :
         if ptype == 1:
           htmltext += "<tr><td><a action=\"bypass -h Quest q8782_ExpressPost fpform_"+str(target)+"\">"+str(target)+"</a></td></tr>"
         else:
           htmltext += "<tr><td><a action=\"bypass -h Quest q8782_ExpressPost fposform_"+str(target)+"\">"+str(target)+"</a></td></tr>"      
       except :
         try : inbox.close()
         except : pass
     try :
       con.close()
     except :
       pass
     htmltext += "</table><br><br>"
     cona=L2DatabaseFactory.getInstance().getConnection()
     getcount=cona.prepareStatement("SELECT COUNT(`char_id`) FROM `character_friends` WHERE `char_id`=?")
     getcount.setString(1,ownerId)
     rsc=getcount.executeQuery()
     if (rsc.next()):
       rows  = rsc.getInt(1)
       vsego = int((rows/LIMIT)+1)
       if vsego > 2:
         htmltext += "Страницы: "
         st.set("stranic",str(vsego))
         prvsego = range(1,vsego)
         for i in prvsego:
           htmltext += ""
           htmltext += " <a action=\"bypass -h Quest q8782_ExpressPost str_4_" + str(i) +"\">"+str(i)+"</a><font color=>  </font>"
         else:
           htmltext += ""
     rsc.close()
     getcount.close()
     cona.close()
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost home\">Вернуться.</a>"
     htmltext += "</body></html>"
     return htmltext
   elif event.startswith("clan_"):
     ptype = int(event.replace("clan_", ""))
     clanId = str(player.getClanId())
     if not player.getClanId():
       htmltext = "<html><body>Почтальон:<br>Вы не в клане!<br><a action=\"bypass -h Quest q8782_ExpressPost home\">Вернуться.</a>"
       htmltext += "</body></html>"
       return htmltext
     htmltext = "<html><body>Почтальон:<br>"
     htmltext += "Список сокланов:"
     htmltext += "<table width=300><tr><td></td></tr>"
     con=L2DatabaseFactory.getInstance().getConnection()
     inbox=con.prepareStatement("SELECT * FROM `characters` WHERE `clanid`=?  ORDER BY `clanid` LIMIT 0,?")
     inbox.setString(1, clanId)
     inbox.setInt(2, int(LIMIT))
     rs=inbox.executeQuery()
     while (rs.next()) :
       target=rs.getString("char_name")  
       try :
         if ptype == 1:
           htmltext += "<tr><td><a action=\"bypass -h Quest q8782_ExpressPost fpform_"+str(target)+"\">"+str(target)+"</a></td></tr>"
         else:
           htmltext += "<tr><td><a action=\"bypass -h Quest q8782_ExpressPost fposform_"+str(target)+"\">"+str(target)+"</a></td></tr>"      
       except :
         try : inbox.close()
         except : pass
     try :
       con.close()
     except :
       pass
     htmltext += "</table><br><br>"
     cona=L2DatabaseFactory.getInstance().getConnection()
     getcount=cona.prepareStatement("SELECT COUNT(`clanid`) FROM `characters` WHERE `clanid`=?")
     getcount.setString(1,clanId)
     rsc=getcount.executeQuery()
     if (rsc.next()):
       rows  = rsc.getInt(1)
       vsego = int((rows/LIMIT)+1)
       if vsego > 2:
         htmltext += "Страницы: "
         st.set("stranic",str(vsego))
         prvsego = range(1,vsego)
         for i in prvsego:
           htmltext += ""
           htmltext += " <a action=\"bypass -h Quest q8782_ExpressPost str_5_" + str(i) +"\">"+str(i)+"</a><font color=>  </font>"
         else:
           htmltext += ""
     rsc.close()
     getcount.close()
     cona.close()
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost home\">Вернуться.</a>"
     htmltext += "</body></html>"
     return htmltext
   elif event.startswith("fpform_"):
     target = str(event.replace("fpform_", ""))
     if st.getQuestItemsCount(POSTMARK) < PISMO:
       htmltext = "<html><body>К сожалению, у вас не хватает почтовых марок.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a></body></html>"
       return htmltext
     htmltext = "<html><body>Кому: "+target+"<br>"
     htmltext += "Тема:<br>"
     htmltext += "<edit var=\"tema\" width=200 length=\"16\">"
     htmltext += "Текст:"
     htmltext += "<multiedit var=\"text\" width=280 height=70><br>"
     htmltext += "<button value=\"Отправить\" action=\"bypass -h Quest q8782_ExpressPost send_ "+target+" _ $tema _ $text\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("fposform_"):
     target = str(event.replace("fposform_", ""))
     if st.getQuestItemsCount(POSTMARK) < POSILKA:
       htmltext = "<html><body>К сожалению, у вас не хватает почтовых марок.<br>"
       htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a></body></html>"
       return htmltext
     itemObjId = st.getInt("sellitem")
     itemCount = st.getInt("lcount")
     Item = st.getPlayer().getInventory().getItemByObjectId(itemObjId)
     itemTemplate = Item.getItem()
     if Item and itemTemplate.getDuration() == -1 and not Item.isEquipped():
       count = str(itemCount)
       grade = itemTemplate.getCrystalType()
       igrade = str(itemTemplate.getCrystalType())
       itype = str(Item.getItemType())
       idtest = Item.getItemId()
       grades = {1: "d", 2: "c", 3: "b", 4: "a", 5: "s"}
       pgrade = grades.get(grade, str(""))
       enchant = (Item.getEnchantLevel() > 0 and " +"+str(Item.getEnchantLevel())+"") or str("")
       htmltext =  "<html><body>Почтальон:<br>Посылка:<br>"
       htmltext += "<table width=300><tr><td><img src=\"" + itemTemplate.getIcon() + "\" width=32 height=32></td><td><font color=LEVEL>" + itemTemplate.getName() + " " + enchant + "</font><img src=\"symbol.grade_"+str(pgrade)+"\" width=16 height=16><br></td></tr></table><br><br>"
       htmltext += "Количество: "+count+"<br>"
       htmltext += "Получатель: "+target+"<br>"
       htmltext += "Тема:<br>"
       htmltext += "<edit var=\"tema\" width=200 length=\"16\">"
       htmltext += "Текст:"
       htmltext += "<multiedit var=\"text\" width=280 height=70><br>"
       htmltext += "<br><br>Дополнительно***:<br>"
       htmltext += "Ключевое слово:<br>"
       htmltext += "<edit var=\"key\" width=200 length=\"16\">"
       htmltext += "Coin of Luck:<br>"
       htmltext += "<edit var=\"col\" width=200 length=\"16\">"
       htmltext += "Blue Eva:<br>"
       htmltext += "<edit var=\"blueeva\" width=200 length=\"16\">"
       htmltext += "Gold Golem:<br>"
       htmltext += "<edit var=\"goldgolem\" width=200 length=\"16\"><br><br>"
       htmltext += "<button value=\"Отправить\" action=\"bypass -h Quest q8782_ExpressPost possend_ "+target+" _ $tema _ $text _ $key _ $col _ $blueeva _ $goldgolem\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
       htmltext += "<br><br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a><br><br>"
       htmltext += "*** указываем, сколько получатель должен заплатить за посылку или ключевое слово.</body></html>"
     else :
       htmltext = "<html><body>Этот предмет нельзя отправить!<br><br><a action=\"bypass -h Quest q8782_ExpressPost posilki\">Вернуться.</a></body></html>"
     return htmltext
   elif event.startswith("find_ "):
     search = str(event.replace("find_ ", ""))
     search,ptype=search.split("_")
     search,ptype=str(search),str(ptype)
     search = str(search.replace(" ", ""))
     ptype = str(ptype.replace(" ", ""))
     if search == "":
       htmltext = "<html><body>Задан пустой поисковый запрос<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a></body></html>"
       return htmltext
     name = str(st.getPlayer().getName())
     htmltext = "<html><body>Поиск по "+ptype+":"+search+"<br>"
     htmltext += "<table width=300><tr><td></td><td>Автор</td><td>Тема</td><td>Дата</td></tr>"
     con=L2DatabaseFactory.getInstance().getConnection()
     if ptype == "Автор":
       sql=con.prepareStatement("SELECT * FROM `z_post_in` WHERE `from`=? AND `to`=?  ORDER BY `date` DESC, `time` DESC LIMIT 0,?")
     else:
       sql=con.prepareStatement("SELECT * FROM `z_post_in` WHERE `tema`=? AND `to`=?  ORDER BY `date` DESC, `time` DESC LIMIT 0,?")  
     inbox = sql
     inbox.setString(1, search)
     inbox.setString(2, name)
     inbox.setInt(3, int(LIMIT))
     rs=inbox.executeQuery()
     while (rs.next()) :
       pid=rs.getInt("id")  
       tema=rs.getString("tema")
       sender=rs.getString("from")
       data=rs.getString("date")
       ptype=rs.getInt("type")
       time=rs.getString("time")
       try :
         cdata = str(strftime("%d %m %Y", gmtime()))
         if cdata == data:
           data = time
         if ptype == 1:
           htmltext += "<tr><td width=16><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost inshow_"+str(pid)+"\">"+str(tema)+"</a></td><td>"+str(data)+"</td></tr>"
         else:
           htmltext += "<tr><td width=16><img src=\"Icon.etc_letter_envelope_i00\" width=16 height=16></td><td>"+str(sender)+"</td><td><a action=\"bypass -h Quest q8782_ExpressPost inshow_"+str(pid)+"\"><font color=CC00FF>"+str(tema)+"</font></a></td><td>"+str(data)+"</td></tr>"
       except :
         try : inbox.close()
         except : pass
     try :
       con.close()
     except :
       pass
     htmltext += "</table><br><br>"
     cona=L2DatabaseFactory.getInstance().getConnection()
     if ptype == "Автор":
       sqll=cona.prepareStatement("SELECT COUNT(`from`) FROM `z_post_in` WHERE `from`=? AND `to`=?")
     else:
       sqll=cona.prepareStatement("SELECT COUNT(`tema`) FROM `z_post_in` WHERE `tema`=? AND `to`=?")
     getcount = sqll
     getcount.setString(1, search)
     getcount.setString(2, name)
     rsc=getcount.executeQuery()
     if (rsc.next()):
       rows  = rsc.getInt(1)
       vsego = int((rows/LIMIT)+1)
       if vsego > 2:
         htmltext += "Страницы: "
         st.set("stranic",str(vsego))
         prvsego = range(1,vsego)
         for i in prvsego:
           htmltext += ""
           htmltext += " <a action=\"bypass -h Quest q8782_ExpressPost str_1_" + str(i) +"\">"+str(i)+"</a><font color=>  </font>"
         else:
           htmltext += ""
     rsc.close()
     getcount.close()
     cona.close()
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a>"
     htmltext += "</body></html>"
     return htmltext
   elif event == "adrbook":
     name = str(st.getPlayer().getName())
     htmltext = "<html><body>Почта:<table width=260 border=0><tr><td align=right>Контакт:</td><td align=right><edit var=\"nick\" width=90 length=\"16\"></td><td align=right><combobox width=30 var=act list=\"+;-\"></td><td><button value=\"Ок\" action=\"bypass -h Quest q8782_ExpressPost adrbook_ $nick _ $act\" width=30 height=17 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>"
     htmltext += "<br>Адресная книга"
     htmltext += "<table width=300><tr><td></td></tr>"
     con=L2DatabaseFactory.getInstance().getConnection()
     inbox=con.prepareStatement("SELECT * FROM `z_post_adrbook` WHERE `name`=?  ORDER BY `friend` LIMIT 0,?")
     inbox.setString(1, name)
     inbox.setInt(2, int(LIMIT))
     rs=inbox.executeQuery()
     while (rs.next()) :
       target=rs.getString("friend")  
       try :
         htmltext += "<tr><td><a action=\"bypass -h Quest q8782_ExpressPost fpform_"+str(target)+"\">"+str(target)+"</a></td></tr>"    
       except :
         try : inbox.close()
         except : pass
     try :
       con.close()
     except :
       pass
     htmltext += "</table><br><br>"
     cona=L2DatabaseFactory.getInstance().getConnection()
     getcount=cona.prepareStatement("SELECT COUNT(`name`) FROM `z_post_adrbook` WHERE `name`=?")
     getcount.setString(1,name)
     rsc=getcount.executeQuery()
     if (rsc.next()):
       rows  = rsc.getInt(1)
       vsego = int((rows/LIMIT)+1)
       if vsego > 2:
         htmltext += "Страницы: "
         st.set("stranic",str(vsego))
         prvsego = range(1,vsego)
         for i in prvsego:
           htmltext += ""
           htmltext += " <a action=\"bypass -h Quest q8782_ExpressPost str_6_" + str(i) +"\">"+str(i)+"</a><font color=>  </font>"
         else:
           htmltext += ""
     rsc.close()
     getcount.close()
     cona.close()
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost in\">Вернуться.</a>"
     htmltext += "</body></html>"
     return htmltext
   elif event.startswith("adrbook_ "):
     search = str(event.replace("adrbook_ ", ""))
     nick,act=search.split(" _ ")
     nick,act=str(nick),str(act)
     name = str(st.getPlayer().getName())
     htmltext = "<html><body>Управление адресной книгой:<br>"
     if act == "+":
       ins=L2DatabaseFactory.getInstance().getConnection()
       sendin=ins.prepareStatement("INSERT INTO z_post_adrbook VALUES (?,?)") 
       sendin.setString(1, name)
       sendin.setString(2, nick)
       try :
         sendin.executeUpdate()
         sendin.close()
         ins.close()
       except :
         try : ins.close()
         except : pass
       htmltext += ""+nick+" добавлен в адресную книгу."
     else:
       con=L2DatabaseFactory.getInstance().getConnection()
       delete=con.prepareStatement("DELETE FROM z_post_adrbook WHERE name=? AND friend=?")
       delete.setString(1, name)
       delete.setString(2, nick)
       try :
         delete.executeUpdate()
         delete.close()
         con.close()
       except :
         try : con.close()
         except : pass
       htmltext += ""+nick+" удален из адресной книги."
    #>
     htmltext += "<br><br><a action=\"bypass -h Quest q8782_ExpressPost adrbook\">Вернуться.</a>"
     htmltext += "</body></html>"
     return htmltext
   return
 
 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   if npcId == POSTMAN:
     self.startQuestTimer("home",100,None,player)       
   return

QUEST       = Quest(8782,qn,"custom")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(POSTMAN)
QUEST.addTalkId(POSTMAN)
