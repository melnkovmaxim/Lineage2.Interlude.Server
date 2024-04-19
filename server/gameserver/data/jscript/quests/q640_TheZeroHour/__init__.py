import sys
from ru.agecold import Config
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "q640_TheZeroHour"

#NPCs
KAHMAN = 31554

#MOBs
MOBS = range(22105,22121)

#Items
DROP_CHANCE = 75
STAKATOFUNG = 8085

class Quest (JQuest) :
 def __init__(self,id,name,descr):
    JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event
    fung = st.getQuestItemsCount(STAKATOFUNG)
    if event == "ok.htm" :
       st.setState(STARTED)
       st.playSound("ItemSound.quest_accept")
       st.set("cond","1")
    elif event == "vihod.htm" :
       st.exitQuest(1)
       st.playSound("ItemSound.quest_finish")
    elif event == "enria" :
        if fung >= 12 :
            st.takeItems(STAKATOFUNG,12)
            st.giveItems(4042,1)
            st.playSound("ItemSound.quest_itemget")
            htmltext = "dal.htm"
        else :
            htmltext = "nehvataet.htm"
    elif event == "asofe" :
        if fung >= 6 :
            st.takeItems(STAKATOFUNG,6)
            st.giveItems(4043,1)
            st.playSound("ItemSound.quest_itemget")
            htmltext = "dal.htm"
        else :
            htmltext = "nehvataet.htm"
    elif event == "thons" :
        if fung >= 6 :
            st.takeItems(STAKATOFUNG,6)
            st.giveItems(4044,1)
            st.playSound("ItemSound.quest_itemget")
            htmltext = "dal.htm"
        else :
            htmltext = "nehvataet.htm"
    elif event == "vpurity" :
        if fung >= 81 :
            st.takeItems(STAKATOFUNG,81)
            st.giveItems(1887,10)
            st.playSound("ItemSound.quest_itemget")
            htmltext = "dal.htm"
        else :
            htmltext = "nehvataet.htm"
    elif event == "syncokes" :
        if fung >= 33 :
            st.takeItems(STAKATOFUNG,33)
            st.giveItems(1888,5)
            st.playSound("ItemSound.quest_itemget")
            htmltext = "dal.htm"
        else :
            htmltext = "nehvataet.htm"
    elif event == "combraid" :
        if fung >= 30 :
            st.takeItems(STAKATOFUNG,30)
            st.giveItems(1889,10)
            st.playSound("ItemSound.quest_itemget")
            htmltext = "dal.htm"
        else :
            htmltext = "nehvataet.htm"
    elif event == "dmplate" :
        if fung >= 150 :
            st.takeItems(STAKATOFUNG,150)
            st.giveItems(5550,10)
            st.playSound("ItemSound.quest_itemget")
            htmltext = "dal.htm"
        else :
            htmltext = "nehvataet.htm"
    elif event == "malloy" :
        if fung >= 131 :
            st.takeItems(STAKATOFUNG,131)
            st.giveItems(1890,10)
            st.playSound("ItemSound.quest_itemget")
            htmltext = "dal.htm"
        else :
            htmltext = "nehvataet.htm"
    elif event == "orihar" :
        if fung >= 123 :
            st.takeItems(STAKATOFUNG,123)
            st.giveItems(1893,10)
            st.playSound("ItemSound.quest_itemget")
            htmltext = "dal.htm"
        else :
            htmltext = "nehvataet.htm"		
    return htmltext

 def onTalk (self,npc,player):
    st = player.getQuestState(qn)
    htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>" 
    if not st: return htmltext
    npcId = npc.getNpcId()
    id = st.getState()
    cond = st.getInt("cond")
    fung = st.getQuestItemsCount(STAKATOFUNG)
    if npcId == KAHMAN :
       if id == CREATED :
          if player.getLevel() >= 66 :
             htmltext = "privetstvie.htm"
          else :
             htmltext = "lvl.htm"
             st.exitQuest(1)
       elif cond == 1:
          if fung >= 1 :
             htmltext = "zanagradoi.htm"
          else:
             htmltext = "nehvataet.htm"
    return htmltext

 def onKill(self,npc,player,isPet):
    partyMember = self.getRandomPartyMember(player,"1")
    if not partyMember : return
    st = partyMember.getQuestState(qn)
    if st :
        chance = DROP_CHANCE*Config.RATE_DROP_QUEST
        numItems, chance = divmod(chance,100)
        random = st.getRandom(100)
        if random <= chance:
           numItems += 1
        if int(numItems) != 0 :
           st.giveItems(STAKATOFUNG,int(numItems))
           st.playSound("ItemSound.quest_itemget")
    return  

QUEST = Quest(640,qn,"The Zero Hour")
CREATED     = State('Start',     QUEST)
STARTED     = State('Started',   QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(KAHMAN)
QUEST.addTalkId(KAHMAN)

for m in MOBS:
   QUEST.addKillId(m)
