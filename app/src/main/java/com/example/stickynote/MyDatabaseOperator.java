package com.example.stickynote;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class MyDatabaseOperator
{
    Context context;
    MyOpenHelper myHelper;
    SQLiteDatabase myDatabase;

    public MyDatabaseOperator(Context context)
    {
        this.context = context;
        myHelper = new MyOpenHelper(context);
    }

    public ArrayList<Note> getArray()
    {
        ArrayList<Note> array = new ArrayList<Note>();
        ArrayList<Note> array1 = new ArrayList<Note>();
        myDatabase = myHelper.getWritableDatabase();
        Cursor cursor = myDatabase.rawQuery("select id,title,password,content from mynote",null);

        // 如果光标移至开头
        if(cursor.moveToFirst())
        {
            do
            {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String password = cursor.getString(cursor.getColumnIndex("password"));
                Note note = new Note(id,title,content,password);
                array.add(note);
            } while(cursor.moveToNext());
        }

        myDatabase.close();

        // 从note表的最后一项至第一项
        for (int i = array.size() ; i > 0 ; i--)
        {
            array1.add(array.get(i-1));
        }

        return array1;
    }

    public Note getTiandCon(int id)
    {
        myDatabase = myHelper.getWritableDatabase();
        Cursor cursor = myDatabase.rawQuery("select title , content , password from mynote where id = '"+id+"'",null);
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex("title"));
        String content = cursor.getString(cursor.getColumnIndex("content"));
        String password = cursor.getString(cursor.getColumnIndex("password"));
        Note note = new Note(title,content,password);
        myDatabase.close();
        return note;
    }

    public void toUpdate(Note note)
    {
        myDatabase = myHelper.getWritableDatabase();
        myDatabase.execSQL(
                "update mynote set title='" + note.getTitle()
                        + "',content='" + note.getContent()
                        + "',password='" + note.getPassword()
                        + "' where id='" + note.getId() + "'"
        );
        myDatabase.close();
    }

    public void toUpdate2(Note note)
    {
        myDatabase = myHelper.getWritableDatabase();
        myDatabase.execSQL(
                "update mynote set title='" + note.getTitle()
                        + "',content='" + note.getContent()
                        + "' where id='" + note.getId() + "'"
        );
        myDatabase.close();
    }

    public void toInsert(Note note)
    {
        myDatabase = myHelper.getWritableDatabase();
        myDatabase.execSQL("insert into mynote (title,password,content) values('"
               + note.getTitle() + "','"
                + note.getPassword() +"','"
               + note.getContent() + "')");
        myDatabase.close();
    }

    public void toDelete(int id)
    {
        myDatabase = myHelper.getWritableDatabase();
        myDatabase.execSQL("delete  from mynote where id ="+id+"");
        myDatabase.close();
    }
}
