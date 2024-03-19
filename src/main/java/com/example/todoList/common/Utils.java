package com.example.todoList.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.sql.Date;

public class Utils {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 文字列をyyyy-MM-dd型の日付としてjava.sql.Dateオブジェクトへ変換する。<br>
     *
     * @param s 変換対象
     * @return 変換結果(引数がnull, "", yyyy-MM-dd形式でなければnullを返す)
     */

    public static Date str2date(String s) {
        if (s == null || s.equals("")) {
            return null;
        }

        long ms = 0;
        try {
            ms = sdf.parse(s).getTime();
        } catch (ParseException e) {
            return null;
        }
        return new Date(ms);
    }

    /**
     * 文字列をyyyy-MM-dd型の日付としてjava.sql.Dateオブジェクトへ変換する。<br>
     *
     * @param s 変換対象
     * @return 変換結果(変換できなければnullを返す)
     */
    public static Date str2dateOrNull(String s) {
        Date date = null;

        try {
            long ms = sdf.parse(s).getTime();
            // yyyy-MM-ddで解釈できた場合
            date = new Date(ms);

        } catch (ParseException e) {
            // 変換できなかった場合
            // date は null のまま
        }

        return date;
    }

    /**
     * 引数が全角SPACEだけで構成されていればtrueを返す
     *
     * @param s チェック対象
     * @return true:全角SPACEのみ, "", null false:左記以外
     */
    public static boolean isAllDoubleSpace(String s) {
        if (s == null || s.equals("")) {
            return true;
        }

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '　') { // 全角SPACE
                return false;
            }
        }
        return true;
    }

    /**
     * 引数が"" or 半角SPACE/TABだけで構成されているならtrueを返す
     *
     * @param s チェック対象
     * @return true:半角SPACE/TABのみ or "", null, false:左記以外
     */
    public static boolean isBlank(String s) {
        if (s == null || s.equals("")) {
            return true;
        }

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ' && s.charAt(i) != '\t') {// 半角SPACE
                return false;
            }
        }
        return true;
    }

    /**
     * 引数がyyyy-mm-ddと解釈できればtrueを返す
     *
     * @param str チェック対象
     * @return true:yyyy-mm-ddと解釈できる、false:解釈できない or 引数がnull or ""
     */
    public static boolean isValidDateFormat(String str) {
        if (str == null || str.equals("")) {
            return false;
        }

        try {
            // parseできればyyyy-mm-dd形式とみなす
            LocalDate.parse(str);
            return true;

        } catch (DateTimeException e) {
            // yyyy-mm-dd形式以外
            return false;
        }
    }

    /**
     * 引数が今日以降の日付を表す文字列(yyyy-MM-dd形式)ならtrueを返す
     *
     * @param str チェック対象
     * @return true:今日以降、false:昨日以前、引数がyyyy-MM-dd形式でない or null or ""
     */
    public static boolean isTodayOrFurtureDate(String str) {
        if (str == null || str.equals("")) {
            return false;
        }

        LocalDate tody = LocalDate.now();
        LocalDate deadlineDate = null;
        try {
            // "yyyy-mm-dd" -> LocalDate
            deadlineDate = LocalDate.parse(str);
            if (deadlineDate.isBefore(tody)) {
                // 過去日付なのでfalse
                return false;

            } else {
                return true;
            }

        } catch (DateTimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * java.sql.Dateオブジェクトをyyyy-MM-dd形式の文字列へ変換する。<br>
     *
     * @param date 変換対象
     * @return 変換結果(引数がnullなら""を返す)
     */
    public static String date2str(Date date) {
        String s = "";
        if (date != null) {
            s = sdf.format(date);
        }

        return s;
    }
}
