package ru.agecold.gameserver.network.serverpackets;

import ru.agecold.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author  devScarlet
 */
public class TitleUpdate extends L2GameServerPacket
{
	private String _title;
	private int _objectId;
	private boolean can_writeimpl = false;

	public TitleUpdate(L2PcInstance cha)
	{
		_objectId = cha.getObjectId();
		_title = cha.getTitle();
		
		if(_title.length() > 16)
		{
			cha.sendMessage("�� ����� 16 ��������");
			return;
		}

		can_writeimpl = true;
	}

	/**
	 * @see ru.agecold.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()	
	{	
		if(!can_writeimpl)
			return;
			
		writeC(0xcc);
		writeD(_objectId);
		writeS(_title);
	}

	/**
	 * @see ru.agecold.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return "S.TitleUpdate";
	}
}
