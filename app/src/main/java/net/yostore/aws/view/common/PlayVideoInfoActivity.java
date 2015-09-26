package net.yostore.aws.view.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecareme.asuswebstorage.ASUSWebStorage;
import com.ncku.mis.litchi.R;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.api.entity.ApiCookies;
import net.yostore.aws.api.entity.VideoInfo;

import java.util.ArrayList;
import java.util.List;

public class PlayVideoInfoActivity extends Activity {
	private final String TAG = "PlayVideoInfoActivity";
	private ApiConfig apiCfg = null;
	private List<VideoInfo> video;
	private String fileID;

	private boolean esange = false;
	LinearLayout ll;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playvideo_info);
		apiCfg = ASUSWebStorage.apiCfg;
		Bundle bData = this.getIntent().getExtras();
		ArrayList list = bData.getParcelableArrayList("video");
		String Statu = bData.getString("Status");
		fileID = bData.getString("fileID");
		//putSatus(Statu);
		video = (List<VideoInfo>) list.get(0);

		ll = (LinearLayout) findViewById(R.id.video_type);
		if (esange) {
			putVideoType();
		}

	}

	private void putSatus(String status) {
		TextView progess_txt = (TextView) findViewById(R.id.video_por_txt);
		if ("0".equals(status)) {
			progess_txt.setText("轉檔進度:影片轉換完成");
			esange = true;
		} else {
			progess_txt.setText("轉檔進度:影片轉換中");
		}
	}

	private void putVideoType() {
		for (int i = 0; i < video.size(); i++) {
			final String type = video.get(i).getType();
			String abstractstate = video.get(i).getAbstractstate();
			String progressstate = video.get(i).getProgressstate();
			String resolution = video.get(i).getResolution();

			TextView tv_type = new TextView(this);
			tv_type.setText("影片轉換格式:" + type);
			ll.addView(tv_type);

			TextView tv_abstractstate = new TextView(this);
			tv_abstractstate.setText("轉檔抽象進度" + abstractstate);
			ll.addView(tv_abstractstate);

			TextView tv_progressstate = new TextView(this);
			tv_progressstate.setText("轉檔進度常數:" + progressstate);
			ll.addView(tv_progressstate);

			TextView tv_resolution = new TextView(this);
			tv_resolution.setText(resolution);
			ll.addView(tv_resolution);

			Button b1 = new Button(this);
			// b1.setWidth(pixels);
			b1.setText("播放" + type + "格式影片");

			if ("0".equals(abstractstate) && "0".equals(progressstate)) {
				b1.setEnabled(true);
			} else {
				b1.setEnabled(false);
			}

			b1.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					String url = "https://" + apiCfg.webRelay
							+ "/webrelay/directdownload/" + apiCfg.token
							+ "/?dis=" + ApiCookies.sid + "&fi=" + fileID
							+ "&cpt=" + type;

					intent.setClass(PlayVideoInfoActivity.this,
							VideoPlayerActivity.class);

					Bundle bundle = new Bundle();
					bundle.putString("url", url);
					intent.putExtras(bundle);

					startActivity(intent);

				}
			});

			ll.addView(b1);
		}
	}
}