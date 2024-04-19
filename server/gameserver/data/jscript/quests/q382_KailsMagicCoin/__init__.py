import sys
from ru.agecold import Config
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "q382_KailsMagicCoin"
VERGARA = 30687
#Messages
default = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"

class Quest (JQuest) :

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
      htmltext = default
      return htmltext

QUEST       = Quest(382, qn, "Kail's Magic Coin")
CREATED     = State('Start',     QUEST)
STARTED     = State('Started',   QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(VERGARA)

QUEST.addTalkId(VERGARA)
