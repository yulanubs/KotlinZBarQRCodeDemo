package com.zbar.lib;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.administrator.kotlindemo.R;
import com.zbar.lib.camera.CameraManager;
import com.zbar.lib.decode.CaptureActivityHandler;
import com.zbar.lib.decode.InactivityTimer;

import java.io.IOException;

/**
 * 作者: 陈涛(1076559197@qq.com)
 *
 * 时间: 2014年5月9日 下午12:25:31
 *
 * 版本: V_1.0.0
 * <p>
 * 描述: 扫描界面
 */
public class CaptureActivity extends Activity implements Callback {
	public static final int REQUEST_CODE_SCAN = 0x0000;
	public static final String DECODED_CONTENT_KEY = "codedContent";
	public static final String DECODED_BITMAP_KEY = "codedBitmap";
	private static final int TAKE_PHOTO_REQUEST_CODE =0x2213 ;
	private CaptureActivityHandler handler;
	private boolean hasSurface;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.50f;
	private boolean vibrate;
	private int x = 0;
	private int y = 0;
	private int cropWidth = 0;
	private int cropHeight = 0;
	private RelativeLayout mContainer = null;
	private RelativeLayout mCropLayout = null;
	private boolean isNeedCapture = false;

	public boolean isNeedCapture() {
		return isNeedCapture;
	}

	public void setNeedCapture(boolean isNeedCapture) {
		this.isNeedCapture = isNeedCapture;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getCropWidth() {
		return cropWidth;
	}

	public void setCropWidth(int cropWidth) {
		this.cropWidth = cropWidth;
	}

	public int getCropHeight() {
		return cropHeight;
	}

	public void setCropHeight(int cropHeight) {
		this.cropHeight = cropHeight;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.qr_code_activity_qr_scan);


			CameraManager.Companion.init(getApplication());
			hasSurface = false;
			inactivityTimer = new InactivityTimer(this);

			mContainer = (RelativeLayout) findViewById(R.id.capture_containter);
			mCropLayout = (RelativeLayout) findViewById(R.id.capture_crop_layout);

			ImageView mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
			TranslateAnimation mAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE, 0f,
					TranslateAnimation.RELATIVE_TO_PARENT, 0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);
			mAnimation.setDuration(1500);
			mAnimation.setRepeatCount(-1);
			mAnimation.setRepeatMode(Animation.REVERSE);
			mAnimation.setInterpolator(new LinearInterpolator());
			mQrLineView.setAnimation(mAnimation);


	}

	boolean flag = true;

	protected void light() {
		if (flag == true) {
			flag = false;
			// 开闪光灯
			CameraManager.Companion.get().openLight();
		} else {
			flag = true;
			// 关闪光灯
			CameraManager.Companion.get().offLight();
		}

	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		doNext(requestCode, grantResults);
	}
	private void doNext(int requestCode, int[] grantResults) {
		if (requestCode == 1) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Permission Granted
				hasSurface = true;
				SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capture_preview);
				SurfaceHolder surfaceHolder = surfaceView.getHolder();
				if (hasSurface) {
					initCamera(surfaceHolder);
				} else {
					surfaceHolder.addCallback(this);
					surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				}
			} else {
				CameraManager.Companion.get().closeDriver();
				// Permission Denied
				//  displayFrameworkBugMessageAndExit();
				Toast.makeText(this, "请在应用管理中打开“相机”访问权限！", Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions( this,
					new String[]{Manifest.permission.CAMERA},
					TAKE_PHOTO_REQUEST_CODE);
			hasSurface=true;
		} else {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capture_preview);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			if (hasSurface) {
				initCamera(surfaceHolder);



			} else {
				surfaceHolder.addCallback(this);
				surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			}
			playBeep = true;
			AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
			if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
				playBeep = false;
			}
			initBeepSound();
			vibrate = true;
		}


	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.Companion.get().closeDriver();



	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	public void handleDecode(String result) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();

		// 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
		// handler.sendEmptyMessage(R.id.restart_preview);
		Intent intent=new Intent();
		intent.putExtra(DECODED_CONTENT_KEY,result);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.Companion.get().openDriver(surfaceHolder);

			Point point = 	CameraManager.Companion.get().getCameraResolution();
			int width = point.y;
			int height = point.x;

			int x = mCropLayout.getLeft() * width / mContainer.getWidth();
			int y = mCropLayout.getTop() * height / mContainer.getHeight();

			int cropWidth = mCropLayout.getWidth() * width / mContainer.getWidth();
			int cropHeight = mCropLayout.getHeight() * height / mContainer.getHeight();

			setX(x);
			setY(y);
			setCropWidth(cropWidth);
			setCropHeight(cropHeight);
			// 设置是否需要截图
			setNeedCapture(true);


		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(CaptureActivity.this);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {

			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.CAMERA)
					!= PackageManager.PERMISSION_GRANTED) {
//				hasSurface=false;
				ActivityCompat.requestPermissions((Activity) this,
						new String[]{Manifest.permission.CAMERA},
						TAKE_PHOTO_REQUEST_CODE);

			} else {
				hasSurface = true;
				initCamera(holder);
			}

		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public Handler getHandler() {
		return handler;
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.qr_code_beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
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

	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
}
