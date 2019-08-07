package com.example.yfsl.getscreenimage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    private RxPermissions rxPermissions;
    private int second,minute,hour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rxPermissions = new RxPermissions(this);

        final TextView textView = findViewById(R.id.text);
        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取权限
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Observer<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Boolean aBoolean) {
                                if (aBoolean) {
//                                    getScreenImage();
                                    getAndSaveCurrentImage();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    second ++;
                    if (second == 60) {
                        second = 0;
                        minute++;
                    }
                    if (minute == 60) {
                        minute = 0;
                        hour ++;
                    }
                    runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            textView.setText(new DecimalFormat("00").format(hour) + ":" + new DecimalFormat("00").format(minute)
                                    + ":" + new DecimalFormat("00").format(second));
                        }
                    });
                }
            }
        }).start();


    }

    private void getScreenImage(){
        View dView = getWindow().getDecorView();
        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(dView.getDrawingCache());
        if (bitmap != null) {
            try {
                // 获取内置SD卡路径
                String sdCardPath = Environment.getExternalStorageDirectory().getPath();
                // 图片文件路径
                String filePath = sdCardPath + File.separator + "screenshot.png";
                File file = new File(filePath);
                FileOutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
                Log.d("a7888", "存储完成");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取和保存当前屏幕的截图
     */
    private File getAndSaveCurrentImage() {
        //1.构建Bitmap
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int w = display.getWidth();
        int h = display.getHeight();

        Bitmap Bmp = Bitmap.createBitmap( w, h, Bitmap.Config.ARGB_8888 );

        //2.获取屏幕
        View decorview = this.getWindow().getDecorView();
        decorview.buildDrawingCache(true);
        decorview.setDrawingCacheEnabled(true);
        Bmp = decorview.getDrawingCache();


        String SavePath = getSDCardPath()+"/AndyDemo/ScreenImage";
        File file = null;
        //3.保存Bitmap
        try {
            File path = new File(SavePath);
            //创建图片文件名  IMG_20190702025900.jpg
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.CHINA);
            String imageFileName = "IMG_"+dateFormat.format(new Date(System.currentTimeMillis()))+".jpg";
            file = new File(SavePath,imageFileName);
            if(!path.exists()){
                path.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            if (null != fos) {
                Bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();

                Toast.makeText(MainActivity.this, "截屏文件已保存至SDCard/AndyDemo/ScreenImage/下", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e("TAG",e.toString());
            e.printStackTrace();
        }
        //对截取的view操作完之后清空缓存 不然多次截屏后只会显示第一张
        decorview.destroyDrawingCache();
        return file;
    }

    /**
     * 获取SDCard的目录路径功能
     * @return
     */
    private String getSDCardPath(){
        File sdcardDir = null;
        //判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(sdcardExist){
            sdcardDir = Environment.getExternalStorageDirectory();
        }
        return sdcardDir.toString();
    }
}
