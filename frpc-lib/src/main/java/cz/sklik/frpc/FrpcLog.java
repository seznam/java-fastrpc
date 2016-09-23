package cz.sklik.frpc;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;

/**
 * Helper class for loging Frpc.
 * 
 * @author Jakub Janda
 *
 */
public class FrpcLog {

    private static boolean sVerbose = true;

    private static boolean sDebug   = true;

    private static boolean sInfo    = true;

    private static boolean sWarning = true;

    private static boolean sError   = true;

    public static void setLevels(boolean v, boolean d, boolean i, boolean w, boolean e) {
        sVerbose = v;
        sDebug = d;
        sInfo = i;
        sWarning = w;
        sError = e;
    }

    public static void v(String tag, String msg, Object... params) {
        if (sVerbose) {
            System.out.printf(msg, params);
        }
    }

    public static void d(String tag, String msg, Object... params) {
        if (sDebug) {
            System.out.printf(msg, params);
        }
    }

    public static void i(String tag, String msg, Object... params) {
        if (sInfo) {
            System.out.printf(msg, params);
        }
    }

    public static void w(String tag, String msg, Object... params) {
        if (sWarning) {
            System.err.printf(msg, params);
        }
    }

    public static void e(String tag, String msg, Object... params) {
        if (sError) {
            System.err.printf(msg, params);
        }
    }

    public static void wtf(String tag, String msg, Object... params) {
        System.err.printf(msg, params);
    }

    /**
     * Converts frpc object to readable String.
     * 
     * @param item
     * @return string representing frpc object structure
     */
    public static String frpcToString(Object item) {
        if (item != null) {
            return frpcToString(item, "", true).toString();
        } else {
            return "null";
        }
    }

