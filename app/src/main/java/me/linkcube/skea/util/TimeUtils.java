package me.linkcube.skea.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ervinwang on 2014/12/19.
 */
public class TimeUtils {

    public static int getAgeByBirthday(String birthday) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        try {
            date = sdf.parse(birthday);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        long now = (new Date()).getTime();
        long Birthdate = date.getTime();
        long time = now - Birthdate;
        int count = 0;
        long days = time / 1000 / 60 / 60 / 24;
        int birthYear = Integer.parseInt((birthday.substring(0, 4)));
        for (int i = calendar.get(Calendar.YEAR); i >= birthYear; i--)
            if ((i % 4 == 0 && !(i % 100 == 0)) || (i % 400 == 0)) {
                count++;
            }
        int age = ((int) days - count) / 365;
        return age;
    }
}
