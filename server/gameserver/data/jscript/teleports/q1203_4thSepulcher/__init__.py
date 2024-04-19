# Made by djpvd
# Modified by Bibigon 27.02.08 for RT T0

import sys

from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "q1203_4thSepulcher"

# Main Quest Code
class Quest (JQuest):

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
    st = player.getQuestState(qn)
    npcId = npc.getNpcId()
    htmltext = "You have been teleported."
    if npcId == 319191 :
       if st.getQuestItemsCount(7261)>=1 or st.getQuestItemsCount(7262)>=1 :
         if st.getQuestItemsCount(7262) >=1 :
           st.player.teleToLocation(-80038,-111397,-4899)
         elif st.getQuestItemsCount(7261) >= 1 :
           st.takeItems(7261,1)
           print 1
           st.player.teleToLocation(-80038,-111397,-4899)
       else:
          htmltext = '<html><head>Ghost Chamberlain of Elmoreden:<br>I teleport travelers to the Imperial Tomb. Only those who have visited the Four Sepulchers in the past may go there.<br>To teleport, the traveler must carry a used pass for the sepulcher. I can also teleport those who possess any document that proves that they are directly connected to the Imperial Tomb.</body></html>'
    if npcId == 31920 :
       if st.getQuestItemsCount(7261)>=1 or st.getQuestItemsCount(7262)>=1 :
         if st.getQuestItemsCount(7262) >=1 :
           st.player.teleToLocation(-80038,-111397,-4899)
         elif st.getQuestItemsCount(7261) >= 1 :
           st.takeItems(7261,1)
           st.player.teleToLocation(-80038,-111397,-4899)
       else:
          htmltext = '<html><head>Ghost Chamberlain of Elmoreden:<br>I teleport travelers to the Imperial Tomb. Only those who have visited the Four Sepulchers in the past may go there.<br>To teleport, the traveler must carry a used pass for the sepulcher. I can also teleport those who possess any document that proves that they are directly connected to the Imperial Tomb.</body></html>'
    return htmltext

# Quest class and state definition
QUEST       = Quest(1203, qn, "4th Sepulcher")
CREATED     = State('Start', QUEST)

# Quest initialization
QUEST.setInitialState(CREATED)

QUEST.addStartNpc(319191)
QUEST.addStartNpc(31920)

QUEST.addTalkId(319191)
QUEST.addTalkId(31920)