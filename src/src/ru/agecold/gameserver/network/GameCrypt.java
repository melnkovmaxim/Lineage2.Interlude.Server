/* This program is free software; you can redistribute it and/or modify
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
package ru.agecold.gameserver.network;

import ru.agecold.gameserver.network.smartguard.SmartGuard;
import smartguard.api.integration.SmartCrypt;
import smartguard.core.properties.GuardProperties;

/**
 * @author KenM
 */
public class GameCrypt
{
	private final byte[] _inKey = new byte[16];
	private final byte[] _outKey = new byte[16];
	private boolean _isEnabled;

	private SmartCrypt crypt = new SmartCrypt();

	public void setKey(final byte[] key)
	{
		if(SmartGuard.isActive())
			crypt.setKey(key);
		else
		{
			System.arraycopy(key, 0, _inKey, 0, 16);
			System.arraycopy(key, 0, _outKey, 0, 16);
		}
	}

	public void setKey(final byte[] key, final boolean value)
	{
		setKey(key);
	}

	public boolean decrypt(final byte[] raw, final int offset, final int size)
	{
		if(SmartGuard.isActive())
		{
			crypt.decrypt(raw, offset, size);
		}
		else
		{
			if (!_isEnabled) {
				return true;
			}

			int temp = 0;
			for (int i = 0; i < size; i++) {
				int temp2 = raw[offset + i] & 0xFF;
				raw[offset + i] = (byte) (temp2 ^ _inKey[i & 15] ^ temp);
				temp = temp2;
			}

			int old = _inKey[8] & 0xff;
			old |= _inKey[9] << 8 & 0xff00;
			old |= _inKey[10] << 0x10 & 0xff0000;
			old |= _inKey[11] << 0x18 & 0xff000000;

			old += size;

			_inKey[8] = (byte) (old & 0xff);
			_inKey[9] = (byte) (old >> 0x08 & 0xff);
			_inKey[10] = (byte) (old >> 0x10 & 0xff);
			_inKey[11] = (byte) (old >> 0x18 & 0xff);
		}
		return true;
	}

	public void encrypt(final byte[] raw, final int offset, final int size)
	{
		if(SmartGuard.isActive())
			crypt.encrypt(raw, offset, size);
		else
		{
			if (!_isEnabled) {
				_isEnabled = true;
				return;
			}

			int temp = 0;
			for (int i = 0; i < size; i++) {
				int temp2 = raw[offset + i] & 0xFF;
				temp = temp2 ^ _outKey[i & 15] ^ temp;
				raw[offset + i] = (byte) temp;
			}

			int old = _outKey[8] & 0xff;
			old |= _outKey[9] << 8 & 0xff00;
			old |= _outKey[10] << 0x10 & 0xff0000;
			old |= _outKey[11] << 0x18 & 0xff000000;

			old += size;

			_outKey[8] = (byte) (old & 0xff);
			_outKey[9] = (byte) (old >> 0x08 & 0xff);
			_outKey[10] = (byte) (old >> 0x10 & 0xff);
			_outKey[11] = (byte) (old >> 0x18 & 0xff);
		}
	}

	public static void intToBytes(int value, byte[] array, int offset) {
		array[offset++] = (byte) (value & 0xff);
		array[offset++] = (byte) (value >> 0x08 & 0xff);
		array[offset++] = (byte) (value >> 0x10 & 0xff);
		array[offset++] = (byte) (value >> 0x18 & 0xff);
	}
}
