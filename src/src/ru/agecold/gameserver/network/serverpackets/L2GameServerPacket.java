package ru.agecold.gameserver.network.serverpackets;

//import java.util.logging.Logger;
import ru.agecold.gameserver.network.L2GameClient;
import org.mmocore.network.nio.impl.SendablePacket;

/**
 *
 * @author KenM
 */
public abstract class L2GameServerPacket extends SendablePacket<L2GameClient> {
    //private static final Logger _log = Logger.getLogger(L2GameServerPacket.class.getName());

    /**
     * @see org.mmocore.network.SendablePacket#write()
     */
    @Override
    public final boolean write() {
        /*System.out.println(getClient().toString() + "Server: " + getType());
         L2PcInstance activeChar = client.getActiveChar();
         if (activeChar != null && activeChar.isSpy()) {
         Log.add(TimeLogger.getLogTime() + " Player: " + activeChar.getName() + "// " + client.toString() + "Server: " + getType(), "packet_spy");
         }*/

        try {
            writeImpl();
            gc();
            return true;
        } catch (Throwable t) {
            System.out.println("Client: " + getClient() + " - Failed writing: " + getType());
            t.printStackTrace();
        }
        //System.out.println("Server-WriteImple: "+getType());
        return false;
    }

    protected abstract void writeImpl();

    /**
     * @return A String with this packet name for debuging purposes
     */
    public String getType() {
        return "S." + getClass().getSimpleName();
    }

    // ������� � ������� �� ������ ������� � ������� (���� ����) ����� �������� ���������� ������
    public void gc() {
    }
    // ������� � ������� �� ������ ������� � ������� (���� ����) ����� �������� ������ ����������

    public void gcb() {
    }

    public boolean isCharInfo() {
        return false;
    }
}
