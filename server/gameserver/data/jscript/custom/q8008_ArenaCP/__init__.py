import sys
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.gameserver.datatables import SkillTable
from java.lang import System

qn = "q8008_ArenaCP"

ARENA_MAN = [31225, 31226]

ADNEA = 57

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   if npcId in ARENA_MAN:
     if st.getQuestItemsCount(ADNEA) >= 100:
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4380,1)) # animation
       player.setCurrentCp(player.getMaxCp())
       st.takeItems(ADNEA,100)
     else:
       return "<html><body>U vas ne hvataet deneg!</body></html>"
   return

QUEST       = Quest(8008,qn,"custom")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

for i in ARENA_MAN:
  QUEST.addStartNpc(i)
  QUEST.addTalkId(i)
