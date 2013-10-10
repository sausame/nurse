package com.ankh.nurse;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class PersonalDailyInformationDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_daily_information_detail);

		Gallery gallery = (Gallery) findViewById(R.id.gallery);
		gallery.setAdapter(new ImageAdapter(this));
		gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// 这里不做响应
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.personal_daily_information_detail,
				menu);
		return true;
	}

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context context) {
			mContext = context;
		}

		public int getCount() {
			return mps.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		private Integer[] mps = { R.drawable.ic_launcher,
				R.drawable.ic_launcher };

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ImageView image = new ImageView(mContext);
			image.setImageResource(mps[arg0]);
			image.setAdjustViewBounds(true);
			image.setLayoutParams(new Gallery.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			return image;
		}
	}

}
