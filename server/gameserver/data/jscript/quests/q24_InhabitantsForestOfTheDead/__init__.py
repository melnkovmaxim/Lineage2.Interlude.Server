# Made by Bibigon for RT v.02 01.12.07
# fixed by Pixar
"""
ƒл€ того чтобы игрок прошел квест на стадии когда он должен вернутьс€
в деревню ночью, чтобы посмотреть на вампиров - ему необходимо вз€ть в target
одного из вампиров (EILHALDER_VON_HELLMANN, VIOLET_VAMP, KURSTIN_VAMP, MINA_VAMP, DORIAN_VAMP)
—о служанкой Maid of Lidia можно поговорить только после суток игрового времени.
"""

import sys
from ru.agecold.util import Rnd
from ru.agecold.gameserver.model.quest        import State
from ru.agecold.gameserver.model.quest        import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "q24_InhabitantsForestOfTheDead"

# ~~~~~~ npcId list: ~~~~~~
DORIAN	                = 31389
TOMBSTONE	        = 31531
MAID_OF_LIDIA           = 31532
MYSTERIOUS_WIZARD       = 31522
BENEDICT	        = 31349
# ~~~~~~~~~~~~~~~~~~~~~~~~~

#~~~~~~~~~~ Mob`s~~~~~~~~~~
#  Bone Snatchers, Bone Shapers, Bone Collectors, Bone Animators, Bone Slayers, Skull Collectors, Skull Animators
MOBS = range(21557,21559)+[21560]+range(21560,21568)
VAMPIRE = range(25328,25333)
# ~~~~~ itemId List ~~~~~
LIDIA_HAIR_PIN                   = 7148
SUSPICIOUS_TOTEM_DOLL            = 7151
FLOWER_BOUQUET 	                 = 7152
SILVER_CROSS_OF_EINHASAD         = 7153
BROKEN_SILVER_CROSS_OF_EINHASAD  = 7154
# ~~~~~~~~~~~~~~~~~~~~~~~


