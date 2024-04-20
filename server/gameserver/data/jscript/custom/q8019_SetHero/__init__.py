import sys
from time import gmtime, strftime
from ru.agecold import L2DatabaseFactory
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "q8019_SetHero"

NPC = 99999
ITEM = 12222 # ���� ����������� ��� ������
COST = 4 # ��������� �� 1 ����
INAME = "Donate Coin" # �������� �����

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onAdvEvent (self,event,npc,player):
   st = player.getQuestState(qn)
   if event.startswith("hero"):
     days = event.replace("hero", "")
     if days == " ":
       htmltext = "<html><body>�� �� ����� ���������� ����.</body></html>"
       return htmltext
     if player.isHero():
       htmltext = "<html><body>�� ��� �����..)</body></html>"
       return htmltext
     days = days.replace(" ", "")
     if not days.isdigit():
       htmltext = "<html><body>�� �� ����� ���������� ����.</body></html>"
       return htmltext
     days = int(days)
     if days < 1:
       htmltext = "<html><body>�� �� ����� ���������� ����.</body></html>"
       return htmltext
     payment = COST * days
     if st.getQuestItemsCount(ITEM) < payment:
       htmltext = "<html><body>������ ���� �� "+str(days)+" ����: <font color=74bff5>"+str(payment)+" "+INAME+"</font></body></html>"
       return htmltext
     htmltext = "<html><body>������ �� �����!<br><br>"
    #<
     st.takeItems(ITEM,payment)
     player.setHero(days)
    #<
     login = str(player.getAccountName()) 
     name = str(player.getName())
     date = str(strftime("%Y-%m-%d", gmtime()))
     time = str(strftime("%H:%M:%S", gmtime()))
     writelog=L2DatabaseFactory.getInstance().getConnection()
     write=writelog.prepareStatement("INSERT INTO zz_donate_log (date,time,login,name,action,payment) VALUES (?,?,?,?,?,?)")
     write.setString(1, date)
     write.setString(2, time)
     write.setString(3, login)
     write.setString(4, name)
     write.setString(5, "Hero Status, "+str(days)+" days.")
     write.setInt(6, payment)
     try :
       write.executeUpdate()
       write.close()
       writelog.close()
     except :
       try : writelog.close()
       except : pass
    #<
     htmltext += "<font color=bef574>������� �� ���������!</font><br></body></html>"
   else:
     htmltext = "<html><body>������ ����:<br>Oops!</body></html>"
   return htmltext

 def onTalk (self,npc,player):
   htmltext = "<html><body>��� ��� � ������ ������ ������ ���.</body></html>"
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   if npcId == NPC:
     htmltext = "privetstvie.htm"
   return htmltext

QUEST       = Quest(8019,qn,"custom")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(NPC)
QUEST.addTalkId(NPC)
