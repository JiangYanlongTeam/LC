/**
 * 
 */
package weaver.interfaces.jiangyl.rest;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

/**
 * @author jiangyanlong
 *
 */
public class CalFlowHours extends BaseCronJob {

	public void execute() {
		BaseBean bean = new BaseBean();
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		String modelid = Util.null2String(bean.getPropValue("flowreport", "modelid"));
		String timefrom = Util.null2String(bean.getPropValue("flowreport", "timefrom"));
		String timeto = Util.null2String(bean.getPropValue("flowreport", "timeto"));
		String hrmno = Util.null2String(bean.getPropValue("flowreport", "hrmno"));
		String delayhour = Util.null2String(bean.getPropValue("flowreport", "delayhour"));
		String sh_delayhour = Util.null2String(bean.getPropValue("flowreport", "sh_delayhour"));
		String special_delayhour = Util.null2String(bean.getPropValue("flowreport", "special_delayhour"));
		String holiday_modelid = Util.null2String(bean.getPropValue("flowreport", "holiday_modelid"));
		List<String> holidayList = new ArrayList<String>();
		
		if (!"".equals(holiday_modelid)) {
			String sql1 = "select w.tablename from modeinfo m,workflow_bill w where w.id=m.formid and m.id = '"
					+ holiday_modelid + "'";
			bean.writeLog("根据MODELID获取假期表单建模表：" + sql1);
			rs.execute(sql1);
			rs.next();
			String tableName = Util.null2String(rs.getString("tablename"));
			String sql = "select rq from " + tableName + "_dt1";
			rs.execute(sql);
			while(rs.next()) {
				holidayList.add(Util.null2String(rs.getString("rq")));
			}
		}

		String sql1 = "select w.tablename from modeinfo m,workflow_bill w where w.id=m.formid and m.id = '" + modelid
				+ "'";
		bean.writeLog("根据MODELID获取表单建模表：" + sql1);
		rs.execute(sql1);
		rs.next();
		String tableName = Util.null2String(rs.getString("tablename"));
		
		//czzh=zhanghongyun 的记录，bzlx一定为‘审批’（先刷一遍这个，确保所有zhanghongyun的记录为审批）
		String updateldsql = "update "+tableName+" set bzlx = '审批' where czzh='zhanghongyun' ";
		rs.execute(updateldsql);
		
		String sql = "select xtbs,lcid,lcbt,czzh,ddrq,ddsj,clrq,clsj,bzlx from " + tableName + " where clsc is null ";
		bean.writeLog("查询表单建模表中的数据：" + sql);
		rs.execute(sql);
		while (rs.next()) {
			String czzh = Util.null2String(rs.getString("czzh"));
			String xtbs = Util.null2String(rs.getString("xtbs"));
			String lcid = Util.null2String(rs.getString("lcid"));
			String lcbt = Util.null2String(rs.getString("lcbt"));
			String ddrq = Util.null2String(rs.getString("ddrq"));
			String ddsj = Util.null2String(rs.getString("ddsj"));
			String clrq = Util.null2String(rs.getString("clrq"));
			String clsj = Util.null2String(rs.getString("clsj"));
			String bzlx = Util.null2String(rs.getString("bzlx"));
			String wybs = xtbs + lcid + czzh + ddrq + ddsj;
			if ("".equals(ddrq) || "".equals(ddsj) || "".equals(clrq) || "".equals(clsj)) {
				bean.writeLog("系统标识[" + xtbs + "],流程id[" + lcid + "],流程标题[" + lcbt + "]字段值不全，不进行计算，到达日期[" + ddrq
						+ "],到达时间[" + ddsj + "],处理日期[" + clrq + "],处理时间[" + clsj + "]");
				continue;
			}
			String org_ddrq = ddrq;
			double hours = 0;
			int i = 0;
			// 判断ddrq在clrq之前 14:54 10:50
			while (getDateWithStr(ddrq, "yyyy-MM-dd").before(getDateWithStr(clrq, "yyyy-MM-dd"))) {
				// 判断是否是节假日，如果节假日，跳过，标识+1
				if (holidayList.contains(ddrq)) {
					ddrq = getIncomeDate3(getDateWithStr(ddrq, "yyyy-MM-dd"), 1);
					i++;
				} else {// 如果不是节假日，判断标识是否大于1，如果大于1说明前面已经走过节假日，compare第一个参数为标准开始时间
					if (i > 0) {
						double hour = compareKSSJ(timefrom, timefrom, timeto);
						hours += hour;
						ddrq = getIncomeDate3(getDateWithStr(ddrq, "yyyy-MM-dd"), 1);
						i++;
					} else {// 如果不是节假日，判断标识是否等于1，如果等于1说明前面没有走过节假日，compare第一个参数为ddsj
						double hour = compareKSSJ(ddsj, timefrom, timeto);
						hours += hour;
						ddrq = getIncomeDate3(getDateWithStr(ddrq, "yyyy-MM-dd"), 1);
						i++;
					}
				}
			}
			// 判断ddrq等于clrq
			// 判断不是节假日
			if (!holidayList.contains(ddrq)) {
				double hour = compareJSSJ(clsj, timefrom, timeto, org_ddrq, clrq, ddsj);
				hours += hour;
			}
			bean.writeLog("系统标识[" + xtbs + "],流程id[" + lcid + "],流程标题[" + lcbt + "]计算出来小时数为：" + hours);
			String sfcs = "";
			if(czzh.equals(hrmno)) {
				int flag = Double.compare(Double.valueOf(special_delayhour), hours);
				if (flag == -1) {
					sfcs = "是";
				}
				if (flag >= 0) {
					sfcs = "否";
				}
			} else {
				if ("审批".equals(bzlx)) {
					int flag = Double.compare(Double.valueOf(delayhour), hours);
					if (flag == -1) {
						sfcs = "是";
					}
					if (flag >= 0) {
						sfcs = "否";
					}
				} else {
					int flag = Double.compare(Double.valueOf(sh_delayhour), hours);
					if (flag == -1) {
						sfcs = "是";
					}
					if (flag >= 0) {
						sfcs = "否";
					}
				}
			}
			
			String updateSQL = "update " + tableName + " set sfcs = '" + sfcs + "', clsc = '" + hours
					+ "' where wybs = '" + wybs + "'";
			bean.writeLog("更新SQL：" + updateSQL);
			rs1.execute(updateSQL);
		}
	}

