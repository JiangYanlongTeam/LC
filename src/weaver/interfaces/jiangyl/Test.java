package weaver.interfaces.jiangyl;

import java.util.Calendar;

import com.alibaba.fastjson.JSONObject;

import weaver.common.util.taglib.SplitPageXmlServlet;
import weaver.common.util.taglib.SplitPageXmlServletNew;

public class Test {

	public static void main(String[] args) {
		SplitPageXmlServlet s = new SplitPageXmlServlet();
	}

	public static String[] getDate(String selectDateString) {
		String[] date = new String[2];

		int countDays = 0; // 需要显示的天数
		int offsetDays = 0; // 相对显示显示第一天的偏移天数
		String thisDate = ""; // 当前日期
		String selectDate = ""; // 用于显示日期

		String beginDate = "";
		String endDate = "";

		String beginYear = "";
		String beginMonth = "";
		String beginDay = "";

		String endYear = "";
		String endMonth = "";
		String endDay = "";

		Calendar thisCalendar = Calendar.getInstance(); // 当前日期
		Calendar selectCalendar = Calendar.getInstance(); // 用于显示的日期

		String thisYear = add0((thisCalendar.get(Calendar.YEAR)), 4); // 当前年
		String thisMonth = add0((thisCalendar.get(Calendar.MONTH)) + 1, 2); // 当前月
		String thisDayOfMonth = add0((thisCalendar.get(Calendar.DAY_OF_MONTH)), 2); // 当前日
		thisDate = thisYear + "-" + thisMonth + "-" + thisDayOfMonth;

		if (!"".equals(selectDateString))
		// 当选择日期
		{
			int selectYear = getIntValue(selectDateString.substring(0, 4)); // 被选择年
			int selectMonth = getIntValue(selectDateString.substring(5, 7)) - 1; // 被选择月
			int selectDay = getIntValue(selectDateString.substring(8, 10)); // 被选择日
			selectCalendar.set(selectYear, selectMonth, selectDay);
		}

		String selectYear = add0((selectCalendar.get(Calendar.YEAR)), 4); // 年
		String selectMonth = add0((selectCalendar.get(Calendar.MONTH)) + 1, 2); // 月
		String selectDayOfMonth = add0((selectCalendar.get(Calendar.DAY_OF_MONTH)), 2); // 日
		String selectWeekOfYear = String.valueOf(selectCalendar.get(Calendar.WEEK_OF_YEAR)); // 第几周
		String selectDayOfWeek = String.valueOf(selectCalendar.get(Calendar.DAY_OF_WEEK)); // 一周第几天
		selectDate = selectYear + "-" + selectMonth + "-" + selectDayOfMonth;

		// 月计划显示
		selectCalendar.set(Calendar.DATE, 1); // 设置为月第一天
		int offsetDayOfWeek = selectCalendar.get(Calendar.DAY_OF_WEEK) - 1;
		offsetDays = Integer.parseInt(selectDayOfMonth) - 1 + offsetDayOfWeek;
		selectCalendar.add(Calendar.DAY_OF_WEEK, -1 * offsetDayOfWeek); // 设置为月首日那周的第一天

		beginYear = add0(selectCalendar.get(Calendar.YEAR), 4); // 年
		beginMonth = add0(selectCalendar.get(Calendar.MONTH) + 1, 2); // 月
		beginDay = add0(selectCalendar.get(Calendar.DAY_OF_MONTH), 2); // 日
		beginDate = beginYear + "-" + beginMonth + "-" + beginDay;

		// 月计划显示
		selectCalendar.add(Calendar.DATE, offsetDays);
		// System.out.println("######" + selectCalendar.get(Calendar.DATE));
		selectCalendar.set(Calendar.DATE, 1); // 设置为月第一天
		selectCalendar.add(Calendar.MONTH, 1);
		selectCalendar.add(Calendar.DATE, -1);
		countDays = selectCalendar.get(Calendar.DAY_OF_MONTH); // 当月天数
		int offsetDayOfWeekEnd = 7 - selectCalendar.get(Calendar.DAY_OF_WEEK);
		selectCalendar.add(Calendar.DAY_OF_WEEK, offsetDayOfWeekEnd); // 设置为月末日那周的最后一天

		endYear = add0(selectCalendar.get(Calendar.YEAR), 4); // 年
		endMonth = add0(selectCalendar.get(Calendar.MONTH) + 1, 2); // 月
		endDay = add0(selectCalendar.get(Calendar.DAY_OF_MONTH), 2); // 日
		endDate = endYear + "-" + endMonth + "-" + endDay;

		date[0] = beginDate;
		date[1] = endDate;

		return date;
	}

	public static String add0(int paramInt1, int paramInt2) {
		long l = (long) Math.pow(10.0D, paramInt2);
		return String.valueOf(l + paramInt1).substring(1);
	}

	public static int getIntValue(String paramString) {
		return getIntValue(paramString, -1);
	}

	public static int getIntValue(String paramString, int paramInt) {
		try {
			return Integer.parseInt(paramString);
		} catch (Exception localException) {
		}
		return paramInt;
	}
}
