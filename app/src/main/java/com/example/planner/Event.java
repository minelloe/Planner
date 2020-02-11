package com.example.planner;

public class Event {
    private String name;
    private String date;
    private String notes;
    private String time;

    public Event(String Name, String Date, String Notes, String Time){
        this.name = Name;
        this.date = Date;
        this.notes = Notes;
        this.time = Time;
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
}
