package com.ankh.nurse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HistoryActivity extends Activity implements OnItemClickListener {
	private static final String TAG = "HistoryActivity";

	private static final int ADD = 0;
	private static final int MODIFY = 1;
	private static final int DELETE = 2;

	private ArrayList<DailyStatusItem> mData = null;
	private PersonalDailyInformationAdapter mAdapter;
	private SwipeListView mDateStatusList;

	private PersonalDailyInformationManager mManager = new PersonalDailyInformationManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FadingActionBarHelper helper = new FadingActionBarHelper()
				.actionBarBackground(R.drawable.ab_background_light)
				.headerLayout(R.layout.header)
				.contentLayout(R.layout.activity_history).parallax(true);
		
		setContentView(helper.createView(this));
		helper.initActionBar(this);

		initData();

		mAdapter = new PersonalDailyInformationAdapter(this, mData);
		mDateStatusList = (SwipeListView) findViewById(android.R.id.list);
		mDateStatusList.setAdapter(mAdapter);
		mDateStatusList.setOnItemClickListener(this);
		mDateStatusList.setWindow(getWindow());
		mDateStatusList
				.setListViewCallBack(new SwipeListView.ListViewCallBack() {
					@Override
					public void showCannotSwipe() {
					}

					@Override
					public void onChildDismissed(View v, int position) {
						deleteListView(position);
					}

					@Override
					public boolean canDismissed(View v, int position) {
						return isCanBeDismissed(position);
					}

					@Override
					public void undoDismiss() {
						setData();
					}

					@Override
					public void dismiss(int position) {
						deleteRaw(position);
					}
				});

		setData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			onActionAdd();
			return true;
		case R.id.action_settings:
			onActionSettings();
			return true;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "" + requestCode + ", " + resultCode + ", " + data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case ADD:
				onActionAddResult((PersonalDailyInformation) data
						.getSerializableExtra(PersonalDailyInformationActivity.MESSAGE));
				break;
			case MODIFY:
				onActionModifyResult(
						data.getIntExtra(PersonalDailyInformationActivity.ID, 0),
						(PersonalDailyInformation) data
								.getSerializableExtra(PersonalDailyInformationActivity.MESSAGE));
				break;
			case DELETE:
				break;
			default:
				break;
			}
		}
	}

	private void onActionAdd() {
		onActionAdd(-1);
	}

	private void onActionAdd(final int position) {
		Intent intent = new Intent(this, PersonalDailyInformationActivity.class);

		if (position >= 0) {
			Calendar c1 = new GregorianCalendar();
			Calendar c2 = new GregorianCalendar();

			c1.setTime(mData.get(position).date);

			c2.set(Calendar.YEAR, c1.get(Calendar.YEAR));
			c2.set(Calendar.MONTH, c1.get(Calendar.MONTH));
			c2.set(Calendar.DAY_OF_MONTH, c1.get(Calendar.DAY_OF_MONTH));

			PersonalDailyInformation infor = new PersonalDailyInformation();
			infor.whichDay = c2.getTime();

			Bundle bundle = new Bundle();
			bundle.putSerializable(PersonalDailyInformationActivity.MESSAGE,
					infor);
			intent.putExtras(bundle);
		}

		startActivityForResult(intent, ADD);
	}

	private void onActionModify(final int id, final int position,
			final int offset) {
		PersonalDailyInformation infor = mData.get(position).get(offset).infor;

		Intent intent = new Intent(this, PersonalDailyInformationActivity.class);

		Bundle bundle = new Bundle();
		bundle.putSerializable(PersonalDailyInformationActivity.MESSAGE, infor);
		bundle.putSerializable(PersonalDailyInformationActivity.ID, id);
		intent.putExtras(bundle);

		startActivityForResult(intent, MODIFY);
	}

	private void onActionAddResult(PersonalDailyInformation infor) {
		mManager.add(infor);
		mManager.save();

		setData();
	}

	private void onActionModifyResult(int id, PersonalDailyInformation infor) {
		if (infor != null) {
			mManager.modify(id, infor);
		} else {
			mManager.del(id);
		}

		mManager.save();

		setData();
	}

	private void onActionSettings() {
	}

	private DailyStatusItem mDeletedStatusItem = null;

	private void deleteListView(int position) {
		position -= mDateStatusList.getHeaderViewsCount();

		mDeletedStatusItem = mData.get(position);
		mData.remove(position);

		mAdapter.notifyDataSetChanged();
	}

	private void deleteRaw(int position) {
		DailyStatusItem item = mDeletedStatusItem;

		int num = item.size();
		if (num > 0) {
			for (int i = 0; i < num; i++) {
				int id = item.get(i).id;
				mManager.del(id);
			}

			mManager.save();
			setData();
		}
	}

	private boolean isCanBeDismissed(int position) {
		return mData.get(position).size() > 0;
	}

	private String getDay(Date date) {
		return SimpleDateFormat.getDateInstance().format(date);
	}

	private static Date getDay(Date date, int diff) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, diff);

		return calendar.getTime();
	}

	public void initData() {
		mManager.setPathname(getString(R.string.infor_filename));
		mManager.load();

		mData = new ArrayList<DailyStatusItem>();
	}

	public void setData() {
		Log.i(TAG, mManager.toString());

		mData.clear();
		mManager.reset();

		DailyStatusItem dialyItem = null;
		PersonalDailyInformation infor;

		for (int id = 0; null != (infor = mManager
				.getPersonalDailyInformation()); id++) {

			if (dialyItem == null || !infor.isSameDay(dialyItem.date)) {
				if (dialyItem != null) {
					Date tomorrow = getDay(dialyItem.date, -1);
					if (!infor.isSameDay(tomorrow)) {
						mData.add(new DailyStatusItem(tomorrow));
					}
				}

				dialyItem = new DailyStatusItem(infor.whichDay);
				mData.add(dialyItem);
			}

			dialyItem.add(new StatusItem(id, infor));
		}

		mAdapter.notifyDataSetChanged();
	}

	public class PersonalDailyInformationAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<DailyStatusItem> mData = null;

		public PersonalDailyInformationAdapter(Context context,
				ArrayList<DailyStatusItem> data) {
			mContext = context;
			mData = data;
		}

		// ====================================================================
		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		private final static int MAX_IN_AN_ITEM = 3;

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (position < 0 || position >= getCount()) {
				return null;
			}

			DateStatusViewGroup viewGroup = null;
			if (view == null) {
				LayoutInflater factory = LayoutInflater.from(mContext);
				view = factory.inflate(R.layout.listitem_daily_status, null);

				viewGroup = new DateStatusViewGroup();

				viewGroup.mStatusLayout = (LinearLayout) view
						.findViewById(R.id.layout);

				viewGroup.mDate = (TextView) view.findViewById(R.id.date);

				for (int i = 0; i < MAX_IN_AN_ITEM; i++) {
					OneStatusViewGroup object = new OneStatusViewGroup();
					View viewOneStatus = factory.inflate(R.layout.item_status,
							viewGroup.mStatusLayout, false);

					object.mLayout = (RelativeLayout) viewOneStatus
							.findViewById(R.id.one_status_layout);

					object.mName = (TextView) viewOneStatus
							.findViewById(R.id.name);
					object.mLevel = (View) viewOneStatus
							.findViewById(R.id.level);

					viewGroup.mStatusViewGroupList.add(object);

					viewGroup.mStatusLayout.addView(viewOneStatus, i);
				}

				view.setTag(viewGroup);
			} else {
				viewGroup = (DateStatusViewGroup) view.getTag();
			}

			showItemInfos(position, viewGroup);

			return view;
		}

		private void showItemInfos(final int position,
				DateStatusViewGroup viewGroup) {

			DailyStatusItem item = mData.get(position);

			int num = item.size();
			if (num >= MAX_IN_AN_ITEM) {
				num = MAX_IN_AN_ITEM;
			}

			viewGroup.mDate.setText(getDay(item.date));

			PersonalDailyInformation infor;
			OneStatusViewGroup object;
			int i;
			for (i = 0; i < num; i++) {
				final int offset = i;
				final int id = item.get(offset).id;

				object = viewGroup.mStatusViewGroupList.get(offset);

				infor = item.get(offset).infor;

				object.mName.setText(infor.name);
				object.mName.setVisibility(View.VISIBLE);

				object.mLevel.setVisibility(View.VISIBLE);
				object.mLevel
						.setBackgroundResource(getBackgroundResource(infor.level));

				// XXX Android error. If it's an Image or Text, it needs to be
				// set twice.
				object.mLevel.getLayoutParams().width = getButtonLength(infor.level);
				// object.mLevel.setWidth(getButtonLength(infor.level));

				object.mLevel.requestLayout();
				object.mLevel.invalidate();

				object.mLayout.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						onClickLayout(id, position, offset);
					}
				});
			}

			for (; i < MAX_IN_AN_ITEM; i++) {
				object = viewGroup.mStatusViewGroupList.get(i);

				object.mName.setVisibility(View.INVISIBLE);
				object.mLevel.setVisibility(View.INVISIBLE);
			}
		}

		private int getBackgroundResource(int level) {
			return mResIDGroupOfBackground[level % 5];
		}

		private int getButtonLength(int level) {
			return (int) (getResources().getDimension(
					R.dimen.status_level_step_width) * (level + 1));
		}

		private void onClickLayout(final int id, final int position,
				final int offset) {
			onActionModify(id, position, offset);
		}

		// ====================================================================
		private class OneStatusViewGroup {
			public RelativeLayout mLayout;
			public TextView mName;
			public View mLevel;
		}

		private class DateStatusViewGroup {
			public LinearLayout mStatusLayout;
			public TextView mDate;
			public ArrayList<OneStatusViewGroup> mStatusViewGroupList = new ArrayList<OneStatusViewGroup>();
		}

		private final int mResIDGroupOfBackground[] = {
				R.drawable.list_selector_holo_green,
				R.drawable.list_selector_holo_blue,
				R.drawable.list_selector_holo_yellow,
				R.drawable.list_selector_holo_orange,
				R.drawable.list_selector_holo_red };
	}

	private class StatusItem {
		public StatusItem(int id, PersonalDailyInformation infor) {
			this.id = id;
			this.infor = infor;
		}

		private int id;
		private PersonalDailyInformation infor;
	}

	private class DailyStatusItem {
		public DailyStatusItem(Date date) {
			this.date = date;
		}

		public void add(StatusItem item) {
			if (list == null) {
				list = new ArrayList<StatusItem>();
			}

			list.add(item);
		}

		public StatusItem get(int position) {
			if (list == null) {
				return null;
			}
			return list.get(position);
		}

		public int size() {
			if (list == null) {
				return 0;
			}
			return list.size();
		}

		Date date;
		ArrayList<StatusItem> list;
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position,
			long id) {
	}

}
