package com.openxu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.openxu.zxing.R;
import com.zxing.ErCodeScanActivity;
import com.zxing.encoding.EncodingHandler;

public class MainActivity extends AppCompatActivity {

    private EditText et_str;
    private TextView tv_result;
    private ImageView qr_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_str = (EditText)findViewById(R.id.et_str);
        tv_result = (TextView)findViewById(R.id.tv_result);
        qr_img = (ImageView)findViewById(R.id.qr_img);


    }

    /**
     * 生成二维码
     * @param v
     */
    public void shengcheng(View v){
        String text = et_str.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            try {
                Bitmap qrCodeBitmap = EncodingHandler.createQRCode(text,450);
                if (qrCodeBitmap != null) {
                    // 放开下面的代码，则可以加图片水印
                    // Canvas canvas = new Canvas(qrCodeBitmap);
                    // Bitmap btmap =
                    // BitmapFactory.decodeResource(getResources(),
                    // R.drawable.xfapp_icon);
                    // btmap = Bitmap.createScaledBitmap(btmap, 110, 110, true);
                    // canvas.drawBitmap(btmap, 260, 260, null);
                    qr_img.setImageBitmap(qrCodeBitmap);
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this,"请输入",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 扫描二维码
     * @param v
     */
    public void saomiao(View v){
        startActivityForResult(new Intent(this, ErCodeScanActivity.class), 1);
    }


    @Override
    protected void onActivityResult(int arg0, int arg1, Intent data) {
        switch (arg0) {
            case 1:    //扫描二维码
                if (data != null) {
                    String code = data.getStringExtra("result");
                    tv_result.setText(code+"");
                }
                return;
        }
    }


}
