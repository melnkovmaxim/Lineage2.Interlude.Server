/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.agecold.gameserver.network.serverpackets;

/**
 *
 * @author �������������
 */
public class NetPing extends L2GameServerPacket {

    private final int _time;

    public NetPing(int time) {
        _time = time;
    }

    @Override
    protected void writeImpl() {
        writeC(0xD3);
        writeD(_time);
    }
}
