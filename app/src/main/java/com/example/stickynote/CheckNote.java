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

public class CheckNote extends AppCompatActivity
{
    EditText title, content;
    Button btnBack, btnDelete, btnShare, btnPhoto, btnSave, btnLock;
    MyDatabaseOperator myDatabaseOperator;
    Note note;
    int id;
    String password;
    static final int IMAGE_CODE = 99;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_note);

        title=findViewById(R.id.addTitle);
        content=findViewById(R.id.addContext);
        btnBack=findViewById(R.id.title_back);
        btnPhoto=findViewById(R.id.title_photo);
        btnDelete=findViewById(R.id.title_delete);
        btnShare=findViewById(R.id.title_share);
        btnSave=findViewById(R.id.title_save);
        btnLock=findViewById(R.id.title_lock);

        myDatabaseOperator=new MyDatabaseOperator(this);
        Intent intent = this.getIntent();
        id = intent.getIntExtra("id",0);
        note=myDatabaseOperator.getTiandCon(id);
        title.setText(note.getTitle());
        content.setText(note.getContent());
        password = note.getPassword();
        initContent();

        ActionBar actionBar = getSupportActionBar();

        //当系统自带的导航栏存在的时候
        if (actionBar != null)
        {
            actionBar.hide();
        }

        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goBack();
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

        btnDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                myDatabaseOperator.toDelete(id);
                goBack();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,
                        "Title: " + title.getText().toString()+ " "
                                + "Content: " + content.getText().toString());
                startActivity(intent);
            }
        });

        btnLock.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(CheckNote.this);
                View view = LayoutInflater.from(CheckNote.this).inflate(R.layout.my_dialog2,null);
                TextView cancel = view.findViewById(R.id.choosepage_cancel);
                TextView sure = view.findViewById(R.id.choosepage_sure);
                TextView unlock = view.findViewById(R.id.choosepage_unlock);
                final EditText edittext = view.findViewById(R.id.choosepage_edittext);
                edittext.setText(note.getPassword());

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

                unlock.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        password = "";
                        dialog.dismiss();
                    }
                });
            }
        });

        btnPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                callGallery();
            }
        });
    }

    public void goBack()
    {
        Intent intent = new Intent(CheckNote.this,ShowNote.class);
        startActivity(intent);
        CheckNote.this.finish();
    }

    public void isSave()
    {
        String tt = title.getText().toString();
        String ct = content.getText().toString();
        Note note2 = new Note(id,tt,ct,password);
        myDatabaseOperator.toUpdate(note2);
        Intent intent = new Intent(CheckNote.this,ShowNote.class);
        startActivity(intent);
        CheckNote.this.finish();
    }

    /**
     * 加载图片代码
     */
    private void callGallery()
    {
        int permission_WRITE = ActivityCompat.checkSelfPermission(CheckNote.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_READ = ActivityCompat.checkSelfPermission(CheckNote.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        //如果得到系统的允许
        if (permission_WRITE != PackageManager.PERMISSION_GRANTED || permission_READ != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(CheckNote.this,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
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

        //如果验证代码是IMAGE_CODE
        if (requestCode == IMAGE_CODE)
        {
            try
            {
                // 获得图片的uri
                Uri originalUri = data.getData();
                bm = MediaStore.Images.Media.getBitmap(resolver,originalUri);
                String[] proj = {MediaStore.Images.Media.DATA};

                // android多媒体数据库的封装接口
                Cursor cursor = managedQuery(originalUri,proj,null,null,null);

                // 获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                // 将光标移至开头
                cursor.moveToFirst();

                // 根据索引值获取图片路径
                String path = cursor.getString(column_index);
                insertImg(path);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(CheckNote.this,"图片插入失败",Toast.LENGTH_SHORT).show();
            }
        } // end of if (...)
    }

    /**
     *插入图片
     */
    private void insertImg(String path)
    {
        Log.e("插入图片", "insertImg:" + path);

        //为图片路径加上<img>标签
        String tagPath = "<img src=\""+path+"\"/>";
        Bitmap bitmap = BitmapFactory.decodeFile(path);

        //如果二进制图片创建成功
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
            Toast.makeText(CheckNote.this,"插入失败，无读写存储权限，请到权限中心开启",Toast.LENGTH_LONG).show();
        }
    }

    /**
     *region 将图片插入到EditText中
     */
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
     *region 根据图片路径利用SpannableString和ImageSpan来加载图片
     */
    private SpannableString getBitmapMime(String path,String tagPath)
    {
        SpannableString ss = new SpannableString(tagPath);//这里使用加了<img>标签的图片路径
        int width = ScreenUtils.getScreenWidth(CheckNote.this);
        int height = ScreenUtils.getScreenHeight(CheckNote.this);
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

        // 如果m存在
        while (m.find())
        {
            String s = m.group();
            int start = m.start();
            int end = m.end();
            String path = s.replaceAll("\\<img src=\"|\"\\/>","").trim();
            int width = ScreenUtils.getScreenWidth(CheckNote.this);
            int height = ScreenUtils.getScreenHeight(CheckNote.this);
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