class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st):
   htmltext = event
   player = st.getPlayer()
   if event == "31389-03.htm" :
      st.giveItems(FLOWER_BOUQUET,1)
      st.set("cond","1")
      st.setState(STARTED)
      st.playSound("ItemSound.quest_accept")
   elif event == "31531-02.htm" :
      st.takeItems(FLOWER_BOUQUET,-1)
      st.set("cond","2")
   elif event == "31389-13.htm" :
      st.giveItems(SILVER_CROSS_OF_EINHASAD,1)
      st.set("cond","3")
   elif event == "31389-19.htm" :
      st.set("cond","5")
   elif event == "31532-04.htm" :
      st.set("cond","6")
      st.startQuestTimer("Lidias Letter",7000)
   elif event == "Lidias Letter" :
      htmltext = "lidias_letter.htm"
   elif event == "31532-06.htm" :
      st.takeItems(LIDIA_HAIR_PIN,-1)
   elif event == "31532-19.htm" :
      st.set("cond","9")
   elif event == "31522-03.htm" :    
      st.takeItems(SUSPICIOUS_TOTEM_DOLL,-1)
   elif event == "31522-08.htm" :
      st.set("cond","11")
      st.startQuestTimer("To talk with Mystik",600000)
   elif event == "31522-21.htm" :
      st.giveItems(SUSPICIOUS_TOTEM_DOLL,1)
      st.unset("cond")
      st.set("onlyone","1")
      st.setState(COMPLETED)
      st.startQuestTimer("html",5)
      htmltext = "Congratulations! You are completed this quest!"     + \
                 " \n The Quest \"Hiding Behind the Truth\""   + \
                 " become available.\n Show Suspicious Totem Doll to "+ \
                 " Priest Benedict."
   elif event == "html" :
      htmltext = "31522-22.htm"
      st.playSound("ItemSound.quest_finish")
   return htmltext

 def onFocus(self, npc, player) :
    vamp = npc.getNpcId()
    if player.getInt("cond") == 3 :
       if vamp in VAMPIRE :
          player.takeItems(SILVER_CROSS_OF_EINHASAD,-1)
          player.giveItems(BROKEN_SILVER_CROSS_OF_EINHASAD,1)
          player.playSound("SkillSound5.horror_02")
          player.set("cond","4")
          if Rnd.get(3) < 3 :
             player.takeItems(LIDIA_HAIR_PIN,-1)
    else :
       return
    return

 def onTalk (self, npc, player) :
   st = player.getQuestState(qn)
   htmltext = "<html><body>I have nothing to say you...</body></html>"
   if not st : return htmltext
   npcId = npc.getNpcId()
   cond = st.getInt("cond")
   onlyone = st.getInt("onlyone")

   if npcId == DORIAN :
      if cond == 0 and onlyone == 0 :
         LidiasHeart = st.getPlayer().getQuestState("q23_LidiasHeart")
         if not LidiasHeart is None:
            if LidiasHeart.get("onlyone") == "1":
               htmltext = "31389-01.htm"
            else:
               htmltext = "31389-02.htm"	         # ≈сли 23 квест не пройден
      elif cond == 1 and onlyone == 0 and st.getQuestItemsCount(FLOWER_BOUQUET) == 1 : 
         htmltext = "31389-04.htm"			 # ≈сли букет еще в руках
      elif cond == 2 and st.getQuestItemsCount(FLOWER_BOUQUET) == 0 : 
         htmltext = "31389-05.htm"
      elif cond == 3 and st.getQuestItemsCount(SILVER_CROSS_OF_EINHASAD) == 1 :
         htmltext = "31389-14.htm"
      elif cond == 4 and st.getQuestItemsCount(BROKEN_SILVER_CROSS_OF_EINHASAD) == 1 :
         htmltext = "31389-15.htm"
         st.takeItems(BROKEN_SILVER_CROSS_OF_EINHASAD,-1)
      elif cond == 7 and st.getQuestItemsCount(LIDIA_HAIR_PIN) == 0 :
         htmltext = "31389-21.htm"
         st.giveItems(LIDIA_HAIR_PIN,1)
         st.set("cond","8")
         
      
           
   elif npcId == TOMBSTONE :
      if cond == 1 and st.getQuestItemsCount(FLOWER_BOUQUET) == 1 :
         htmltext = "31531-01.htm"
      elif cond == 2 and st.getQuestItemsCount(FLOWER_BOUQUET) == 0 :
         htmltext = "31531-03.htm"                    # ≈сли букет уже оставлен

   elif npcId == MAID_OF_LIDIA :
      if cond == 5 :
         htmltext = "31532-01.htm"
      elif cond == 6 :
         if st.getQuestItemsCount(LIDIA_HAIR_PIN) == 0 :
            htmltext = "31532-07.htm"
            st.set("cond","7")
         else :
            htmltext = "31532-05.htm"
      elif cond == 8 and st.getQuestItemsCount(LIDIA_HAIR_PIN) != 0 :
         htmltext = "31532-10.htm"
         st.takeItems(LIDIA_HAIR_PIN,-1)
         
   elif npcId == MYSTERIOUS_WIZARD :
      timer_speak = st.getQuestTimer("To talk with Mystik")
      if cond == 10 and st.getQuestItemsCount(SUSPICIOUS_TOTEM_DOLL) != 0 :
         htmltext = "31522-01.htm"
      elif cond == 11 and  timer_speak == None and  st.getQuestItemsCount(SUSPICIOUS_TOTEM_DOLL) == 0 :
         htmltext = "31522-09.htm"
      elif cond == 11 and  st.getQuestItemsCount(SUSPICIOUS_TOTEM_DOLL) != 0 :
         htmltext = "31522-22.htm"
              
   return htmltext           

 def onKill (self, npc, player, isPet) :
   st = player.getQuestState(qn)
   if not st : return 
   if st.getState() != STARTED : return 
   npcId = npc.getNpcId()
   cond = st.getInt("cond")
   if npcId in MOBS:
      if cond == 9 and st.getRandom(100)>70:
         st.giveItems(SUSPICIOUS_TOTEM_DOLL,1)
         st.playSound("ItemSound.quest_middle")
         st.set("cond","10")
   return

QUEST     = Quest(24,qn,"Inhabitants of the Forest of the Dead")
CREATED   = State('Start',     QUEST)
STARTING  = State('Starting',  QUEST)
STARTED   = State('Started',   QUEST)
COMPLETED = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(DORIAN)

QUEST.addTalkId(DORIAN)
QUEST.addTalkId(TOMBSTONE)
QUEST.addTalkId(MAID_OF_LIDIA)
QUEST.addTalkId(MYSTERIOUS_WIZARD)

for vamp in VAMPIRE:
    QUEST.addFocusId(vamp)

for npcId in MOBS:
    QUEST.addKillId(npcId)
    STARTED.addQuestDrop(npcId,SUSPICIOUS_TOTEM_DOLL,1)
