import sys
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "2018_EVENTHNY"
NPC=[50006]
QuestId     = 2018
QuestName   = "EVENTHNY"
QuestDesc   = "Quests"
InitialHtml = "1.htm"


class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event

    if event == "0":
        return InitialHtml
    

# 1 подарок
    if event == "1":
        if st.getQuestItemsCount(80005) >= 100:
            st.takeItems(80005,100)
            st.giveItems(80004,1)
            st.playSound("evehny.santa1")
            htmltext = "1.htm"
            st.exitQuest(1)
        else:
             htmltext = "1.htm"
             st.exitQuest(1)
    if htmltext != event:
       st.exitQuest(1)
	   
# 10 подарков
    if event == "2":
        if st.getQuestItemsCount(80005) >= 1000:
            st.takeItems(80005,1000)
            st.giveItems(80004,10)
            st.playSound("evehny.santa1")
            htmltext = "1.htm"
            st.exitQuest(1)
        else:
             htmltext = "1.htm"
             st.exitQuest(1)

# 100 подарков
    if event == "3":
        if st.getQuestItemsCount(80005) >= 10000:
            st.takeItems(80005,10000)
            st.giveItems(80004,100)
            st.playSound("evehny.santa1")
            htmltext = "1.htm"
            st.exitQuest(1)
        else:
             htmltext = "1.htm"
             st.exitQuest(1)

    return htmltext

 def onTalk (self,npc,st):
			htmltext = "<html><head><body>I have nothing to say to you</body></html>"
			st = st.getQuestState(qn)  
			st.setState(STARTED)			 
			return InitialHtml



QUEST       = Quest(2018,qn,"EVENTHNY")
CREATED=State('Start',QUEST)
STARTED=State('Started',QUEST)
COMPLETED=State('Completed',QUEST)


QUEST.setInitialState(CREATED)

for npcId in NPC:
 QUEST.addStartNpc(npcId)
 QUEST.addTalkId(npcId)
 print "#########################"
 print "#         EVENT         #"
 print "#  Happy New Year 2021  #"
 print "#   by STEVE-DOGS.RU    #"
 print "#########################"