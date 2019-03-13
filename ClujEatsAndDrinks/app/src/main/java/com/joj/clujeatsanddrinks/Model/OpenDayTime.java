package com.joj.clujeatsanddrinks.Model;

public class OpenDayTime {
    private String day;
    private String open;
    private String close;

    public OpenDayTime(String day, String open, String close) {
        this.day = day;
        this.open = open;
        this.close = close;
    }

    public OpenDayTime() {
    }

    public boolean isOpen(String day, String time) {
        boolean result = true;
        if (day.equals("anytime") || time.equals("anytime"))
            return true;
        if (!day.equals(this.day))
            result = false;
        int hour = getHour(time);
        int minute = getMinute(time);
        int openHour = getHour(open);
        int openMinute = getMinute(open);
        int closeHour = getHour(close);
        int closeMinute = getMinute(close);
        if (result) {
            if (hour < openHour) result = false;
            else {
                if (hour == openHour) {
                    result = minute > openMinute;
                }
            }
        }
        if (result) {
            if (hour > closeHour) result = false;
            else {
                if (hour == closeHour) {
                    result = minute < closeMinute;
                }
            }
        }
        return result;
    }

    int getHour(String time) {
        return Integer.parseInt(time.substring(0,2));
    }

    int getMinute (String time) {
        return Integer.parseInt(time.substring(3,5));
    }

    public String getDay() {
        return day;
    }

    public String getOpen() {
        return open;
    }

    public String getClose() {
        return close;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public void setClose(String close) {
        this.close = close;
    }

    @Override
    public String toString() {
        return day + " from " + open + " to " + close + "\n";
    }
}
