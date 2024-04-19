# Lets Become A Royal Member ver. 0.1 by DrLecter
import sys
from ru.agecold import Config
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

QuestNumber      = 381
QuestName        = "LetsBecomeARoyalMember"
QuestDescription = "Let's become a Royal Member"
qn = "q381_LetsBecomeARoyalMember"

#Messages
default = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
#Quest items
#NPCs
SORINT, SANDRA = 30232, 30090

class Quest (JQuest) :

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
      htmltext = default
      return htmltext

QUEST       = Quest(QuestNumber, "q"+str(QuestNumber)+"_"+QuestName, QuestDescription)
CREATED     = State('Start',     QUEST)
STARTED     = State('Started',   QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(SORINT)

QUEST.addTalkId(SORINT)
