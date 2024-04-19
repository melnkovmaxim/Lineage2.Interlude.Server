import sys
from java.lang import System
from ru.agecold.gameserver.model.quest        import State
from ru.agecold.gameserver.model.quest        import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold import L2DatabaseFactory

qn = "q2000_NoblesseTeleport"
NPC=[30006,30059,30080,30134,30146,30177,30233,30256,30320,30540,30576,30836,30848,30878,30899,31275,31320,31964]

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onAdvEvent (self,event,npc,player):
   st = player.getQuestState(qn)
   if event.startswith("gatepass_"):
     if st.getQuestItemsCount(57) >= 100000:
       tp = int(event.replace("gatepass_", ""))
       con=L2DatabaseFactory.getInstance().getConnection()
       gettp=con.prepareStatement("SELECT * FROM `teleport` WHERE id=?")
       gettp.setInt(1, tp)
       rs=gettp.executeQuery()
       while (rs.next()) :
         x=rs.getInt("loc_x")
         y=rs.getInt("loc_y")
         z=rs.getInt("loc_z")
         try :
           st.takeItems(57,1)
           player.teleToLocation(int(x),int(y),int(z))
         except :
           try : gettp.close()
           except : pass
       try :
         con.close()
       except :
         pass
     else:
       htmltext = "<html><body>Стоимость ТП = 100000 Adena</body></html>"
       return htmltext
   elif event.startswith("adena_"):
     if st.getQuestItemsCount(57) >= 100000:
       tp = int(event.replace("adena_", ""))
       con=L2DatabaseFactory.getInstance().getConnection()
       gettp=con.prepareStatement("SELECT * FROM `teleport` WHERE id=?")
       gettp.setInt(1, tp)
       rs=gettp.executeQuery()
       while (rs.next()) :
         x=rs.getInt("loc_x")
         y=rs.getInt("loc_y")
         z=rs.getInt("loc_z")
         try :
           st.takeItems(57,100000)
           player.teleToLocation(int(x),int(y),int(z))
         except :
           try : gettp.close()
           except : pass
       try :
         con.close()
       except :
         pass
     else:
       htmltext = "<html><body>Стоимость ТП = 100000 Adena</body></html>"
       return htmltext
   else:
     htmltext = ""+str(event)+".htm"
     return htmltext
   return
 
 def onTalk (Self,npc,player):
   st = player.getQuestState(qn) 
   npcId = str(npc.getNpcId())
   if player.isNoble() == 1 :
     htmltext="<html><body>Для ноблов у нас отдельный сервис ;)<br>"
    #htmltext += "Оплата: 1 Noblesse Gate Pass.<br>"
    #htmltext += "<a action=\"bypass -h Quest q2000_NoblesseTeleport "+npcId+"-8\">Другой город</a>&nbsp;<br>"
    #htmltext += "<a action=\"bypass -h Quest q2000_NoblesseTeleport "+npcId+"-3\">Локации</a>&nbsp;<br>"
    #htmltext += "<a action=\"bypass -h Quest q2000_NoblesseTeleport "+npcId+"-10\">7 печатей</a>&nbsp;<br><br>"
     htmltext +="Оплата: 100000 Adena.<br>"
     htmltext += "<a action=\"bypass -h Quest q2000_NoblesseTeleport "+npcId+"-9\">Другой город</a>&nbsp;<br>"
     htmltext += "<a action=\"bypass -h Quest q2000_NoblesseTeleport "+npcId+"-2\">Локации</a>&nbsp;<br>"
     htmltext += "<a action=\"bypass -h Quest q2000_NoblesseTeleport "+npcId+"-11\">7 печатей</a>&nbsp;<br><br>"
     htmltext += "<a action=\"bypass -h npc_%objectId%_Chat 0\">Вернуться</a>"
     htmltext +="</html></body>"
   else :
     htmltext="nobleteleporter-no.htm"
   return htmltext

QUEST       = Quest(2000,qn,"Teleports")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

for item in NPC:
   QUEST.addStartNpc(item)
   QUEST.addTalkId(item)