	/**
	 * 根据结束时间计算已哪个时间为开始标准时间，并计算 1. 比较time是否在09:00之前，直接返回0 2.
	 * 比较time是否是在21:00之后，按照标准开始时间和标准结束时间 3. 比较time是否在09:00-21:00之间，按照时间时间-21:00计算 4.
	 * 计算比较之后的time，和标准时间对比，计算时间
	 * 
	 * @param time
	 * @param standard
	 * @return
	 */
	public static double compareJSSJ(String time, String timefrom, String timeto, String org_clrq, String clrq, String ddsj) {
		DecimalFormat df = new DecimalFormat("######0.00");
		if(org_clrq.equals(clrq)) {
			// 如果时间在09:00之前，直接返回0
			if (getDateWithStr(time, "HH:mm").before(getDateWithStr(timefrom, "HH:mm"))) {
				return 0;
			}
			// 如果处理时间在09:00之后，21:00之前，开始时间在09:00之后
			if (getDateWithStr(time, "HH:mm").after(getDateWithStr(timefrom, "HH:mm")) && getDateWithStr(time, "HH:mm").before(getDateWithStr(timeto, "HH:mm"))) {
				double db = (double) timediff1(time, ddsj);
				return Double.valueOf(df.format(db / 60));
			}
			// 如果处理时间在09:00之前，21:00之前，开始时间在09:00之前（包含09:00）
			if ((getDateWithStr(ddsj, "HH:mm").before(getDateWithStr(timefrom, "HH:mm")) || getDateWithStr(ddsj, "HH:mm").equals(getDateWithStr(timefrom, "HH:mm"))) 
					&& getDateWithStr(time, "HH:mm").before(getDateWithStr(timeto, "HH:mm"))) {
				double db = (double) timediff1(time, timefrom);
				return Double.valueOf(df.format(db / 60));
			}
			// 如果处理时间在09:00之后，21:00之前，开始时间在09:00之前（包含09:00）
			if (getDateWithStr(ddsj, "HH:mm").before(getDateWithStr(timefrom, "HH:mm")) 
					&& (getDateWithStr(time, "HH:mm").after(getDateWithStr(timeto, "HH:mm")) 
							|| getDateWithStr(time, "HH:mm").equals(getDateWithStr(timeto, "HH:mm")))) {
				double db = (double) timediff1(timeto, ddsj);
				return Double.valueOf(df.format(db / 60));
			}
		} else {
			// 如果时间在09:00之前，直接返回0
			if (getDateWithStr(time, "HH:mm").before(getDateWithStr(timefrom, "HH:mm"))) {
				return 0;
			}
			// 如果时间在21:00之后，计算以标准开始时间为准，到标准结束时间结束
			if (getDateWithStr(time, "HH:mm").after(getDateWithStr(timeto, "HH:mm"))) {
				double db = (double) timediff1(timeto, timefrom);
				return Double.valueOf(df.format(db / 60));
			}
			// 如果时间在09:00之后，计算以实际时间为准，到标准结束时间结束
			if ((getDateWithStr(time, "HH:mm").after(getDateWithStr(timefrom, "HH:mm")))
					|| (getDateWithStr(time, "HH:mm").equals(getDateWithStr(timefrom, "HH:mm")))) {
				double db = (double) timediff1(timeto, time);
				return Double.valueOf(df.format(db / 60));
			}
		}
		return 0;
	}
	
