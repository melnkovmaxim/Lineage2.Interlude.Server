package ru.agecold.gameserver.network.serverpackets;

import javolution.util.FastMap;
import ru.agecold.gameserver.instancemanager.CastleManager;
import ru.agecold.gameserver.instancemanager.CastleManorManager;
import ru.agecold.gameserver.instancemanager.CastleManorManager.CropProcure;
import ru.agecold.gameserver.model.entity.Castle;

/**
 * format(packet 0xFE) ch dd [dddc] c - id h - sub id
 *
 * d - crop id d - size
 *
 * [
 * d - manor name d - buy residual d - buy price c - reward type ]
 *
 * @author l3x
 */
public class ExShowProcureCropDetail extends L2GameServerPacket {

    private int _cropId;
    private FastMap<Integer, CropProcure> _castleCrops;

    public ExShowProcureCropDetail(int cropId) {
        _cropId = cropId;
        _castleCrops = new FastMap<Integer, CropProcure>();

        for (Castle c : CastleManager.getInstance().getCastles()) {
            CropProcure cropItem = c.getCrop(_cropId, CastleManorManager.PERIOD_CURRENT);
            if (cropItem != null && cropItem.getAmount() > 0) {
                _castleCrops.put(c.getCastleId(), cropItem);
            }
        }
    }

    @Override
    public void writeImpl() {
        writeC(0xFE);
        writeH(0x22);

        writeD(_cropId); // crop id
        writeD(_castleCrops.size());       // size

        for (int manorId : _castleCrops.keySet()) {
            CropProcure crop = _castleCrops.get(manorId);
            writeD(manorId);          // manor name
            writeD(crop.getAmount()); // buy residual
            writeD(crop.getPrice());  // buy price
            writeC(crop.getReward()); // reward type
        }
    }

    @Override
    public void gc() {
        _castleCrops.clear();
        _castleCrops = null;
    }
}
