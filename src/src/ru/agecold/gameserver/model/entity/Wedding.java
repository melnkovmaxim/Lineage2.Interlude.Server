package ru.agecold.gameserver.model.entity;

import ru.agecold.Config;
import ru.agecold.gameserver.Announcements;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.instancemanager.CoupleManager;
import ru.agecold.gameserver.model.actor.instance.L2WeddingManagerInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.serverpackets.ConfirmDlg;
import ru.agecold.gameserver.network.serverpackets.MagicSkillUser;

public class Wedding
{
    private L2PcInstance groom = null;
    private L2PcInstance bride = null;
	private L2WeddingManagerInstance prist = null;
	private long expire = 0;
	
	private boolean groomYes = false;
	private boolean brideYes = false;
	
	public boolean married = false;

    public Wedding(L2PcInstance groom, L2PcInstance bride, L2WeddingManagerInstance prist)
    {
        this.groom = groom;
        this.bride = bride;
        this.prist = prist;
		this.expire = System.currentTimeMillis() + 60000;
		
		ThreadPoolManager.getInstance().scheduleAi(new Say(0), 3000, false);
    }
	
	private class Say implements Runnable
	{
		int id;
		Say(int id)
		{
			this.id = id;
		}

		public void run()
		{	
			if(married)
				return;

			int next = id + 1;
			switch (id)
			{
				case 0:
					prist.sayString("��������� �����, ������� �����������!", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 3000, false);
					break;
				case 1:
					prist.sayString("�� ���� ��� �������������� �� ������ �������������� ����������� ���.", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 3000, false);
					break;
				case 2:
					prist.sayString("������� " + groom.getName() + " � " + bride.getName() + "!", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 3000, false);
					break;
				case 3:
					prist.sayString("�������, ����� ������������ ���� � ����� �����, �� ��������� � ����!", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 3000, false);
					break;
				case 4:
					prist.sayString("�� ������ �� ������ ������ ����� ����� �����!", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 3000, false);
					break;
				case 5:
					prist.sayString("������� �������� ����� �������� � �����, �� ���� � �����!", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 3000, false);
					break;
				case 6:
					prist.sayString("���������� ������, ����� ��� ����������� ���������� ����� �����������!", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 3000, false);
					break;
				case 7:
					prist.sayString("��������� �������� ���, �������� �� ���� ������� ����� ����� � �����, �������� � ���������!", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Step(1), 2000, false);
					break;
				case 8:
					prist.sayString("�� ������ ��������� ������� � ��������, ���� ��������������!", 18);
                    CoupleManager.getInstance().createCouple(groom, bride);
                    prist.broadcastPacket(new MagicSkillUser(prist, prist, 2230, 1, 1, 0));
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 2000, false);
					break;
				case 9:
                    prist.broadcastPacket(new MagicSkillUser(prist, prist, 2025, 1, 1, 0));
					prist.sayString("� ������������ � �������� ��������, ��� ���� ���������������!", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 1000, false);
					break;
				case 10:
                    CoupleManager.getInstance().getCouple(groom.getCoupleId()).marry();
                    groom.setMarried(true);
                    groom.setMaryRequest(false);
                    bride.setMarried(true);
                    bride.setMaryRequest(false);

					if (Config.L2JMOD_WEDDING_BOW)
					{
						bride.addItem("WeddingBow", 9140, 1, bride, true);
						groom.addItem("WeddingBow", 9140, 1, groom, true);
					}

                    groom.broadcastPacket(new MagicSkillUser(groom, groom, 2025, 1, 1, 0));
                    bride.broadcastPacket(new MagicSkillUser(bride, bride, 2025, 1, 1, 0));
					prist.sayString("�������� ��� ����� � �����! ������ ���������� ���� �����!", 18);

                    Announcements.getInstance().announceToAll("����������� ����������� " + groom.getName() + " � " + bride.getName() + "! ����� � ������� ���!");
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 3000, false);

					if (Config.WEDDING_COLORS.nick != 16777215)
					{
						groom.getAppearance().setNameColor(Config.WEDDING_COLORS.nick);
						groom.broadcastUserInfo();
						groom.store();
					}
					if (Config.WEDDING_COLORS.title != 16777215)
					{
						bride.getAppearance().setNameColor(Config.WEDDING_COLORS.title);
						bride.broadcastUserInfo();
						bride.store();
					}
					break;
				case 11:
					prist.sayString("��������� ������� ��� ����� ����� �������������� � ����������� ���������, �������� ��!", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 3000, false);
					break;
				case 12:
					prist.sayString("�������� � ������� � ����� ����, ������� �������� ��� ��������� � ��������� ������!", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 3000, false);
					break;
				case 13:
					prist.sayString("������� ��� � ��������� ������������!", 18);
					ThreadPoolManager.getInstance().scheduleAi(new Say(next), 3000, false);
					break;
				case 14:
					married = true;
					prist.sayString("�������� ���� �����! ������ ���������!!!", 18);
					prist.sayString("������� ���������!!!", 18);
					prist.finish(false);
					break;
				case 99:
					prist.finish(true);
					break;
			}
		}
	}

	private class Step implements Runnable
	{
		int id;
		Step(int id)
		{
			this.id = id;
		}

		public void run()
		{	
			switch (id)
			{
				case 1: // ���������� ������
					prist.sayString(groom.getName() + "! �������� �� �� ����� � ���� " + bride.getName() + "?", 18);
					groom.setEngageRequest(true, groom.getObjectId());
					groom.sendPacket(new ConfirmDlg(614, "�������� �� �� ����� � ���� " + bride.getName() + "?"));
					break;
				case 2: // ���������� �������
					prist.sayString(bride.getName() + "! �������� �� �� ����� � ����� " + groom.getName() + "?", 18);
					bride.setEngageRequest(true, groom.getObjectId());
					bride.sendPacket(new ConfirmDlg(614, "�������� �� �� ����� � ����� " + groom.getName() + "?"));
					break;
				case 3: // ��� ��������
					if (groomYes && brideYes)
						ThreadPoolManager.getInstance().scheduleAi(new Say(8), 1000, false);
					else
						ThreadPoolManager.getInstance().scheduleAi(new Say(99), 1000, false);
					break;
			}
		}
	}

	public void sayYes(L2PcInstance player)
	{
		if (player.equals(groom))
		{
			groomYes = true;
			groom.sayString(groom.getName() + ": � ��������!", 18);
			ThreadPoolManager.getInstance().scheduleAi(new Step(2), 1000, false);
		}
		else
		{
			brideYes = true;
			bride.sayString(bride.getName() + ": � ��������!", 18);
			ThreadPoolManager.getInstance().scheduleAi(new Step(3), 1000, false);
		}
	}
	public void sayNo(L2PcInstance player)
	{
		if (player.equals(groom))
		{
			if (groom != null)
				groom.sayString("���!", 18);
			ThreadPoolManager.getInstance().scheduleAi(new Say(99), 100, false);
		}
		else
		{
			if (bride != null)
				bride.sayString("���!", 18);
			ThreadPoolManager.getInstance().scheduleAi(new Say(99), 100, false);
		}
	}
	
	public void broadcastHtml(String text)
	{
        if(groom != null && groom.getParty() != null)
			groom.getParty().broadcastHtmlToPartyMembers(text);
	}

    public L2PcInstance getGroom() 
	{ 
		return groom; 
	}

    public L2PcInstance getBride() 
	{ 
		return bride; 
	}

	public boolean isExpired()
	{
		return (System.currentTimeMillis() > expire);
	}
	
	public void clear()
	{
		groom = null;
		bride = null;
		prist = null;
	}
}