	public static void main(String[] args) {
		System.out.println("zhanhh" == "zhanhh");
		String ddrq = "2017-08-30";
		String ddsj = "14:54";
		String clrq = "2017-09-06";
		String clsj = "10:50";
		String org_ddrq = ddrq;
		String timefrom = "09:00";
		String timeto = "21:00";
		double hours = 0;
		String delayhour = "12";
		int i = 0;
		List<String> holidayList = new ArrayList<String>();
		holidayList.add("2017-09-01");
		// 判断ddrq在clrq之前
		while (getDateWithStr(ddrq, "yyyy-MM-dd").before(getDateWithStr(clrq, "yyyy-MM-dd"))) {
			// 判断是否是节假日，如果节假日，跳过，标识+1
			if (holidayList.contains(ddrq)) {
				ddrq = getIncomeDate3(getDateWithStr(ddrq, "yyyy-MM-dd"), 1);
				i++;
			} else {// 如果不是节假日，判断标识是否大于1，如果大于1说明前面已经走过节假日，compare第一个参数为标准开始时间
				if (i > 0) {
					double hour = compareKSSJ(timefrom, timefrom, timeto);
					hours += hour;
					ddrq = getIncomeDate3(getDateWithStr(ddrq, "yyyy-MM-dd"), 1);
					i++;
				} else {// 如果不是节假日，判断标识是否等于1，如果等于1说明前面没有走过节假日，compare第一个参数为ddsj
					double hour = compareKSSJ(ddsj, timefrom, timeto);
					hours += hour;
					ddrq = getIncomeDate3(getDateWithStr(ddrq, "yyyy-MM-dd"), 1);
					i++;
				}
			}
		}
		// 判断ddrq等于clrq
		// 判断不是节假日
		if (!holidayList.contains(ddrq)) {
			double hour = compareJSSJ(clsj, timefrom, timeto, org_ddrq, clrq, ddsj);
			hours += hour;
		}
		String sfcs = "";
		int flag = Double.compare(Double.valueOf(delayhour), hours);
		if (flag == -1) {
			sfcs = "是";
		}
		if (flag >= 0) {
			sfcs = "否";
		}
		System.out.println(hours);
		System.out.println(sfcs);
	}
	
	public static int timediff1(String paramString1, String paramString2) {
		if ((paramString1.length() != 5) || (paramString2.length() != 5)) {
			return 0;
		}
		Calendar localCalendar = Calendar.getInstance();
		int i = getIntValue(paramString1.substring(0, 2), 0);
		int j = getIntValue(paramString1.substring(3, 5), 0);
		int k = getIntValue(paramString2.substring(0, 2), 0);
		int l = getIntValue(paramString2.substring(3, 5), 0);

		localCalendar.set(localCalendar.get(1), localCalendar.get(2), localCalendar.get(5), i, j);

		Date localDate1 = localCalendar.getTime();
		long l1 = localDate1.getTime();

		localCalendar.set(localCalendar.get(1), localCalendar.get(2), localCalendar.get(5), k, l);

		Date localDate2 = localCalendar.getTime();
		long l2 = localDate2.getTime();

		int i1 = new Long((l1 - l2) / 60000L).intValue();

		return i1;
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
	
	/**
	 * 根据开始时间计算已哪个时间为开始标准时间，并计算 1. 比较time是否在09:00之前／等于09:00，如果之前按照09:00计算 2.
	 * 比较time是否是在21:00之后，如果在之后直接返回0 3. 比较time是否在09:00-21:00之间，如果在按照时间时间-21:00计算 4.
	 * 计算比较之后的time，和标准时间对比，计算时间
	 * 
	 * @param time
	 * @param standard
	 * @return
	 */
	public static double compareKSSJ(String time, String timefrom, String timeto) {
		DecimalFormat df = new DecimalFormat("######0.00");
		// 如果时间在21:00之后，直接返回0
		if (getDateWithStr(time, "HH:mm").after(getDateWithStr(timeto, "HH:mm"))) {
			return 0;
		}
		// 如果时间在09:00之前，计算以标准开始时间为准，到标准结束时间结束
		if ((getDateWithStr(time, "HH:mm").before(getDateWithStr(timefrom, "HH:mm")))
				|| (getDateWithStr(time, "HH:mm").equals(getDateWithStr(timefrom, "HH:mm")))) {
			double db = (double) timediff1(timeto, timefrom);
			return Double.valueOf(df.format(db / 60));
		}
		// 如果时间在09:00之后，计算以实际时间为准，到标准结束时间结束
		if (getDateWithStr(time, "HH:mm").after(getDateWithStr(timefrom, "HH:mm"))) {
			double db = (double) timediff1(timeto, time);
			return Double.valueOf(df.format(db / 60));
		}
		return 0;
	}

	/**
	 * 获取后一天时间
	 * 
	 * @param date
	 * @param flag
	 * @return
	 * @throws NullPointerException
	 */
	public static String getIncomeDate3(Date date, int flag) throws NullPointerException {
		if (null == date) {
			throw new NullPointerException("the date is null or empty!");
		}

		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);

		calendar.add(Calendar.DAY_OF_MONTH, +flag);

		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
		return s.format(calendar.getTime());
	}

	/**
	 * 把字符串时间转换成日期
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDateWithStr(String date, String flag) {
		SimpleDateFormat s = new SimpleDateFormat(flag);
		Date d = null;
		try {
			d = s.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}
}