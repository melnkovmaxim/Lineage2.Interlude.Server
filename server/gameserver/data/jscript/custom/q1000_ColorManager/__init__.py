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
QuestId     	= 1000
QuestName	= "ColorManager"
QuestDesc	= "custom"
qn		= "q"+str(QuestId)+"_"+str(QuestName)
#========================================
StartNpc	= 80007
ItemId		= 11971
ItemQty		= 10
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
	    nameColor = "FF0000"
	elif event == "N_2" :
	    nameColor = "808080"
	elif event == "N_3" :
	    nameColor = "008000"
	elif event == "N_4" :
	    nameColor = "00FF00"
	elif event == "N_5" :
	    nameColor = "800000"
	elif event == "N_6" :
	    nameColor = "008080"
	elif event == "N_7" :
	    nameColor = "800080"
	elif event == "N_8" :
	    nameColor = "808000"
	elif event == "N_9" :
	    nameColor = "00FFFF"
	elif event == "N_10" :
	    nameColor = "C0C0C0"
	elif event == "N_11" :
	    nameColor = "17A0D4"
	elif event == "T_1" :
	    titleColor = "FF0000"
	elif event == "T_2" :
	    titleColor = "808080"
	elif event == "T_3" :
	    titleColor = "008000"
	elif event == "T_4" :
	    titleColor = "00FF00"
	elif event == "T_5" :
	    titleColor = "800000"
	elif event == "T_6" :
	    titleColor = "008080"
	elif event == "T_7" :
	    titleColor = "800080"
	elif event == "T_8" :
	    titleColor = "808000"
	elif event == "T_9" :
	    titleColor = "00FFFF"
	elif event == "T_10" :
	    titleColor = "C0C0C0"
	elif event == "T_11" :
	    titleColor = "17A0D4"
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
