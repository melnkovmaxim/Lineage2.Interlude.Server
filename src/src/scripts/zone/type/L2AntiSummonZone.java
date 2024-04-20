/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.zone.type;

import ru.agecold.gameserver.model.L2Character;
import scripts.zone.L2ZoneType;

/**
 *
 * @author �������������
 */
public class L2AntiSummonZone extends L2ZoneType {

    public L2AntiSummonZone(int id) {
        super(id);
    }

    @Override
    protected void onEnter(L2Character character) {
        character.setSummonPenalty(true);
    }

    @Override
    protected void onExit(L2Character character) {
        character.setSummonPenalty(false);
    }

    @Override
    public void onDieInside(L2Character character) {
    }

    @Override
    public void onReviveInside(L2Character character) {
    }
}