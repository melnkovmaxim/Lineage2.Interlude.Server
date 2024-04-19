###########################################
#   #
#                #
###########################################
import sys
from ru.agecold.gameserver.model.actor.instance import L2PcInstance
from ru.agecold.gameserver.model.quest        	import State
from ru.agecold.gameserver.model.quest        	import QuestState
from ru.agecold.gameserver.model.quest.jython 	import QuestJython as JQuest
#
#========================================
# DO NOT TOUCH THIS
#========================================
QuestId     	= 1001
QuestName	= "ColorManager1"
QuestDesc	= "custom"
qn		= "q"+str(QuestId)+"_"+str(QuestName)
#========================================
StartNpc	= 80007
ItemId		= 11971
ItemQty		= 150
MinLevel	= 1
MaxLevel	= 80
#========================================
class Quest (JQuest) :
    def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
    
    def onAdvEvent(Self,event,npc,player):
	text = "noaction.htm"
	st = player.getQuestState(qn)
	if not st : return text
	if (player.getLevel() < MinLevel) or (player.getLevel() > MaxLevel) : return "charlevel.htm"
	if st.getQuestItemsCount(ItemId) < ItemQty : return "nomoney.htm"
	nameColor = hex(player.getAppearance().getNameColor())
	titleColor = hex(player.getAppearance().getTitleColor())
	st.set("nameColor",str(nameColor))
	st.set("titleColor",str(titleColor))
	if event == "N_1" :
	    nameColor = "000000"
	elif event == "T_1" :
	    titleColor = "000000"
	nameColor = int(nameColor,16)
	titleColor = int(titleColor,16)
	player.getAppearance().setNameColor(nameColor)
	player.getAppearance().setTitleColor(titleColor)
	st.takeItems(ItemId,ItemQty)
	player.broadcastUserInfo()
	player.store()
	text = "done.htm"
	return text
	
    def onTalk (Self,npc,player):
	st = player.getQuestState(qn)
	if not st :
	    return "Quest is not started!"
	st.setState(STARTED)
	return "1.htm"

    def onFirstTalk (Self,npc,player):
	st = getQuestState(qn)
	if not st : return "Invalid quest state"

QUEST	  = Quest(QuestId,qn,QuestDesc)
CREATED	  = State('Start',QUEST)
STARTED	  = State('Started',QUEST)
COMPLETED = State('Completed',QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(StartNpc)
QUEST.addTalkId(StartNpc)

print "Loaded: "+qn
