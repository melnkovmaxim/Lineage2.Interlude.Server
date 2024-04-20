/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.zone.type;

import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import scripts.zone.L2ZoneType;

/**
 *
 * @author �������������
 */
public class L2SpecZone extends L2ZoneType {

    /*
    1-� �������� - ������ �� ����� �� ����
    2-� �������� - ������ ������
    3-� �������� - ���� ����� ����� �� ���� (���-��) ��� ������ ����� -  ���� � ����� ���������������
    4-� �������� - ������ �� ������
     */
    private boolean _partyPenalty = false;
    private boolean _logoutPenalty = false;
    private boolean _exitPenalty = false;
    private boolean _summonPenalty = false;

    public L2SpecZone(int id) {
        super(id);
    }

    @Override
    public void setParameter(String name, String value) {
        if (name.equals("partyPenalty")) {
            _partyPenalty = Boolean.parseBoolean(value);
        } else if (name.equals("logoutPenalty")) {
            _logoutPenalty = Boolean.parseBoolean(value);
        } else if (name.equals("exitPenalty")) {
            _exitPenalty = Boolean.parseBoolean(value);
        } else if (name.equals("summonPenalty")) {
            _summonPenalty = Boolean.parseBoolean(value);
        } else {
            super.setParameter(name, value);
        }
    }

    @Override
    protected void onEnter(L2Character character) {
        checkPlayer(character.getPlayer(), true);
    }

    @Override
    protected void onExit(L2Character character) {
        checkPlayer(character.getPlayer(), true);
    }

    @Override
    public void onDieInside(L2Character character) {
    }

    @Override
    public void onReviveInside(L2Character character) {
    }

    private void checkPlayer(L2PcInstance player, boolean f) {
        if (_partyPenalty) {
            player.setPartyExitPenalty(f);
        }
        if (_logoutPenalty) {
            player.setLogoutPenalty(f);
        }
        if (_exitPenalty) {
            player.setExitPenalty(f);
        }
        if (_summonPenalty) {
            player.setSummonPenalty(f);
        }
    }
}
