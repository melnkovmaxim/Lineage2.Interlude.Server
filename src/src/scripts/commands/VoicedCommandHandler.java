/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package scripts.commands;

import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import ru.agecold.Config;
import scripts.commands.voicedcommandhandlers.*;
import ru.agecold.util.log.AbstractLogger;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class VoicedCommandHandler {

    private static Logger _log = AbstractLogger.getLogger(VoicedCommandHandler.class.getName());
    private static VoicedCommandHandler _instance;
    private Map<String, IVoicedCommandHandler> _datatable;

    public static VoicedCommandHandler getInstance() {
        if (_instance == null) {
            _instance = new VoicedCommandHandler();
        }
        return _instance;
    }

    private VoicedCommandHandler() {
        _datatable = new FastMap<String, IVoicedCommandHandler>();
        if (Config.CMD_ADENA_COL) {
            registerVoicedCommandHandler(new AdenaCol());
        }
        if (Config.CMD_EVENTS) {
            registerVoicedCommandHandler(new Events());
        }
        if (Config.CMD_MENU) {
            registerVoicedCommandHandler(new Menu());
        }
        if (Config.ALT_ALLOW_OFFLINE_TRADE) {
            registerVoicedCommandHandler(new Offline());
        }
        if (Config.L2JMOD_ALLOW_WEDDING) {
            registerVoicedCommandHandler(new Wedding());
        }
        registerVoicedCommandHandler(new Silence());

        registerVoicedCommandHandler(new BlockBuff());
        registerVoicedCommandHandler(new Security());
        registerVoicedCommandHandler(new ServerTime());
        //����������
        registerVoicedCommandHandler(new ModBanChat());
        registerVoicedCommandHandler(new ModKick());
        registerVoicedCommandHandler(new ModTitle());
        registerVoicedCommandHandler(new ModHelp());
        registerVoicedCommandHandler(new ModSpecial());
        registerVoicedCommandHandler(new Acp());
        registerVoicedCommandHandler(new Autofarm());
        _log.config("VoicedCommandHandler: Loaded " + _datatable.size() + " handlers.");
    }

    public void registerVoicedCommandHandler(IVoicedCommandHandler handler) {
        String[] ids = handler.getVoicedCommandList();
        for (int i = 0; i < ids.length; i++) {
            //if (Config.DEBUG) _log.fine("Adding handler for command "+ids[i]);
            _datatable.put(ids[i], handler);
        }
    }

    public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand) {
        String command = voicedCommand;
        if (voicedCommand.indexOf(" ") != -1) {
            command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
        }
        //if (Config.DEBUG)
        //_log.fine("getting handler for command: "+command+" -> "+(_datatable.get(command) != null));
        return _datatable.get(command);
    }

    /**
     * @return
     */
    public int size() {
        return _datatable.size();
    }
}
