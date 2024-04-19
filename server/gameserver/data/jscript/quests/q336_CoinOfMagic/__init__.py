# Coins of Magic version 0.1 by DrLecter

#Quest info
qn = "q336_CoinOfMagic"

#Messages
default = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
#NPCs
SORINT, BERNARD, PAGE, HAGGER, STAN, RALFORD, FERRIS, COLLOB, PANO, DUNING, LORAIN = \
30232,  30702,   30696,30183,  30200,30165,   30847,  30092,  30078,30688,  30673

import sys
from ru.agecold import Config
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

def promote(st) :
   grade = st.getInt("grade")
   if grade == 1 :
      html = "30232-15.htm"
   else :
      h = 0
      for i in range(len(PROMOTE[grade])) :
         if st.getQuestItemsCount(PROMOTE[grade][i]):
            h += 1
      if h == i + 1 :
         for j in PROMOTE[grade] :
             st.takeItems(j,1)
         html = "30232-"+str(19-grade)+".htm"
         st.takeItems(3812+grade,-1)
         st.giveItems(3811+grade,1)
         st.set ("grade",str(grade-1))
         cond=COND[grade]
         st.playSound("ItemSound.quest_fanfare_middle")
      else :
         html = "30232-"+str(16-grade)+".htm"
         cond=COND[grade]-1
      st.set("cond",str(cond))
   return html

# main code
class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onTalk (self,npc,player):
   htmltext = default
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   if npcId == SORINT :
     return htmltext
   return htmltext

# Quest class and state definition
QUEST       = Quest(336, qn, "Coins of Magic")
CREATED     = State('Start',     QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)

# Quest NPC starter initialization
QUEST.addStartNpc(SORINT)
QUEST.addTalkId(SORINT)
