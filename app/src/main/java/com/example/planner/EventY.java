package com.example.planner;

public class EventY {
    private String name;
    private String date;
    private String notes;
    private String time;
    private String additionalNotification;

    public EventY(String Name, String Date, String Notes, String Time, String AdditionalNotification){
        this.name = Name;
        this.date = Date;
        this.notes = Notes;
        this.time = Time;
        this.additionalNotification = AdditionalNotification;
    }

    //NAME
    public String getName(){
        return name;
    }

    public void setName(String Name){
        this.name = Name;
    }

    //DATE
    public String getDate(){
        return date;
    }

    public void setDate(String Date){
        this.date = Date;
    }

    //NOTES
    public String getNotes(){
        return notes;
    }

    public void setNotes(String Notes){
        this.notes = Notes;
    }

    //TIME
    public String getTime(){
        return time;
    }

    public void setTime(String Time){
        this.time = Time;
    }

    //ADDITIONAL NOTIFICATION
    public String getAdditionalNotification(){
        return additionalNotification;
    }

    public void setAdditionalNotification(String AdditionalNotification){
        this.additionalNotification = AdditionalNotification;
    }
}
