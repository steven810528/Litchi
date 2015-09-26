package net.yostore.aws.view.common;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.ecareme.asuswebstorage.ASUSWebStorage;
import com.ncku.mis.litchi.R;

import net.yostore.aws.api.ApiConfig;

public class VideoPlayerActivity extends Activity {
	private final String TAG = "VideoPlayerActivity";
	private ApiConfig apiCfg = null;
	private VideoView videoView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_player);
		apiCfg = ASUSWebStorage.apiCfg;
		Bundle bData = this.getIntent().getExtras();

		String url = bData.getString("url");
		videoView = (VideoView) this.findViewById(R.id.splashvideo);
		MediaController mc = new MediaController(this);
		videoView.setMediaController(mc);

		PlayVideo(url);
	}

	private void PlayVideo(String url) {
		Log.d(TAG, "url:" + url);
		videoView.setVideoURI(Uri.parse(url));
		videoView.requestFocus();
		videoView.start();
	}

}