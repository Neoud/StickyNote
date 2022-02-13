package com.example.stickynote;

public class Note
{
    private int id;
    private String title;
    private String content;
    private  String password;

    public Note(int id, String title, String content, String password)
    {
        this.id = id;
        this.title = title;
        this.content = content;
        this.password = password;
    }

    public Note(int id, String title, String content)
    {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public Note(String title, String content)
    {
        this.title = title;
        this.content = content;
    }

    public Note(String title, String content, String password)
    {
        this.title = title;
        this.content = content;
        this.password = password;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setContent(String content)
    {
        this.content = content;
    }


    public int getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getContent()
    {
        return content;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
