# Mod for RT by Zloctb at 02.11.2007

import sys
from ru.agecold.gameserver.model.actor.instance import L2PcInstance
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "q704_MissQueen"

TICKET_ONE = 7832
TICKET_TWO = 7833
MISS_QUEEN = 31760


class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onEvent(self,event,st):
	htmltext = event
	if event == "1" :
		if st.getPlayer().getLevel() <= 25 and st.getPlayer().getLevel() >= 6 and st.getPlayer().getPkKills() == 0 and int(st.get("onlyone"))==0:
			st.giveItems(TICKET_ONE,1)
			st.set("onlyone","1")
		else :
			htmltext = "haveit_trainee_coupon.htm"
	elif event == "2" :
		if st.getPlayer().getLevel() <= 25 and st.getPlayer().getPkKills() == 0 and st.getPlayer().getClassId().ordinal() in [0x04,0x01,0x07,0x0b,0x0f,0x13,0x16,0x1a,0x1d,0x20,0x23,0x27,0x2a,0x2d,0x2f,0x32,0x36,0x38] and int(st.get("cond"))==0:
			st.giveItems(TICKET_TWO,1)
			st.set("cond","1")
			htmltext = "traveler_coupon.htm"
		elif int(st.get("cond"))==1:
			htmltext = "haveit_traveler_coupon.htm"
		else:
			htmltext = "notallowed_traveler_coupon.htm"
	return htmltext
 
 def onTalk (Self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   htmltext = "<html><body>I have nothing to say you</body></html>"
   id = st.getState()
   if id == CREATED :
		st.set("onlyone","0")
		st.set("cond","0")
		st.setState(STARTED)
   if npcId == MISS_QUEEN  :
		return "quest.htm"

QUEST = Quest(704,qn,"Miss Queen")
CREATED = State('Start',QUEST)
STARTED = State('Started', QUEST)
COMPLETED = State('Completed', QUEST)

QUEST.setInitialState(CREATED)

for i in [ MISS_QUEEN ] :
    QUEST.addStartNpc(i)
    QUEST.addTalkId(i)
    QUEST.addTalkId(i)