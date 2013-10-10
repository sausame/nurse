package com.ankh.nurse;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PersonalDailyInformation {

	private static final String TAG = "PersonalDailyInformation";

	public Date whichDay;
	public String name; // Disease name.
	public int level;

	public List<DetailInformation> detailList;

	public int compare(Date whichDay) {
		return this.whichDay.compareTo(whichDay);
	}

	public int compare(PersonalDailyInformation infor) {
		return compare(infor.whichDay);
	}

	public static class DetailInformation {
		public String description;
		public String attachmentPath;

		public static DetailInformation parseDetailInformation(JSONObject object) {
			try {
				DetailInformation detailInfo = new DetailInformation();

				detailInfo.description = object.getString("description");
				detailInfo.attachmentPath = object.getString("attachmentPath");

				return detailInfo;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public JSONObject toJSONObject() {
			try {
				JSONObject object = new JSONObject();
				object.put("description", description);
				object.put("attachmentPath", attachmentPath);
				return object;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static PersonalDailyInformation parsePersonalDailyInformation(
			JSONObject object) {
		PersonalDailyInformation info;
		try {
			info = new PersonalDailyInformation();

			info.setUTCDateTime(object.getString("whichDay"));
			info.name = object.getString("name");
			info.level = Integer.parseInt(object.getString("level"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		try {
			JSONArray jsonArray = object.getJSONArray("detailList");
			int num = jsonArray.length();
			if (num > 0) {
				info.detailList = new ArrayList<DetailInformation> ();

				for (int i = 0; i < num; i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					DetailInformation detailInfo = DetailInformation
							.parseDetailInformation(obj);
					if (detailInfo != null) {
						info.detailList.add(detailInfo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return info;
	}

	public String toString() {
		String str = "Date: " + whichDay + "\n";
		str += "Name: " + name + "\n";
		str += "Level: " + level + "\n";

		if (detailList != null) {
			for (int i = 0; i < detailList.size(); i++) {
				str += "NO." + i + ": " + detailList.get(i).description + ", "
						+ detailList.get(i).attachmentPath + "\n";
			}
			str += "\n";
		}

		return str;
	}

	private final String getUTCDateTime() {
		try {
			DateFormat df = DateFormat.getDateTimeInstance();
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			return df.format(whichDay);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void setUTCDateTime(String dt) {
		try {
			DateFormat df = DateFormat.getDateTimeInstance();
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			whichDay = df.parse(dt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JSONObject toJSONObject() {
		try {
			JSONObject object = new JSONObject();
			object.put("whichDay", getUTCDateTime());
			object.put("name", name);
			object.put("level", level);

			if (detailList != null) {
				JSONArray objectArray = new JSONArray();
				for (int i = 0; i < detailList.size(); i++) {
					objectArray.put(detailList.get(i).toJSONObject());
				}

				object.put("detailList", objectArray);
			}
			return object;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Date getDay(int diff) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, diff);

		return calendar.getTime();
	}

	public static PersonalDailyInformation createRandomPersonalDailyInformation() {
		Date date = new Date();
		Random random = new Random(date.getTime());

		PersonalDailyInformation infor = new PersonalDailyInformation();
		infor.whichDay = getDay(-1 * (Math.abs(random.nextInt()) % 10));

		final String NAME_GROUP[] = { "AAAA", "BBBB", "CCCC", "DDDD", "EEEE" };

		infor.name = NAME_GROUP[Math.abs(random.nextInt()) % 5];

		int num = Math.abs(random.nextInt()) % 4;

		if (num > 0) {
			infor.detailList = new ArrayList<DetailInformation> ();
			for (int i = 0; i < num; i++) {
				DetailInformation detailInfo = new DetailInformation();

				detailInfo.description = "NO." + i + ": description.";
				detailInfo.attachmentPath = "NO." + i + ": attachmentPath.";
				infor.detailList.add(detailInfo);
			}
		}

//		Log.i(TAG, infor.toString());
		return infor;
	}
}