    private static StringBuffer frpcToString(Object item, String div, boolean structured) {
        StringBuffer result = new StringBuffer();
        if (structured) {
            result.append(div);
        }

        if (item instanceof Object[]) {
            Object[] array = (Object[])item;
            if (structured) {
                result.append("(Array ");
                result.append(array.length);
                result.append(") [ \n");
            } else {
                result.append("[");
            }
            for (int i = 0; i < array.length && i < 5; i++) {
                if (structured) {
                    result.append(i);
                }
                result.append(frpcToString(array[i], div + "\t", structured) + ", ");

            }
            if (structured) {
                result.append("]\n");
            } else {
                result.append("]");
            }
            return result;
        }
        if (item instanceof List) {
            List<?> array = (List<?>)item;
            if (structured) {
                result.append("(Array ");
                result.append(array.size());
                result.append(") [ \n");
            } else {
                result.append("[");
            }
            for (int i = 0; i < array.size() && i < 5; i++) {
                if (structured) {
                    result.append(i);
                }

                result.append(frpcToString(array.get(i), div + "\t", structured) + ", ");

            }
            if (structured) {
                result.append("]\n");
            } else {
                result.append("]");
            }
            return result;
        } else if (item instanceof double[]) {
            if (structured) {
                result.append("(Array) [ \n");
            } else {
                result.append("[");
            }
            double[] array = (double[])item;
            int i = 0;
            for (double d : array) {
                result.append(frpcToString(d, div + "\t", structured) + ", ");
                i++;

                if (i > 5 && structured) {
                    result.append(div + "\t" + "...");
                    break;
                }
            }
            if (structured) {
                result.append("]\n");
            } else {
                result.append("]");
            }
            return result;
        } else if (item instanceof float[]) {
            if (structured) {
                result.append("(Array) [ \n");
            } else {
                result.append("[");
            }
            float[] array = (float[])item;
            int i = 0;
            for (float d : array) {
                result.append(frpcToString(d, div + "\t", structured) + ", ");
                i++;
                if (i > 5 && structured) {
                    result.append(div + "\t" + "...");
                    break;
                }
            }
            if (structured) {
                result.append("]\n");
            } else {
                result.append("]");
            }
            return result;
        } else if (item instanceof int[]) {
            if (structured) {
                result.append("(Array) [ \n");
            } else {
                result.append("[");
            }
            int[] array = (int[])item;
            int i = 0;
            for (int d : array) {
                result.append(frpcToString(d, div + "\t", structured) + ", ");
                i++;
                if (i > 5 && structured) {
                    result.append(div + "\t" + "...");
                    break;
                }
            }
            if (structured) {
                result.append("]\n");
            } else {
                result.append("]");
            }
            return result;
        } else if (item instanceof long[]) {
            if (structured) {
                result.append("(Array) [ \n");
            } else {
                result.append("[");
            }
            long[] array = (long[])item;
            int i = 0;
            for (long d : array) {
                result.append(frpcToString(d, div + "\t", structured) + ", ");
                i++;
                if (i > 5) {
                    result.append(div + "\t" + "...");
                    break;
                }
            }
            if (structured) {
                result.append("]\n");
            } else {
                result.append("]");
            }
            return result;
        } else if (item instanceof Float) {
            result.append(frpcToString(((Float)item).doubleValue(), div, structured)).toString();
            return result;
        } else if (item instanceof Double) {
            if (structured) {
                result.append("(Double) ");
                result.append(item);
                result.append("\n");
            } else {
                result.append(item);
            }
            return result;
        } else if (item instanceof Integer) {
            if (structured) {
                result.append("(Int) ");
                result.append((Integer)item);
                result.append("\n");
            } else {
                result.append((Integer)item);
            }
            return result;
        } else if (item instanceof Long) {
            if (structured) {
                result.append("(Long) ");
                result.append((Long)item);
                result.append("\n");
            } else {
                result.append((Long)item);
            }
            return result;
        } else if (item instanceof String) {
            if (structured) {
                result.append("(String) ");
                result.append((String)item);
                result.append("\n");
            } else {
                result.append("\"" + (String)item + "\"");
            }
            return result;
        } else if (item instanceof byte[]) {
            return result.append("(Binary)\n");
        } else if (item instanceof ByteBuffer) {
            return result.append("(Binary)\n");
        } else if (item instanceof Boolean) {
            if (structured) {
                result.append("(Bool) ");
                result.append((Boolean)item);
                result.append("\n");
            } else {
                result.append((Boolean)item ? "True" : "False");
            }
            return result;
        } else if (item instanceof GregorianCalendar) {
            GregorianCalendar date = (GregorianCalendar)item;
            String dateString = "(Calendar)timeStamp - %d;  day - %d; date - %d/%d/%d; time - %d:%d:%d; zone - %d";
            return result.append(String.format(dateString, date.getTimeInMillis() / 1000,
                    date.get(GregorianCalendar.DAY_OF_WEEK), date.get(GregorianCalendar.YEAR),
                    date.get(GregorianCalendar.MONTH), date.get(GregorianCalendar.DATE),
                    date.get(GregorianCalendar.HOUR_OF_DAY), date.get(GregorianCalendar.MINUTE),
                    date.get(GregorianCalendar.SECOND), date.get(GregorianCalendar.ZONE_OFFSET)
                            / 1000 / 60 / 15 + date.get(GregorianCalendar.DST_OFFSET) / 1000 / 60
                            / 15));
        } else if (item instanceof HashMap<?, ?>) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> struct = (HashMap<String, Object>)item;
            Set<Entry<String, Object>> items = struct.entrySet();
            Iterator<Entry<String, Object>> iter = items.iterator();
            if (structured) {
                result.append("(Struct) { \n");
            } else {
                result.append("{");
            }
            while (iter.hasNext()) {
                Entry<String, Object> entry = iter.next();
                if (structured) {
                    result.append(div);
                }
                result.append("\"" + entry.getKey() + "\"");
                if (structured) {
                    result.append(": \n");
                } else {
                    result.append(":");
                }
                result.append(frpcToString(entry.getValue(), div + "\t", structured));
                if (!structured) {
                    result.append(", ");
                }
            }
            if (structured) {
                result.append(div);
                result.append("}\n");
            } else {
                result.append("}");
            }
            return result;
        } else {
            return result.append("(Unknown) \n");
        }
    }

    /**
     * Direct log of frpc object.
     *  
     * @param logtag
     * @param item
     */
    public static void logFrpcStruct(String logtag, Object item) {
        logFrpcStruct(logtag, item, "");
    }

    private static void logFrpcStruct(String logtag, Object item, String div) {

        if (item instanceof Object[]) {
            Object[] array = (Object[])item;
            FrpcLog.d(logtag, "(Array " + array.length + ") [ \n");
            for (int i = 0; i < array.length; i++) {
                FrpcLog.d(logtag, "(%d)", i);
                logFrpcStruct(logtag, array[i], div + "\t");
            }
            FrpcLog.d(logtag, "]\n");
        } else if (item instanceof double[]) {
            FrpcLog.d(logtag, div + "(Array) [ \n");
            double[] array = (double[])item;
            for (double d : array) {
                logFrpcStruct(logtag, d, div + "\t");
            }
            FrpcLog.d(logtag, "]\n");
        } else if (item instanceof float[]) {
            FrpcLog.d(logtag, div + "(Array) [ \n");
            float[] array = (float[])item;
            for (float d : array) {
                logFrpcStruct(logtag, d, div + "\t");
            }
            FrpcLog.d(logtag, "]\n");
        } else if (item instanceof int[]) {
            FrpcLog.d(logtag, div + "(Array) [ \n");
            int[] array = (int[])item;
            for (int d : array) {
                logFrpcStruct(logtag, d, div + "\t");
            }
            FrpcLog.d(logtag, "]\n");
        } else if (item instanceof long[]) {
            FrpcLog.d(logtag, div + "(Array) [ \n");
            long[] array = (long[])item;
            for (long d : array) {
                logFrpcStruct(logtag, d, div + "\t");
            }
            FrpcLog.d(logtag, "]\n");
        } else if (item instanceof Float) {
            logFrpcStruct(logtag, ((Float)item).doubleValue(), div);
        } else if (item instanceof Double) {
            FrpcLog.d(logtag, div + "(Double) " + (Double)item + "\n");
        } else if (item instanceof Integer) {
            FrpcLog.d(logtag, div + "(Int) " + (Integer)item + "\n");
        } else if (item instanceof Long) {
            FrpcLog.d(logtag, div + "(Long) " + (Long)item + "\n");
        } else if (item instanceof String) {
            FrpcLog.d(logtag, div + "(String) " + (String)item + "\n");
        } else if (item instanceof byte[]) {
            FrpcLog.d(logtag, div + "(Binary)\n");
        } else if (item instanceof Boolean) {
            FrpcLog.d(logtag, div + "(Bool) " + (Boolean)item + "\n");
        } else if (item instanceof GregorianCalendar) {
            GregorianCalendar date = (GregorianCalendar)item;
            String dateString = div
                    + "(Calendar)timeStamp - %d;  day - %d; date - %d/%d/%d; time - %d:%d:%d; zone - %d";
            FrpcLog.d(logtag, String.format(dateString, date.getTimeInMillis() / 1000,
                    date.get(GregorianCalendar.DAY_OF_WEEK), date.get(GregorianCalendar.YEAR),
                    date.get(GregorianCalendar.MONTH), date.get(GregorianCalendar.DATE),
                    date.get(GregorianCalendar.HOUR_OF_DAY), date.get(GregorianCalendar.MINUTE),
                    date.get(GregorianCalendar.SECOND), date.get(GregorianCalendar.ZONE_OFFSET)
                            / 1000 / 60 / 15 + date.get(GregorianCalendar.DST_OFFSET) / 1000 / 60
                            / 15));
        } else if (item instanceof HashMap<?, ?>) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> struct = (HashMap<String, Object>)item;
            Set<Entry<String, Object>> items = struct.entrySet();
            Iterator<Entry<String, Object>> iter = items.iterator();
            FrpcLog.d(logtag, div + "(Struct) { \n");
            while (iter.hasNext()) {
                Entry<String, Object> entry = iter.next();
                FrpcLog.d(logtag, div + entry.getKey() + ": \n");
                logFrpcStruct(logtag, entry.getValue(), div + "\t");
            }
            FrpcLog.d(logtag, div + "}\n");
        } else {
            FrpcLog.d(logtag, div + "(Unknown) \n");
        }
    }
}
