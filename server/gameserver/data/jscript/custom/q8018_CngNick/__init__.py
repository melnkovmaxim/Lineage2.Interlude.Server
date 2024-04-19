import sys
from ru.agecold import L2DatabaseFactory
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.gameserver.util import Util
from time import gmtime, strftime

qn = "q8018_CngNick"

NPC = 99999
ITEM = 12222 # итем необходимый для обмена
COST = 10 # стоимость
INAME = "Донат Коин" # название итема

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
   
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
   
 def error(self,action, text):
   return "<html><body> " + action + ": <br> " + text + "</body></html>"
   
 def existsNick(self,nick):
   try:
     con=L2DatabaseFactory.getInstance().getConnection()
     st=con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE char_name=? LIMIT 1")
     st.setString(1,nick)
     rs=st.executeQuery()
     if (rs.next()):
       if rs.getInt(1) == 1:
         return True
   except:
     return True
   finally:
     self.closeRes(rs)
     self.closeStat(st)
     self.closeCon(con)
   return False

 def onAdvEvent (self,event,npc,player):
   st = player.getQuestState(qn)
   if event.startswith("step2_"):
     nick = str(event.replace("step2_", ""))
     nick = str(nick.replace(" ", ""))
     htmltext = "<html><body>Смена ника:<br>Проверка...<br><br>"
     if nick == "":
       return self.error("Шаг 2","Вы не ввели желаемый ник!")
     if not Util.isValidName(player, nick):
       return self.error("Шаг 2","Данный ник не может быть использован.")
    #<
     if self.existsNick(nick):
       return self.error("Шаг 2","Данный ник уже занят.")
    #< 
     htmltext += "<font color=66CC33>Ник "+nick+" свободен.</font><br>"
     htmltext += "<a action=\"bypass -h Quest q8018_CngNick step3_"+nick+"\" msg=\"Новый ник "+nick+". Уверены?\">Продолжить.</a></body></html>"
   elif event.startswith("step3"):
     nick = str(event.replace("step3_", ""))
     if st.getQuestItemsCount(ITEM) < COST:
       return self.error("Шаг 3","Смена ника: <font color=74bff5>"+str(COST)+" "+INAME+"")
    #<
     if self.existsNick(nick):
       return self.error("Шаг 2","Данный ник уже занят.")
    #<
     login = str(player.getAccountName()) 
     name = str(player.getName())
     date = str(strftime("%Y-%m-%d", gmtime()))
     time = str(strftime("%H:%M:%S", gmtime()))
     try:
       con=L2DatabaseFactory.getInstance().getConnection()
       st=con.prepareStatement("INSERT INTO zz_donate_log (date,time,login,name,action,payment) VALUES (?,?,?,?,?,?)")
       st.setString(1, date)
       st.setString(2, time)
       st.setString(3, login)
       st.setString(4, name)
       st.setString(5, "Nick: "+nick+"")
       st.setInt(6, COST)
       st.executeUpdate()
     except:
       return self.error("Шаг 3","Ошибка базы данных")
     finally:
       self.closeStat(st)
       self.closeCon(con)
    #<
     player.destroyItemByItemId("q8018_CngNick", ITEM, COST, player, True)
     player.changeName(nick)
    #st.takeItems(ITEM,COST)
     htmltext =  "<html><body>Смена ника:<br>Готово!<br>Надеемся вы довольны новым ником.</body></html>"
   else:
     htmltext = "<html><body>Смена ника:<br>Oops!</body></html>"
   return htmltext

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   if npcId == NPC:
     if st.getQuestItemsCount(ITEM) < COST:
       return self.error("Шаг 1","Смена ника: <font color=74bff5>"+str(COST)+" "+INAME+"")
     htmltext = "<html><body><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><img src=\"L2UI.SquareBlank\" width=260 height=2><br1>"       
     htmltext += "Введите желаемый ник:<br><br>"
     htmltext += "Допустимые символы - [~!@#%^*().,;/|\?+<>:'_-] (Русские ники можно использовать)"
     htmltext += "<edit var=\"nick\" width=200 length=\"16\"><br>"
     htmltext += "<button value=\"Проверить\" action=\"bypass -h Quest q8018_CngNick step2_ $nick\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br><br>"
     htmltext += "Смена ника: <font color=74bff5>"+str(COST)+" "+INAME+"</font><br>"
     htmltext += "<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></body></html>" 
     return htmltext
   return

QUEST       = Quest(8018,qn,"custom")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(NPC)
QUEST.addTalkId(NPC)
