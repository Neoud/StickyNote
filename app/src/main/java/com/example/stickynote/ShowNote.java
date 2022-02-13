package com.example.stickynote;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class ShowNote extends AppCompatActivity
{
    ImageButton imageButton;
    ListView lv;
    LayoutInflater inflater;
    ArrayList<Note> array;
    MyDatabaseOperator mdb;
    private String password = null;
    Note note;
    Intent intent;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_note);

        imageButton = findViewById(R.id.btnNew);
        lv=findViewById(R.id.lvNote);
        inflater=getLayoutInflater();
        mdb=new MyDatabaseOperator(this);
        array=mdb.getArray();
        MyAdapter adapter = new MyAdapter(ShowNote.this,R.layout.list_item,array);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                note = array.get(position);
                int id2 = note.getId();
                intent = new Intent(getApplicationContext(),CheckNote.class);
                intent.putExtra("id",id2);

                // 如果note加了密保锁
                if (note.getPassword().equals(""))
                {
                    startActivity(intent);
                    ShowNote.this.finish();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowNote.this);
                    View view1 = LayoutInflater.from(ShowNote.this).inflate(R.layout.my_dialog,null);
                    TextView cancel = view1.findViewById(R.id.choosepage_cancel);
                    TextView sure = view1.findViewById(R.id.choosepage_sure);
                    final EditText edittext = view1.findViewById(R.id.choosepage_edittext);
                    edittext.setTransformationMethod(PasswordTransformationMethod.getInstance());

                    final Dialog dialog =builder.create();
                    dialog.show();
                    dialog.getWindow().setContentView(view1);
                    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                    cancel.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            dialog.dismiss();
                        }
                    });

                    sure.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            password = edittext.getText().toString();

                            // 如果输入的密码正确
                            if (password.equals(note.getPassword()))
                            {
                                startActivity(intent);
                                ShowNote.this.finish();
                            }
                            else
                            {
                                Toast.makeText(ShowNote.this,"Wrong Password",Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ShowNote.this);
                dialog.setTitle("Delete");
                dialog.setMessage("Delete or not ?");
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mdb.toDelete(array.get(position).getId());
                        array=mdb.getArray();
                        MyAdapter adapter1 = new MyAdapter(ShowNote.this,R.layout.list_item,array);
                        lv.setAdapter(adapter1);
                    }
                });
                dialog.show();

                return true;
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(),NewNote.class);
                startActivity(intent);
                ShowNote.this.finish();
            }
        });
    }

}
