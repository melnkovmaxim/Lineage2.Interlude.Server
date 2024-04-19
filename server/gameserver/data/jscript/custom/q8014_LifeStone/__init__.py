import sys
from java.util import Iterator
from ru.agecold.util import Rnd
from ru.agecold.gameserver.network.serverpackets import SystemMessage
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold import L2DatabaseFactory
from ru.agecold.gameserver.templates import L2EtcItemType
from ru.agecold.gameserver.templates import L2Item
from java.lang import System
from ru.agecold.gameserver.idfactory import IdFactory
from ru.agecold.gameserver.model import L2ItemInstance
from ru.agecold.gameserver.datatables import AugmentationData
from ru.agecold.gameserver.model import L2Augmentation
from ru.agecold.gameserver.network.serverpackets import ItemList
from ru.agecold.gameserver.model import L2Skill
from ru.agecold.gameserver.datatables import SkillTable

qn = "q8014_LifeStone"

NPC = 15101
ITEM = 11971
COST = 10
FORBIDDEN = [6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621]

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onAdvEvent (self,event,npc,player):
   st = player.getQuestState(qn)
   if event == "spisok":
     if st.getQuestItemsCount(ITEM) < COST:
       htmltext = "<html><body>Перенос Лс стоит <font color=74bff5>"+str(COST)+" BaD Coin</font></body></html>"
       return htmltext
     htmltext = "<html><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><img src=\"L2UI.SquareBlank\" width=260 height=2><br1>"       
     htmltext += "<table width=260><tr><td align=center><font color=LEVEL>Перенос ЛС</font></td></tr></table>"
     htmltext += "<button value=\"Выбрать пушку\" action=\"bypass -h Quest q8014_LifeStone step1\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1>"
     htmltext += "<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center></body></html>"
   elif event == "step1":
     htmltext = "<html><body>Перенос ЛС:<br>Откуда переносим?<br><br><table width=300>"
     SPWEAPONS = ["Sword","Blunt","Dagger","Bow","Etc","Pole","Fist","Dual Sword","Dual Fist","Big Sword","Big Blunt"]
     for Item in st.getPlayer().getInventory().getItems():
       itemTemplate = Item.getItem()
       idtest = Item.getItemId()
       itype = str(Item.getItemType())
       if idtest not in FORBIDDEN and Item.isAugmented() and not Item.isEquipped() and itype in SPWEAPONS:
         cnt = Item.getCount()
         count = str(cnt)
         grade = itemTemplate.getCrystalType()   
         con=L2DatabaseFactory.getInstance().getConnection()
         listitems=con.prepareStatement("SELECT itemIcon FROM z_market_icons WHERE itemId=?")
         listitems.setInt(1, idtest)
         rs=listitems.executeQuery()
         while (rs.next()) :
           icon=rs.getString("itemIcon")
           try :
             if grade == 1:
               pgrade = str("[D]")
             elif grade == 2:
               pgrade = str("[C]")
             elif grade == 3:
               pgrade = str("[B]")
             elif grade == 4:
               pgrade = str("[A]")
             elif grade == 5:
               pgrade = str("[S]")
             else:
               pgrade = str("")
             if Item.getEnchantLevel() == 0:
               enchant = str("")
             else:
               enchant = " +"+str(Item.getEnchantLevel())+""
             htmltext += "<tr><td><img src=\"Icon."+str(icon)+"\" width=32 height=32></td><td><a action=\"bypass -h Quest q8014_LifeStone step1next_" + str(Item.getObjectId()) +"\">" + itemTemplate.getName() + ""+str(pgrade)+" " + enchant + "</a></td></tr>"
           except :
             try : insertion.close()
             except : pass
         try :
           con.close()
         except :
           pass
     htmltext += "</table><br><a action=\"bypass -h Quest q8014_LifeStone spisok\">Вернуться.</a></body></html>"
   elif event.startswith("step1next_"):
     itemObjId = int(event.replace("step1next_", ""))
     obj = str(itemObjId)
     Item = st.getPlayer().getInventory().getItemByObjectId(itemObjId)
     itemTemplate = Item.getItem()
     if Item and not Item.isEquipped():
       cnt = Item.getCount()
       count = str(cnt)
       grade = itemTemplate.getCrystalType()
       igrade = str(itemTemplate.getCrystalType())
       itype = str(Item.getItemType())
       idtest = Item.getItemId()
       con=L2DatabaseFactory.getInstance().getConnection()
       listitems=con.prepareStatement("SELECT itemIcon, skill, attributes FROM `z_market_icons` icon, `augmentations` aug WHERE icon.itemId=? AND aug.item_id=?")
       listitems.setInt(1, idtest)
       listitems.setInt(2, itemObjId)
       rs=listitems.executeQuery()
       while (rs.next()) :
         icon=rs.getString("itemIcon")
         skill=rs.getInt("skill")
         attributes=rs.getInt("attributes")
         try :
           st.set("oneitem",obj)
           st.set("skill",str(skill))
           grades = {1: "d", 2: "c", 3: "b", 4: "a", 5: "s"}
           pgrade = grades.get(grade, str(""))
           enchant = (Item.getEnchantLevel() > 0 and " +"+str(Item.getEnchantLevel())+"") or str("")
           htmltext =  "<html><body>Перенос ЛС:<br>Из этой пушки переносим?<br>"
           htmltext += "<table width=300><tr><td><img src=\"Icon."+str(icon)+"\" width=32 height=32></td><td><font color=LEVEL>" + itemTemplate.getName() + " " + enchant + "</font><img src=\"symbol.grade_"+str(pgrade)+"\" width=16 height=16><br></td></tr></table><br><br>"
           if skill != 0:
             skill = SkillTable.getInstance().getInfo(skill, 1)
             name = skill.getName()
             htmltext += "<br><font color=bef574>["+str(name)+"]</font><br>"
             htmltext += "<button value=\"Продолжить\" action=\"bypass -h Quest q8014_LifeStone step2\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
           else:
             htmltext += "V etoi puwke net skilla"
           htmltext += "<br><a action=\"bypass -h Quest q8014_LifeStone spisok\">Вернуться.</a></body></html>"
         except :
           try : listitems.close()
           except : pass
       try :
         con.close()
       except :
         pass
     else :
       htmltext = "<html><body>Перенос ЛС:<br>Ошибка!<br><a action=\"bypass -h Quest q8014_LifeStone spisok\">Вернуться.</a></body></html>"
   elif event == "step2":
     htmltext = "<html><body>Перенос ЛС:<br>Куда переносим?<br><br><table width=300>"
     SPWEAPONS = ["Sword","Blunt","Dagger","Bow","Etc","Pole","Fist","Dual Sword","Dual Fist","Big Sword","Big Blunt"]
     weapon1 = st.getInt("oneitem")
     for Item in st.getPlayer().getInventory().getItems():
       itemTemplate = Item.getItem()
       idtest = Item.getItemId()
       itype = str(Item.getItemType())
       if idtest not in FORBIDDEN and not Item.isEquipped() and itype in SPWEAPONS and Item.getObjectId() != weapon1 and not Item.isAugmented():
         grade = itemTemplate.getCrystalType()   
         con=L2DatabaseFactory.getInstance().getConnection()
         listitems=con.prepareStatement("SELECT itemIcon FROM z_market_icons WHERE itemId=?")
         listitems.setInt(1, idtest)
         rs=listitems.executeQuery()
         while (rs.next()) :
           icon=rs.getString("itemIcon")
           try :
             if grade == 1:
               pgrade = str("[D]")
             elif grade == 2:
               pgrade = str("[C]")
             elif grade == 3:
               pgrade = str("[B]")
             elif grade == 4:
               pgrade = str("[A]")
             elif grade == 5:
               pgrade = str("[S]")
             else:
               pgrade = str("")
             if Item.getEnchantLevel() == 0:
               enchant = str("")
             else:
               enchant = " +"+str(Item.getEnchantLevel())+""
             htmltext += "<tr><td><img src=\"Icon."+str(icon)+"\" width=32 height=32></td><td><a action=\"bypass -h Quest q8014_LifeStone step2next_" + str(Item.getObjectId()) +"\">" + itemTemplate.getName() + ""+str(pgrade)+" " + enchant + "</a></td></tr>"
           except :
             try : insertion.close()
             except : pass
         try :
           con.close()
         except :
           pass
     htmltext += "</table><br><a action=\"bypass -h Quest q8014_LifeStone spisok\">Вернуться.</a></body></html>"
   elif event.startswith("step2next_"):
     itemObjId = int(event.replace("step2next_", ""))
     obj = str(itemObjId)
     Item = st.getPlayer().getInventory().getItemByObjectId(itemObjId)
     itemTemplate = Item.getItem()
     if Item and not Item.isAugmented() and not Item.isEquipped():
       cnt = Item.getCount()
       count = str(cnt)
       grade = itemTemplate.getCrystalType()
       igrade = str(itemTemplate.getCrystalType())
       itype = str(Item.getItemType())
       idtest = Item.getItemId()
       con=L2DatabaseFactory.getInstance().getConnection()
       listitems=con.prepareStatement("SELECT itemIcon FROM z_market_icons WHERE itemId=?")
       listitems.setInt(1, idtest)
       rs=listitems.executeQuery()
       while (rs.next()) :
         icon=rs.getString("itemIcon")
         try :
           st.set("twoitem",obj)
           st.set("lcount",count)
           st.set("grade",igrade)
           st.set("type",itype)
           grades = {1: "d", 2: "c", 3: "b", 4: "a", 5: "s"}
           pgrade = grades.get(grade, str(""))
           enchant = (Item.getEnchantLevel() > 0 and " +"+str(Item.getEnchantLevel())+"") or str("")
           htmltext =  "<html><body>Перенос ЛС:<br>В эту пушку переносим?<br>"
           htmltext += "<table width=300><tr><td><img src=\"Icon."+str(icon)+"\" width=32 height=32></td><td><font color=LEVEL>" + itemTemplate.getName() + " " + enchant + "</font><img src=\"symbol.grade_"+str(pgrade)+"\" width=16 height=16><br></td></tr></table><br><br>"
           htmltext += "<button value=\"Продолжить\" action=\"bypass -h Quest q8014_LifeStone step3\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
           htmltext += "<br><a action=\"bypass -h Quest q8014_LifeStone spisok\">Вернуться.</a></body></html>"
         except :
           try : listitems.close()
           except : pass
       try :
         con.close()
       except :
         pass
     else :
       htmltext = "<html><body>Перенос Лс:<br>Ошибка!<br><a action=\"bypass -h Quest q8014_LifeStone spisok\">Вернуться.</a></body></html>"
   elif event.startswith("step3"):
     weapon1 = st.getInt("oneitem")
     weapon2 = st.getInt("twoitem")
     skillp = st.getInt("skill")
     htmltext =  "<html><body>Перенос ЛС:<br>Подтверждаете?<br>"
     skill = SkillTable.getInstance().getInfo(skillp, 1)
     name = skill.getName()
     htmltext += "<font color=bef574>["+str(name)+"]</font><br>"
     htmltext += "Из:"
     Item = st.getPlayer().getInventory().getItemByObjectId(weapon1)
     itemTemplate = Item.getItem()
     if Item and Item.isAugmented() and not Item.isEquipped():
       cnt = Item.getCount()
       count = str(cnt)
       grade = itemTemplate.getCrystalType()
       igrade = str(itemTemplate.getCrystalType())
       itype = str(Item.getItemType())
       idtest = Item.getItemId()
       con=L2DatabaseFactory.getInstance().getConnection()
       listitems=con.prepareStatement("SELECT itemIcon FROM z_market_icons WHERE itemId=?")
       listitems.setInt(1, idtest)
       rs=listitems.executeQuery()
       while (rs.next()) :
         icon=rs.getString("itemIcon")
         try :
           grades = {1: "d", 2: "c", 3: "b", 4: "a", 5: "s"}
           pgrade = grades.get(grade, str(""))
           enchant = (Item.getEnchantLevel() > 0 and " +"+str(Item.getEnchantLevel())+"") or str("")
           htmltext += "<table width=300><tr><td><img src=\"Icon."+str(icon)+"\" width=32 height=32></td><td><font color=LEVEL>" + itemTemplate.getName() + " " + enchant + "</font><img src=\"symbol.grade_"+str(pgrade)+"\" width=16 height=16><br></td></tr></table><br><br>"
         except :
           try : listitems.close()
           except : pass
       try :
         con.close()
       except :
         pass
     htmltext += "В:<br>"
     Item = st.getPlayer().getInventory().getItemByObjectId(weapon2)
     itemTemplate = Item.getItem()
     if Item and not Item.isAugmented() and not Item.isEquipped():
       cnt = Item.getCount()
       count = str(cnt)
       grade = itemTemplate.getCrystalType()
       igrade = str(itemTemplate.getCrystalType())
       itype = str(Item.getItemType())
       idtest = Item.getItemId()
       cons=L2DatabaseFactory.getInstance().getConnection()
       listitemss=cons.prepareStatement("SELECT itemIcon FROM z_market_icons WHERE itemId=?")
       listitemss.setInt(1, idtest)
       rs=listitemss.executeQuery()
       while (rs.next()) :
         icon=rs.getString("itemIcon")
         try :
           grades = {1: "d", 2: "c", 3: "b", 4: "a", 5: "s"}
           pgrade = grades.get(grade, str(""))
           enchant = (Item.getEnchantLevel() > 0 and " +"+str(Item.getEnchantLevel())+"") or str("")
           htmltext += "<table width=300><tr><td><img src=\"Icon."+str(icon)+"\" width=32 height=32></td><td><font color=LEVEL>" + itemTemplate.getName() + " " + enchant + "</font><img src=\"symbol.grade_"+str(pgrade)+"\" width=16 height=16><br></td></tr></table><br><br>"
         except :
           try : listitemss.close()
           except : pass
       try :
         cons.close()
       except :
         pass
       htmltext += "<button value=\"Продолжить\" action=\"bypass -h Quest q8014_LifeStone step4\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>"
     else :
       htmltext = "<html><body>Перенос Лс:<br>Ошибка!<br><a action=\"bypass -h Quest q8014_LifeStone spisok\">Вернуться.</a></body></html>"
   elif event.startswith("step4"):
     weapon1 = st.getInt("oneitem")
     weapon2 = st.getInt("twoitem")
     htmltext =  "<html><body>Перенос Лс:<br>Подождите...<br><br>"
     if st.getQuestItemsCount(ITEM) < COST:
       htmltext = "<html><body>Перенос Лс стоит <font color=74bff5>"+str(COST)+" BaD Coin</font></body></html>"
       return htmltext
     item2 = st.getPlayer().getInventory().getItemByObjectId(weapon2)
     item1 = st.getPlayer().getInventory().getItemByObjectId(weapon1)
     con=L2DatabaseFactory.getInstance().getConnection()
     listitems=con.prepareStatement("SELECT * FROM `augmentations` WHERE item_id=?")
     listitems.setInt(1, weapon1)
     rs=listitems.executeQuery()
     while (rs.next()) :
       attributes=rs.getInt("attributes")
       skill=rs.getInt("skill")
       try :
         st.takeItems(ITEM,COST)
         item2.setAugmentation(L2Augmentation(item2, attributes, skill, 10, True))
         player.sendPacket(ItemList(player, False))
         item1.removeAugmentation()
         skilla = SkillTable.getInstance().getInfo(skill, 1)
         name = skilla.getName()
         htmltext += "<font color=bef574>["+str(name)+"]</font> переставлен.<br>"
       except :
         try : listitems.close()
         except : pass
     try :
       con.close()
     except :
       pass
   else:
     htmltext = "<html><body>Перенос ЛС:<br>Oops!</body></html>"
   return htmltext

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   if npcId == NPC:
     htmltext = "privetstvie.htm"
   return htmltext

QUEST       = Quest(8014,qn,"custom")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(NPC)
QUEST.addTalkId(NPC)
