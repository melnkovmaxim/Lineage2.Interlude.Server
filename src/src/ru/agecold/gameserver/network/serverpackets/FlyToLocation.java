package ru.agecold.gameserver.network.serverpackets;

public class FlyToLocation extends L2GameServerPacket
{
    private int _charObjId, _x, _y, _z, _xDst, _yDst, _zDst, _type;

    public FlyToLocation(int _charObjId, int _x, int _y, int _z, int _xDst, int _yDst, int _zDst, int _type) {
        this._charObjId = _charObjId;
        this._x = _x;
        this._y = _y;
        this._z = _z;
        this._xDst = _xDst;
        this._yDst = _yDst;
        this._zDst = _zDst;
        this._type = _type;
    }
    
    //type: 0 ������, 1 �����, 2+ ������������

    @Override
    protected final void writeImpl() {
        writeC(0xC5);

        writeD(_charObjId);

        writeD(_xDst);
        writeD(_yDst);
        writeD(_zDst);

        writeD(_x);
        writeD(_y);
        writeD(_z);

        writeD(_type);
    }
}
