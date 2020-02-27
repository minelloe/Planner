package com.example.planner;

//renamed due to a package, Y has no meaning
public class EventY {

    private String name;
    private String date;
    private String notes;
    private String time;
    private String additionalNotification;
    private String tags;

    //constructor
    public EventY(String Name, String Date, String Notes, String Time, String AdditionalNotification, String Tags){
        this.name = Name;
        this.date = Date;
        this.notes = Notes;
        this.time = Time;
        this.additionalNotification = AdditionalNotification;
        this.tags = Tags;
    }

    //NAME GET & SET
    public String getName(){
        return name;
    }

    public void setName(String Name){
        this.name = Name;
    }

    //DATE GET & SET
    public String getDate(){
        return date;
    }

    public void setDate(String Date){
        this.date = Date;
    }

    //NOTES GET & SET
    public String getNotes(){
        return notes;
    }

    public void setNotes(String Notes){
        this.notes = Notes;
    }

    //TIME GET & SET
    public String getTime(){
        return time;
    }

    public void setTime(String Time){
        this.time = Time;
    }

    //ADDITIONAL NOTIFICATION GET & SET
    public String getAdditionalNotification(){
        return additionalNotification;
    }

    public void setAdditionalNotification(String AdditionalNotification){
        this.additionalNotification = AdditionalNotification;
    }

    //TAGS GET & SET
    public String getTags(){
        return tags;
    }

    public void setTags(String Tags){
        this.tags = Tags;
    }
}
