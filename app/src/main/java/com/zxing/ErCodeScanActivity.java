package com.zxing;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.openxu.utils.PermissionUtils;
import com.openxu.zxing.R;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

/**
 * author : openXu
 * create at : 2016/7/18 15:13
 * project : midzs119
 * class name : ErCodeScanActivity
 * version : 1.0
 * class describe：二维码扫描界面
 *
 *
 */
public class ErCodeScanActivity extends CaptureBaseActivity implements Callback {

    private String TAG  = "ErCodeScanActivity";
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    private boolean isFromBind;

    private String qrCode;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ercode_scan);
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);

        isFromBind = getIntent().getBooleanExtra("isBind", false);

        if (!isFromBind) {
            qrCode = getIntent().getStringExtra("qrCode");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkCameraPermission();
    }

    /**
     * 6.0权限
     */
    private void checkCameraPermission(){
        Log.v(TAG, "申请摄像头权限");
        String permission = Manifest.permission.CAMERA;
        String permissionName = "摄像头";
        String use = "拍摄视频";
        if(PermissionUtils.checkPermission(ErCodeScanActivity.this,
                permission, PermissionUtils.PERMISSION_CAMERA_CODE, permissionName, use)){
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.PERMISSION_CAMERA_CODE:
                if (PermissionUtils.verifyPermissions(grantResults, permissions, null)) {
                    Log.d(TAG, "摄像头权限被允许");
                } else {
                    // Permission Denied
                    Log.e(TAG, "摄像头权限被拒绝了");
                    // Permission Denied
                    showPermissionDialog(true);
                }
                break;
        }
    }

    /**
     * 提示相机权限被拒绝
     * @param goSet 是否提示去设置  6.0以上系统可以在设置中开启权限，而6.0以下系统某些定制系统应用设置详情页没有权限设置功能
     */
    private void showPermissionDialog(boolean goSet){
        AlertDialog.Builder builder = new AlertDialog.Builder(ErCodeScanActivity.this);
        if(goSet){
            builder.setTitle("权限申请")
                    .setMessage("无相机使用权限，若希望继续此功能请到设置-应用-"+getResources().getString(R.string.app_name)+
                            "-权限中开启相机权限")
                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            PermissionUtils.showInstalledAppDetails(ErCodeScanActivity.this, MidCompanyApp.mAppPkg);
                        }
                    })
                    .setNegativeButton("取消",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
        }else{
            builder.setTitle("提示")
                    .setMessage("无相机使用权限，若希望继续此功能请到设置中开启相机权限。")
                    .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        //FIXME
        if (resultString.equals("")) {
            Toast.makeText(ErCodeScanActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
        } else {
//			ToastAlone.show(getApplicationContext(), resultString);
//			System.out.println("Result:"+resultString);
//			submitResult(resultString);
//			if(isFromBind) {
//				//绑定或者修改绑定
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("result", resultString);
            resultIntent.putExtras(bundle);
            this.setResult(RESULT_OK, resultIntent);
            ErCodeScanActivity.this.finish();
//			}else {
//				if(!TextUtils.isEmpty(qrCode) && qrCode.equals(resultString)) {
//					submitResult(resultString);
//				} else {
//					ToastAlone.show(getApplicationContext(), "二维码信息不匹配，请重新扫描");
//					finish();
//				}
//			}
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        Log.v(TAG, "初始化相机");

       try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (Exception e) {
            Log.v(TAG, "当前系统版本："+android.os.Build.VERSION.SDK_INT);
            //此处应加上判断，避免6.0以上系统也弹出此对话框
            if(android.os.Build.VERSION.SDK_INT<android.os.Build.VERSION_CODES.M){
                //对于某些定制系统，在6.0之前就加入了运行时权限，只能依靠抛异常来检测权限是否被拒绝
//                e.printStackTrace();
                showPermissionDialog(false);
                return;
            }
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

}