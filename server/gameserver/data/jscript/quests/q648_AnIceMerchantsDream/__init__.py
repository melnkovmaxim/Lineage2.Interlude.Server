# @author: Aquanox [RusTeam] , thx to May for idea
import sys
from ru.agecold import Config
from ru.agecold.util import Rnd
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest


qn  = "q648_AnIceMerchantsDream"
qn2 = "q115_TheOtherSideofTruth" 
# NPC
RAFFORTY = 32020
ICE_SHELF = 32023

# REWARDS
ADENA = 57
COARSE_BONE_POWDER = 1881
CRAFTED_LEATHER = 1894
STEEL = 1880
EWA = 731
EWB = 947
EAA = 732
EAB = 948
# ITEMS
SILVER_ICE_CRYSTAL = 8077
SILVER_DROP_CHANCE = 75 # 75%
PRICE_PER_SILVER = 300

BLACK_ICE_CRYSTAL = 8078
BLACK_DROP_CHANCE = 10 # 10%
PRICE_PER_BLACK = 1200

HEMOSYCLE = 8057
HEMOSYCLE_CHANCE = 1 # 1%

#MOBS
MONSTERS = [22079,22080,22081,22082,22083,22084,22085,22086,22087,22087,22088,22089,22090,22091,22092,22093,22094,22095,22096,22097,22098,32020,32023]

class Quest (JQuest):
    
    def __init__(self,id,name,descr): 
        JQuest.__init__(self,id,name,descr)
        self.BaseChance = 30
    
    def onAdvEvent (self,event,npc,player):
        st = player.getQuestState(qn)
        cond = st.getInt("cond")
        if event == "start" :
            st2 = player.getQuestState("q115_TheOtherSideOfTruth")
            if st2 and st2.getState().getName() == 'Completed' :
                st.set("cond","2")  
            else :
                st.set("cond","1")
            st.setState(STARTED)
            st.playSound("ItemSound.quest_accept")
            return "03.htm"
        elif event == "quit":
            st.playSound("ItemSound.quest_finish")
            st.exitQuest(1)
            return "exit.htm"
        elif event == "stay":
            return "04.htm"
        elif event == "hemo":
            return "06.htm"
        elif event == "reward":
            cond = st.getInt("cond")
            if cond == 2:
                return "02_2.htm"
            else:
                return "02_1.htm"                    
        elif event == "adena" :
            cond = st.getInt("cond")
            if cond == 1:
                st2 = player.getQuestState("q115_TheOtherSideOfTruth")
                if st2 and st2.getState().getName() == 'Completed' :
                    st.set("cond","2") 
            return "03.htm"
        elif event == "startwrk" :
            return "shelf_2.htm"
        elif event == "chisel" :
            self.BaseChance = self.BaseChance + Rnd.get(-15,10)
            return "shelf_3.htm"
        elif event == "scramer" :
            self.BaseChance = self.BaseChance + Rnd.get(-10,5)
            return "shelf_3.htm"
        elif event == "knife" :
            self.BaseChance = self.BaseChance + Rnd.get(-10,5)
            return "shelf_4.htm"
        elif event == "file" :
            self.BaseChance = self.BaseChance + Rnd.get(-15,10)
            return "shelf_4.htm"
        elif event == "try" :
            if self.BaseChance <= 0 :
                st.takeItems(SILVER_ICE_CRYSTAL, 1)
                st.giveItems(BLACK_ICE_CRYSTAL, 1)
                st.playSound("ItemSound.quest_itemget")                        
                return "shelf_0.htm"
            elif Rnd.get(100) < self.BaseChance :
                st.takeItems(SILVER_ICE_CRYSTAL, 1)
                st.giveItems(BLACK_ICE_CRYSTAL, 1)
                st.playSound("ItemSound.quest_itemget")
                return "shelf_0.htm"
            else :
                st.takeItems(SILVER_ICE_CRYSTAL, 1)
                st.playSound("ItemSound.trash_basket")
                return "Fail"
        return

    def onTalk (self,npc,player):
        htmltext = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
        st = player.getQuestState(qn)
        if not st: return
        npcId = npc.getNpcId()
        cond = st.getInt("cond")
        if npcId == RAFFORTY:
            if cond == 0:
                if player.getLevel() >= 53 and player.getLevel() <= 63:
                    htmltext = "00.htm"
                else:
                    htmltext = "wrong_level.htm"
            elif cond == 1:
                st2 = player.getQuestState("q115_TheOtherSideOfTruth")
                if st2 and st2.getState().getName() == 'Completed' :
                    st.set("cond","2")
                    htmltext = "01_2.htm"
                    return htmltext
                htmltext = "01_1.htm"
            elif cond == 2:
                htmltext = "01_2.htm"           
        elif npcId == ICE_SHELF:
            htmltext = "shelf_0.htm"
            if cond == 2 :
                htmltext = "shelf_1.htm"
        return htmltext

    def onKill (self, npc, player,isPet):
        st = player.getQuestState(qn)
        if not st : return
        cond = st.getInt("cond")
        npcId = npc.getNpcId()
        if (cond == 1) or (cond == 2):
            if Rnd.get(100) <= SILVER_DROP_CHANCE  :
                st.playSound("ItemSound.quest_itemget")
                st.giveItems(SILVER_ICE_CRYSTAL,int(1*Config.RATE_DROP_QUEST))
        if cond == 2 :
            if Rnd.get(100) <= HEMOSYCLE_CHANCE*Config.RATE_DROP_QUEST  :
                st.playSound("ItemSound.quest_itemget")
                st.giveItems(HEMOSYCLE,Rnd.get(1,2))

QUEST       = Quest(648, qn, "An Ice Merchants Dream") 

CREATED     = State('Start',     QUEST)
STARTED     = State('Started',   QUEST)
COMPLETED   = State('Completed', QUEST)

for npcId in MONSTERS:
    QUEST.addAttackId(npcId)
    QUEST.addKillId(npcId)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(RAFFORTY)
QUEST.addStartNpc(ICE_SHELF)

QUEST.addTalkId(RAFFORTY)
QUEST.addTalkId(ICE_SHELF)
