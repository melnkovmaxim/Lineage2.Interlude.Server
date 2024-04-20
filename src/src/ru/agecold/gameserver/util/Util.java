/*
 * $Header: Util.java, 21/10/2005 23:17:40 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 21/10/2005 23:17:40 $
 * $Revision: 1 $
 * $Log: Util.java,v $
 * Revision 1  21/10/2005 23:17:40  luisantonioa
 * Added copyright notice
 *
 *
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
package ru.agecold.gameserver.util;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.regex.Pattern;
import javolution.text.TextBuilder;

import ru.agecold.Config;
import ru.agecold.L2DatabaseFactory;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.mysql.Close;
import ru.agecold.mysql.Connect;

/**
 * General Utility functions related to Gameserver
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public final class Util {

    private static final int MAX_ANGLE = 360;

    public static void handleIllegalPlayerAction(L2PcInstance actor, String message, int punishment) {
        ThreadPoolManager.getInstance().scheduleGeneral(new IllegalPlayerAction(actor, message, punishment), 5000);
    }

    public static String getRelativePath(File base, File file) {
        return file.toURI().getPath().substring(base.toURI().getPath().length());
    }

    /**
     * Return degree value of object 2 to the horizontal line with object 1
     * being the origin
     */
    public static double calculateAngleFrom(L2Object obj1, L2Object obj2) {
        if (obj1 == null || obj2 == null) {
            return 0.;
        }
        return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
    }

    /**
     * Return degree value of object 2 to the horizontal line with object 1
     * being the origin
     */
    public static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y) {
        double angleTarget = Math.toDegrees(Math.atan2(obj1Y - obj2Y, obj1X - obj2X));
        if (angleTarget <= 0) {
            angleTarget += 360;
        }
        return angleTarget;
    }

    public static double calculateDistance(int x1, int y1, int z1, int x2, int y2) {
        return calculateDistance(x1, y1, 0, x2, y2, 0, false);
    }

    public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis) {
        double dx = (double) x1 - x2;
        double dy = (double) y1 - y2;

        if (includeZAxis) {
            double dz = z1 - z2;
            return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
        } else {
            return Math.sqrt((dx * dx) + (dy * dy));
        }
    }

    public static double calculateDistance(L2Object obj1, L2Object obj2, boolean includeZAxis) {
        if (obj1 == null || obj2 == null) {
            return 1000000;
        }
        return calculateDistance(obj1.getPosition().getX(), obj1.getPosition().getY(), obj1.getPosition().getZ(), obj2.getPosition().getX(), obj2.getPosition().getY(), obj2.getPosition().getZ(), includeZAxis);
    }

    public static int calculateHeadingFrom(L2Object obj1, L2Object obj2) {
        return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
    }

    public static int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y) {
        double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
        if (angleTarget < 0) {
            angleTarget = MAX_ANGLE + angleTarget;
        }
        return (int) (angleTarget * 182.044444444);
    }

    /**
     * Capitalizes the first letter of a string, and returns the result.<BR>
     * (Based on ucfirst() function of PHP)
     *
     * @param String str
     * @return String containing the modified string.
     */
    public static String capitalizeFirst(String str) {
        str = str.trim();

        if (str.length() > 0 && Character.isLetter(str.charAt(0))) {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }

        return str;
    }

    /**
     * Capitalizes the first letter of every "word" in a string.<BR> (Based on
     * ucwords() function of PHP)
     *
     * @param String str
     * @return String containing the modified string.
     */
    public static String capitalizeWords(String str) {
        char[] charArray = str.toCharArray();
        TextBuilder result = new TextBuilder();

        // Capitalize the first letter in the given string!
        charArray[0] = Character.toUpperCase(charArray[0]);

        for (int i = 0; i < charArray.length; i++) {
            if (Character.isWhitespace(charArray[i])) {
                charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);
            }

            result.append(Character.toString(charArray[i]));
        }

        return result.toString();
    }

    // Micht: Removed this because UNUSED
    /*
     * public static boolean checkIfInRange(int range, int x1, int y1, int x2,
     * int y2) { return checkIfInRange(range, x1, y1, 0, x2, y2, 0, false); }
     *
     * public static boolean checkIfInRange(int range, int x1, int y1, int z1,
     * int x2, int y2, int z2, boolean includeZAxis) {
     *
     * if (includeZAxis) { return ((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2) +
     * (z1 - z2)*(z1 - z2)) <= range * range; } else { return ((x1 - x2)*(x1 -
     * x2) + (y1 - y2)*(y1 - y2)) <= range * range; } }
     *
     * public static boolean checkIfInRange(int range, L2Object obj1, L2Object
     * obj2, boolean includeZAxis) { if (obj1 == null || obj2 == null) return
     * false;
     *
     * return checkIfInRange(range, obj1.getPosition().getX(),
     * obj1.getPosition().getY(), obj1.getPosition().getZ(),
     * obj2.getPosition().getX(), obj2.getPosition().getY(),
     * obj2.getPosition().getZ(), includeZAxis); }
     */
    public static boolean checkIfInRange(int range, L2Object obj1, L2Object obj2, boolean includeZAxis) {
        if (obj1 == null || obj2 == null) {
            return false;
        }
        if (range == -1) {
            return true; // not limited
        }
        int rad = 0;
        if (obj1.isL2Character()) {
            rad += ((L2Character) obj1).getTemplate().collisionRadius;
        }
        if (obj2.isL2Character()) {
            rad += ((L2Character) obj2).getTemplate().collisionRadius;
        }

        double dx = obj1.getX() - obj2.getX();
        double dy = obj1.getY() - obj2.getY();

        if (includeZAxis) {
            double dz = obj1.getZ() - obj2.getZ();
            double d = dx * dx + dy * dy + dz * dz;

            return d <= range * range + 2 * range * rad + rad * rad;
        } else {
            double d = dx * dx + dy * dy;

            return d <= range * range + 2 * range * rad + rad * rad;
        }
    }

    public static double convertHeadingToDegree(int heading) {
        if (heading == 0) {
            return 360;
        }
        return 9.0 * (heading / 1610.0); // = 360.0 * (heading / 64400.0)
    }

    /**
     * Returns the number of "words" in a given string.
     *
     * @param String str
     * @return int numWords
     */
    public static int countWords(String str) {
        return str.trim().split(" ").length;
    }

    /**
     * Returns a delimited string for an given array of string elements.<BR>
     * (Based on implode() in PHP)
     *
     * @param String[] strArray
     * @param String strDelim
     * @return String implodedString
     */
    public static String implodeString(String[] strArray, String strDelim) {
        TextBuilder result = new TextBuilder();

        for (String strValue : strArray) {
            result.append(strValue + strDelim);
        }

        return result.toString();
    }

    /**
     * Returns a delimited string for an given collection of string
     * elements.<BR> (Based on implode() in PHP)
     *
     * @param Collection&lt;String&gt; strCollection
     * @param String strDelim
     * @return String implodedString
     */
    public static String implodeString(Collection<String> strCollection, String strDelim) {
        return implodeString(strCollection.toArray(new String[strCollection.size()]), strDelim);
    }

    /**
     * Returns the rounded value of val to specified number of digits after the
     * decimal point.<BR> (Based on round() in PHP)
     *
     * @param float val
     * @param int numPlaces
     * @return float roundedVal
     */
    public static float roundTo(float val, int numPlaces) {
        if (numPlaces <= 1) {
            return Math.round(val);
        }

        float exponent = (float) Math.pow(10, numPlaces);

        return (Math.round(val * exponent) / exponent);
    }

    public static boolean isAlphaNumeric(String text) {
        if (text == null) {
            return false;
        }

        boolean result = true;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isLetterOrDigit(chars[i])) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Return amount of adena formatted with "," delimiter
     *
     * @param amount
     * @return String formatted adena amount
     */
    public static String formatAdena(int amount) {
        String s = "";
        int rem = amount % 1000;
        s = Integer.toString(rem);
        amount = (amount - rem) / 1000;
        while (amount > 0) {
            if (rem < 99) {
                s = '0' + s;
            }
            if (rem < 9) {
                s = '0' + s;
            }
            rem = amount % 1000;
            s = Integer.toString(rem) + "," + s;
            amount = (amount - rem) / 1000;
        }
        return s;
    }

    // ������ ����� ����� �� ���� ��������
    public static String htmlSpecialChars(String word) {
        word = word.replaceAll("!", "&excl;");
        word = word.replaceAll("@", "&commat;");
        word = word.replaceAll("#", "&num;");
        word = word.replaceAll("\\$", "&dollar;");
        word = word.replaceAll("%", "&percnt;");
        word = word.replaceAll("\\^", "&Hat;");
        word = word.replaceAll("\\*", "&ast;");
        word = word.replaceAll("\\(", "&lpar;");
        word = word.replaceAll("\\)", "&rpar;");
        word = word.replaceAll("\\.", "&period;");
        word = word.replaceAll(",", "&comma;");
        word = word.replaceAll(";", "&semi;");
        word = word.replaceAll("/", "&sol;");
        word = word.replaceAll("\\|", "&verbar;");
        //word = val.replaceAll("&bsol;", "\");
        word = word.replaceAll("\\?", "&quest;");
        word = word.replaceAll("\\+", "&plus;");
        word = word.replaceAll(":", "&colon;");
        word = word.replaceAll("'", "&apos;");
        word = word.replaceAll("_", "&lowbar;");
        word = word.replaceAll("<", "&lt;");
        word = word.replaceAll(">", "&gt;");
        return word;
    }

    // ������ ����� ����� �� ���� ��������
    public static String htmlSpecialConvert(String word) {
        word = word.replaceAll("!", "&excl;");
        word = word.replaceAll("@", "&commat;");
        word = word.replaceAll("#", "&num;");
        word = word.replaceAll("\\$", "&dollar;");
        word = word.replaceAll("%", "&percnt;");
        word = word.replaceAll("\\^", "&Hat;");
        word = word.replaceAll("\\*", "&ast;");
        word = word.replaceAll("\\(", "&lpar;");
        word = word.replaceAll("\\)", "&rpar;");
        word = word.replaceAll("\\.", "&period;");
        word = word.replaceAll(",", "&comma;");
        word = word.replaceAll(";", "&semi;");
        word = word.replaceAll("/", "&sol;");
        word = word.replaceAll("\\|", "&verbar;");
        //word = val.replaceAll("&bsol;", "\");
        word = word.replaceAll("\\?", "&quest;");
        word = word.replaceAll("\\+", "&plus;");
        word = word.replaceAll(":", "&colon;");
        word = word.replaceAll("'", "&apos;");
        word = word.replaceAll("_", "&lowbar;");
        word = word.replaceAll("<", "&lt;");
        word = word.replaceAll(">", "&gt;");
        return word;
    }

    // �������������� IP
    public static String intToIp(int i) {
        return ((i >> 24) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "."
                + ((i >> 8) & 0xFF) + "."
                + (i & 0xFF);
    }

    public static Integer ipToInt(String addr) {
        long num = 0;
        String[] len = addr.split("\\.");
        for (int i = 0; i < len.length; i++) {
            int power = 3 - i;
            num += ((Integer.parseInt(len[i]) % 256 * Math.pow(256, power)));
        }
        return (int) num;
    }
    // ��������� double
    private final static double EPSILON = 0.00001;

    public static boolean doubleEquals(double a, double b) {
        return a == b ? true : Math.abs(a - b) < EPSILON;
    }
    private static final Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    public static boolean isValidEmail(String text) {
        return emailPattern.matcher(text).matches();
    }

    public static boolean isValidName(L2PcInstance player, String name) {
        if (name.length() < 3 || name.length() > 16) {
            player.sendHtmlMessage("��� ������ ���� ����� 3 � ����� 16 ��������.");
            return false;
        }

        if (name.startsWith("GM.") || name.startsWith("ADM.") || name.startsWith("EGM.") || name.startsWith("-")) {
            player.sendHtmlMessage("����������� ���.");
            return false;
        }

        if (name.endsWith(".GM") || name.endsWith(".ADM") || name.endsWith(".EGM") || (!Config.PREMIUM_NAME_PREFIX.isEmpty() && name.endsWith(Config.PREMIUM_NAME_PREFIX))) {
            player.sendHtmlMessage("����������� ���.");
            return false;
        }

        if (!isValidName(name)) {
            return false;
        }

        return true;
    }

    private static final Pattern cnamePattern = Pattern.compile(Config.DON_CNAME_TEMPLATE);//Pattern.compile("[\\w\\u005F\\u002E]+", Pattern.UNICODE_CASE);

    private static boolean isValidName(String text) {
        /* Pattern pattern;
         try {
         pattern = Pattern.compile(Config.CNAME_TEMPLATE);
         } catch (PatternSyntaxException e) // case of illegal pattern
         {
         _log.warning("ERROR : Character name pattern of config is wrong!");
         pattern = Pattern.compile(".*");
         }*/

        return cnamePattern.matcher(text).matches();
    }

    public static boolean isExistsName(String name) {
        Connect con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            con = L2DatabaseFactory.get();
            con.setTransactionIsolation(1);
            st = con.prepareStatement("SELECT acc FROM `characters` WHERE `char_name` = ? LIMIT 0,1");
            st.setString(1, name);
            rs = st.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (final Exception e) {
            System.out.println("[ERROR] Util, isExistsName() error: " + e);
            return true;
        } finally {
            Close.CSR(con, st, rs);
        }
        return false;
    }
    private static VoteRefName _vrn;

    public static interface VoteRefName {

        public String findRefName(String name);
    }

    public static class VoteRefNameDefault implements VoteRefName {

        @Override
        public String findRefName(String name) {
            for (int i = (name.length() - 1); i >= 0; i--) {
                if (!Pattern.matches("^\\d*$", String.valueOf(name.charAt(i)))) {
                    break;
                }
                name = name.substring(0, i);
            }
            return name;
        }
    }

    public static class VoteRefNameNcs implements VoteRefName {

        @Override
        public String findRefName(String name) {
            if (name.length() < 8) {
                return name;
            }
            return name.substring(0, name.length() - 7);
        }
    }

    public static void setVoteRefMethod() {
        if (Config.VOTE_NCS_SPAWN) {
            _vrn = new VoteRefNameNcs();
        } else {
            _vrn = new VoteRefNameDefault();
        }
    }

    public static String findRefName(String name) {
        return _vrn.findRefName(name);
    }

    public static String checkServerVotePrefix(String name) {
        if (Config.VOTE_SERVER_PREFIX.isEmpty()) {
            return name;
        }

        if (name.startsWith(Config.VOTE_SERVER_PREFIX)) {
            String[] tmp = name.split("-");
            if (tmp.length == 2) {
                if (Config.VOTE_SERVER_PREFIX.equalsIgnoreCase(tmp[0])) {
                    name = tmp[1];
                } else {
                    name = "";
                }
            }
        }
        return name;
    }
    private static final Pattern safeSql = Pattern.compile("[A-Za-z0-9\\- ]{3,16}");

    public static boolean isValidStringAZ(String text) {
        return safeSql.matcher(text).matches();
    }

    //
    public static boolean isForbidden(String text) {
        for (String bad : Config.SPAM_FILTER_LIST) {
            if (bad.isEmpty()) {
                continue;
            }

            if (text.matches(".*" + bad + ".*")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSpamText(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("[^A-Za-z�-��-�0-9]", "");

        for (String bad : Config.SPAM_FILTER_LIST) {
            if (bad.isEmpty()) {
                continue;
            }

            if (text.matches(".*" + bad + ".*")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSpamDigitText(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("\\D", "");

        for (String bad : Config.SPAM_FILTER_DIGITS_LIST) {
            if (bad.isEmpty()) {
                continue;
            }

            if (text.matches(".*" + bad + ".*")) {
                return true;
            }
        }
        return false;
    }

    public static String replaceIdent(String word) {
        word = word.toLowerCase();
        word = word.replace("a", "�");
        word = word.replace("c", "�");
        word = word.replace("s", "�");
        word = word.replace("e", "�");
        word = word.replace("k", "�");
        word = word.replace("m", "�");
        word = word.replace("o", "�");
        word = word.replace("0", "�");
        word = word.replace("x", "�");
        word = word.replace("uy", "��");
        word = word.replace("y", "�");
        word = word.replace("u", "�");
        word = word.replace("�", "�");
        word = word.replace("9", "�");
        word = word.replace("3", "�");
        word = word.replace("z", "�");
        word = word.replace("d", "�");
        word = word.replace("p", "�");
        word = word.replace("i", "�");
        word = word.replace("ya", "�");
        word = word.replace("ja", "�");
        return word;
    }

    public static boolean isForBroadcst(String text) {
        text = text.toLowerCase();

        text = text.replaceAll("\n", "");
        text = text.replace("\n", "");
        text = text.replace("n\\", "");
        text = text.replace("\r", "");
        text = text.replace("r\\", "");

        text = ltrim(text);
        text = rtrim(text);
        text = itrim(text);
        text = lrtrim(text);

        //text = replaceIdent(text);

        //text = text.replaceAll("\\W", "");
        text = text.replaceAll("[^A-Za-z�-��-�0-9]", "");
        for (String bad : Config.CHAT_GM_BROADCAST_LIST) {
            if (bad.isEmpty()) {
                continue;
            }

            if (text.matches(".*" + bad + ".*")) {
                return true;
            }
        }
        return false;
    }

    public static String ltrim(String source) {
        return source.replaceAll("^\\s+", "");
    }

    public static String rtrim(String source) {
        return source.replaceAll("\\s+$", "");
    }

    public static String itrim(String source) {
        return source.replaceAll("\\b\\s{2,}\\b", " ");
    }

    public static String trim(String source) {
        return itrim(ltrim(rtrim(source)));
    }

    public static String lrtrim(String source) {
        return ltrim(rtrim(source));
    }
    private static final Pattern ruPattern = Pattern.compile("[�-��-�0-9~!@#$%^&*()_+:<>]{3,16}$");
    private static final Pattern enPattern = Pattern.compile("[A-Za-z0-9~!@#$%^&*()_+:<>]{3,16}$");

    public static boolean isNumEn(String string) {
        return enPattern.matcher(string).matches();
    }

    public static boolean isNumRu(String string) {
        return ruPattern.matcher(string).matches();
    }

    public static boolean isAllowedRegExp(String string) {
        return ruPattern.matcher(string).matches() != enPattern.matcher(string).matches();
    }

    public static void append(StringBuilder sb, Object... content)
    {
        for(Object obj : content)
            sb.append(obj == null ? null : obj.toString());
    }
}
