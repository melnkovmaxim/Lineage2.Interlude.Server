package ru.agecold.gameserver.network.serverpackets;

import javolution.util.FastMap;

import ru.agecold.Config;
import ru.agecold.util.Location;

public class ConfirmDlg extends L2GameServerPacket {

    private int _requestId;
    private int _time;
    private int _type = 0;
    private FastMap<Integer, Point> _points = new FastMap<Integer, Point>();

    static class Point {

        public String name;
        public Location loc;

        Point(String name) {
            this.name = name;
        }

        Point(Location loc) {
            this.loc = loc;
        }
    }

    public ConfirmDlg(int requestId, String requestorName, int type) {
        _type = type;
        _requestId = requestId;
        switch (_requestId) {
            case 100: // �����
                _time = 10000;
                break;
            case 614: // �������
                _time = Config.WEDDING_ANSWER_TIME;
                break;
            case 1510: // ���
                _time = Config.RESURECT_ANSWER_TIME;
                break;
            case 1842: // ������ ����
                _time = Config.SUMMON_ANSWER_TIME;
                break;
        }

        if (_type > 1) {
            _time = 7000;
        }
        _points.put(0, new Point(requestorName));
    }

    public ConfirmDlg(int requestId, String requestorName, int type, int time)
    {
        _type = type;
        _requestId = requestId;
        switch (_requestId)
        {
            case 100:
                _time = 10000;
                break;
            case 614:
                _time = Config.WEDDING_ANSWER_TIME;
                break;
            case 1510:
                _time = Config.RESURECT_ANSWER_TIME;
                break;
            case 1842:
                _time = Config.SUMMON_ANSWER_TIME;
                break;
        }

        if (_type > 1) {
            _time = 7000;
        }
        if (time != 0) {
            _time = time;
        }
        _points.put(0, new Point(requestorName));
    }

    public ConfirmDlg(int requestId, String requestorName) {
        _requestId = requestId;
        switch (_requestId) {
            case 100: // �����
                _time = 10000;
                break;
            case 614: // �������
                _time = Config.WEDDING_ANSWER_TIME;
                break;
            case 1510: // ���
                _time = Config.RESURECT_ANSWER_TIME;
                break;
            case 1842: // ������ ����
                _time = Config.SUMMON_ANSWER_TIME;
                break;
        }
        _points.put(0, new Point(requestorName));
    }

    public void addLoc(Location loc) {
        _points.put(7, new Point(loc));
    }

    public Location getLoc() {
        return _points.get(7).loc;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xed);
        writeD(_requestId);
        writeD(_points.size()); // ??

        for (FastMap.Entry<Integer, Point> e = _points.head(), end = _points.tail(); (e = e.getNext()) != end;) {
            Integer type = e.getKey(); // No typecast necessary.
            Point value = e.getValue(); // No typecast necessary.
            if (type == null || value == null) {
                continue;
            }

            writeD(type);
            switch (type) {
                case 0:
                    writeS(value.name);
                    break;
                case 7:
                    Location loc = value.loc;
                    writeD(loc.x);
                    writeD(loc.y);
                    writeD(loc.z);
                    break;
            }
        }

        writeD(_time); // ??
        writeD(_type); // ??
    }

    public void clearPoints() {
        //_points.clear();
        //_points = null;
    }
}
