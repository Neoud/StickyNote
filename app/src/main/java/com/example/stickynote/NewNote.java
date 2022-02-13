package com.example.stickynote;
import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewNote extends AppCompatActivity
{
    Button btnBack2, btnSave, btnPhoto2, btnLock2;
    EditText title, content;
    MyDatabaseOperator myDatabaseOperator;
    Note note;
    String password = "";
    static final int IMAGE_CODE = 99;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);
        btnBack2=findViewById(R.id.title_back2);
        btnSave=findViewById(R.id.title_save);
        btnPhoto2=findViewById(R.id.title_photo2);
        btnLock2=findViewById(R.id.title_lock2);
        title = findViewById(R.id.addTitle);
        content = findViewById(R.id.addContext);
        myDatabaseOperator = new MyDatabaseOperator(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.hide();
        }

        btnBack2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goBack();
            }
        });

        btnPhoto2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                callGallery();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isSave();
            }
        });

        btnLock2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setLock();
            }
        });
    }

    public void goBack()
    {
        Intent intent = new Intent(NewNote.this,ShowNote.class);
        startActivity(intent);
        NewNote.this.finish();
    }


    public void isSave()
    {
        String tt = title.getText().toString();
        String ct = content.getText().toString();
        note = new Note(tt,ct,password);
        myDatabaseOperator.toInsert(note);
        Intent intent = new Intent(NewNote.this,ShowNote.class);
        startActivity(intent);
        NewNote.this.finish();
    }

    public void setLock()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewNote.this);
        View view = LayoutInflater.from(NewNote.this).inflate(R.layout.my_dialog,null);
        TextView cancel = view.findViewById(R.id.choosepage_cancel);
        TextView sure = view.findViewById(R.id.choosepage_sure);

        final EditText edittext = view.findViewById(R.id.choosepage_edittext);
        final Dialog dialog =builder.create();

        dialog.show();
        dialog.getWindow().setContentView(view);
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
                dialog.dismiss();
            }
        });
    }


    /**
     * region 调用图库
     */
    private void callGallery()
    {
        int permission_WRITE = ActivityCompat.checkSelfPermission(NewNote .this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_READ = ActivityCompat.checkSelfPermission(NewNote.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        // 如果得到系统的允许
        if (permission_WRITE != PackageManager.PERMISSION_GRANTED || permission_READ != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(NewNote.this,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        }

        Intent getAlbum = new Intent(Intent.ACTION_PICK);
        getAlbum.setType("image/*");
        startActivityForResult(getAlbum,IMAGE_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Bitmap bm = null;
        ContentResolver resolver = getContentResolver();

        if (requestCode == IMAGE_CODE)
        {
            try
            {
                // 获得图片的uri
                Uri originalUri = data.getData();
                bm = MediaStore.Images.Media.getBitmap(resolver,originalUri);
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(originalUri,proj,null,null,null);

                // 获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                cursor.moveToFirst();

                // 最后根据索引值获取图片路径
                String path = cursor.getString(column_index);
                insertImg(path);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(NewNote.this,"图片插入失败",Toast.LENGTH_SHORT).show();
            }
        } // end of if (...)
    }

    private void insertImg(String path)
    {
        Log.e("插入图片", "insertImg:" + path);
        String tagPath = "<img src=\""+path+"\"/>";//为图片路径加上<img>标签
        Bitmap bitmap = BitmapFactory.decodeFile(path);

        // 如果二进制图创建成功
        if (bitmap != null)
        {
            SpannableString ss = getBitmapMime(path, tagPath);
            insertPhotoToEditText(ss);
            content.append("\n");
            //Log.e("YYPT_Insert", content.getText().toString());

        }
        else
        {
            //Log.d("YYPT_Insert", "tagPath: "+tagPath);
            Toast.makeText(NewNote.this,"插入失败，无读写存储权限，请到权限中心开启",Toast.LENGTH_LONG).show();
        }
    }

    private void insertPhotoToEditText(SpannableString ss)
    {
        Editable et = content.getText();
        int start = content.getSelectionStart();
        et.insert(start,ss);
        content.setText(et);
        content.setSelection(start+ss.length());
        content.setFocusableInTouchMode(true);
        content.setFocusable(true);
    }

    /**
     * region 根据图片路径利用SpannableString和ImageSpan来加载图片
     * @param path
     * @param tagPath
     * @return
     */
    private SpannableString getBitmapMime(String path,String tagPath)
    {
        SpannableString ss = new SpannableString(tagPath);//这里使用加了<img>标签的图片路径
        int width = ScreenUtils.getScreenWidth(NewNote.this);
        int height = ScreenUtils.getScreenHeight(NewNote.this);
        Log.d("YYPT_IMG_SCREEN", "高度:"+height+",宽度:"+width);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        Log.d("YYPT_IMG_IMG", "高度:"+bitmap.getHeight()+",宽度:"+bitmap.getWidth());

        bitmap = ImageUtils.zoomImage(bitmap,(width-32)*0.8,bitmap.getHeight()/(bitmap.getWidth()/((width-32)*0.8)));
        Log.d("YYPT_IMG_COMPRESS", "高度："+bitmap.getHeight()+",宽度:"+bitmap.getWidth());

        ImageSpan imageSpan = new ImageSpan(this, bitmap);
        ss.setSpan(imageSpan, 0, tagPath.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    private void initContent()
    {
        String input = note.getContent().toString();
        Pattern p = Pattern.compile("\\<img src=\".*?\"\\/>");
        Matcher m = p.matcher(input);
        SpannableString spannable = new SpannableString(input);

        // 当m存在
        while (m.find())
        {
            String s = m.group();
            int start = m.start();
            int end = m.end();
            String path = s.replaceAll("\\<img src=\"|\"\\/>","").trim();
            int width = ScreenUtils.getScreenWidth(NewNote.this);
            int height = ScreenUtils.getScreenHeight(NewNote.this);

            try
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                bitmap = ImageUtils.zoomImage(bitmap,(width-32)*0.8,bitmap.getHeight()/(bitmap.getWidth()/((width-32)*0.8)));
                ImageSpan imageSpan = new ImageSpan(this, bitmap);
                spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        content.setText(spannable);
    }

}
