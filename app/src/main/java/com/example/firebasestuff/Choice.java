package com.example.firebasestuff;

public class Choice {
    private String foodType;
    private String day;
    private String hour;

    public Choice(String foodType, String day, String hour) {
        this.foodType = foodType;
        this.day = day;
        this.hour = hour;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }
}
