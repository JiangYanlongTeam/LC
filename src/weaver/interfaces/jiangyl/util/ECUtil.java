package weaver.interfaces.jiangyl.util;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.TimeUtil;
import weaver.general.Util;

public class ECUtil extends BaseBean {

	/**
	 * 
	 * 方法描述 : 获取流程数据集合 返回map
	 * 里面存在一个主表map(索引maindatamap)，每个明细表的List(索引dt1表示第一个明细，dt2表示第二个)
	 * 
	 * @param requestid
	 * @param formid
	 *            表单id
	 * @return Map
	 */
	public static Map<String, Object> getrequestdatamap(String requestid, int formid) {
		Map<String, Object> requestmap = new HashMap<String, Object>();
		formid = Math.abs(formid);
		RecordSet rs = new RecordSet();
		RecordSet rs2 = new RecordSet();

		String tablename = ""; // 表名
		rs.executeSql("select tablename from workflow_bill where id=-" + formid);
		if (rs.next()) {
			tablename = rs.getString("tablename");
		}

		/*
		 * 获取主表数据map
		 */
		Map<String, String> maindatamap = new HashMap<String, String>();
		rs.executeSql("select * from " + tablename + " where requestid=" + requestid);
		while (rs.next()) {
			// 遍历所有字段
			String[] colnames = rs.getColumnName();
			for (String name : colnames) {
				name = name.toLowerCase();
				maindatamap.put(name, Util.null2String(rs.getString(name)));
			}
		}
		requestmap.put("maindatamap", maindatamap);

		/*
		 * 获取明细表数据map
		 */
		rs.executeSql("select TABLENAME,orderid from WORKFLOW_BILLDETAILTABLE where BILLID=-" + formid);
		while (rs.next()) {
			String detailtablename = Util.null2String(rs.getString("tablename"));
			String orderid = Util.null2String(rs.getString("orderid"));
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			rs2.executeSql("select * from " + detailtablename + " where mainid=" + maindatamap.get("id"));
			while (rs2.next()) {
				Map<String, String> detaildatamap = new HashMap<String, String>();
				// 遍历所有字段
				String[] colnames = rs2.getColumnName();
				for (String name : colnames) {
					name = name.toLowerCase();
					detaildatamap.put(name, Util.null2String(rs2.getString(name)));
				}

				list.add(detaildatamap);
			}
			requestmap.put("dt" + orderid, list);
		}
		return requestmap;
	}

	/**
	 * 
	 * 方法描述 : 将数据插入表单建模,需要在表单建模中新建一个字段为uuid,长度要超过36
	 * 
	 * @param modeid
	 *            模块id
	 * @param userid
	 *            创建人id
	 * @param map
	 *            数据map
	 * @return int
	 */
	public static int createmodedata(int modeid, int userid, Map<String, ?> map) {
		int dataid = 0;
		RecordSet rs = new RecordSet();
		// 查询该模块表名
		rs.executeSql(
				"select b.TABLENAME,a.FORMID from modeinfo a left join workflow_bill b on a.FORMID=b.id where a.id="
						+ modeid);
		rs.next();
		String tablename = Util.null2String(rs.getString("tablename"));
		String uuid = UUID.randomUUID().toString();
		boolean flag = rs.executeSql("insert into " + tablename
				+ "(uuid,modedatacreater,modedatacreatedate,modedatacreatetime,formmodeid) values('" + uuid + "',"
				+ userid + ",'" + TimeUtil.getCurrentDateString() + "','" + TimeUtil.getOnlyCurrentTimeString() + "',"
				+ modeid + ")");
		if (flag) {
			rs.executeSql("select id from " + tablename + " where uuid='" + uuid + "'");
			rs.next();
			dataid = Util.getIntValue(rs.getString("id"));
			if (dataid > 0) {
				// 遍历数据 进行update
				String updatesql = "update " + tablename + " set ";
				Set<String> keySet = map.keySet();
				for (String key : keySet) {
					updatesql += key + "='" + map.get(key).toString() + "',";
				}
				if (updatesql.endsWith(",")) {
					updatesql = updatesql.substring(0, updatesql.length() - 1);
					updatesql += " where id=" + dataid;
					rs.executeSql(updatesql);
				}
				/*
				 * 进行权限重构
				 */
				ModeRightInfo moderight = new ModeRightInfo();
				moderight.editModeDataShare(userid, modeid, dataid);
			}
		}
		return dataid;
	}

	/**
	 * 
	 * 方法描述 : 通用的插入操作
	 * 
	 * @param tablename
	 *            表名
	 * @param map
	 *            数据map
	 * @return boolean
	 */
	public static boolean datainsert(String tablename, Map<String, ?> map, String datasource) {
		boolean flag = false;
		String insertsql = "insert into " + tablename + "";

		String keyString = "";
		String valueString = "";

		Set<String> keySet = map.keySet();
		for (String key : keySet) {
			keyString += key + ",";
			valueString += "'" + map.get(key).toString() + "',";
		}
		if (keyString.endsWith(",")) {
			keyString = keyString.substring(0, keyString.length() - 1);
		}
		if (valueString.endsWith(",")) {
			valueString = valueString.substring(0, valueString.length() - 1);
		}

		insertsql += "(" + keyString + ") values(" + valueString + ")";

		RecordSetDataSource rs = new RecordSetDataSource(datasource);
		flag = rs.executeSql(insertsql);

		return flag;
	}

	/**
	 * java对象转换为xml文件 <br>
	 * 
	 * String xmlstring = XMLUtil.beanToXml(model, JTCLFBXCreateModel.class);<br>
	 * @XmlRootElement(name="INPUT")<br>
	 * 									@XmlType(propOrder={"HEADER","LINE"})<br>
	 * 
	 * @param xmlPath
	 *            xml文件路径
	 * @param load
	 *            java对象.Class
	 * @return xml文件的String
	 * @throws JAXBException
	 */
	public static String beanToXml(Object obj, Class<?> load) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(load);
		Marshaller marshaller = context.createMarshaller();
		// marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "GBK");
		StringWriter writer = new StringWriter();
		marshaller.marshal(obj, writer);
		return writer.toString().replace("standalone=\"yes\"", "");
	}

	/**
	 * 方法描述 : 加法
	 * 
	 * @param a
	 * @param b
	 * @return String
	 */
	public static String floatAdd(String a, String b) {
		a = a.equals("") ? "0" : a;
		b = b.equals("") ? "0" : b;
		BigDecimal bg1 = new BigDecimal(a);
		BigDecimal bg2 = new BigDecimal(b);

		BigDecimal bd = bg1.add(bg2);

		return bd.toString();
	}

	/**
	 * 
	 * 方法描述 : 减法
	 * 
	 * @param a
	 * @param b
	 * @return String
	 */
	public static String floatSubtract(String a, String b) {
		a = a.equals("") ? "0" : a;
		b = b.equals("") ? "0" : b;
		BigDecimal bg1 = new BigDecimal(a);
		BigDecimal bg2 = new BigDecimal(b);
		BigDecimal bd = bg1.subtract(bg2);
		return bd.toString();
	}

	/**
	 * 
	 * 方法描述 : 除法
	 * 
	 * @param a
	 * @param b
	 * @return String
	 */
	public static String floatDivide(String a, String b) {
		a = a.equals("") ? "0" : a;
		b = b.equals("") ? "0" : b;
		BigDecimal bg1 = new BigDecimal(a);
		BigDecimal bg2 = new BigDecimal(b);

		BigDecimal bd = bg1.divide(bg2, 2, BigDecimal.ROUND_HALF_UP);
		return bd.toString();
	}

	/**
	 * 
	 * 方法描述 : 除法
	 * 
	 * @param a
	 * @param b
	 * @param n
	 * @return String
	 */
	public static String floatDivide(String a, String b, int n) {
		a = a.equals("") ? "0" : a;
		b = b.equals("") ? "0" : b;
		BigDecimal bg1 = new BigDecimal(a);
		BigDecimal bg2 = new BigDecimal(b);

		BigDecimal bd = bg1.divide(bg2, n, BigDecimal.ROUND_HALF_UP);
		return bd.toString();
	}

	/**
	 * 
	 * 方法描述 : 乘法
	 * 
	 * @param a
	 * @param b
	 * @return String
	 */
	public static String floatMultiply(String a, String b) {
		a = a.equals("") ? "0" : a;
		b = b.equals("") ? "0" : b;
		BigDecimal bg1 = new BigDecimal(a);
		BigDecimal bg2 = new BigDecimal(b);

		BigDecimal bd = bg1.multiply(bg2).setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.toString();
	}

	/**
	 * 
	 * 方法描述 : 比较大小
	 * 
	 * @param a
	 * @param b
	 * @return int
	 */
	public static int floatCompare(String a, String b) {
		a = a.equals("") ? "0" : a;
		b = b.equals("") ? "0" : b;
		BigDecimal bg1 = new BigDecimal(a);
		BigDecimal bg2 = new BigDecimal(b);

		return bg1.compareTo(bg2);
	}

	/**
	 * 方法描述 : 计算两段日期的重合日期
	 * 
	 * @param str1
	 *            开始日期1
	 * @param str2
	 *            结束日期1
	 * @param str3
	 *            开始日期2
	 * @param str4
	 *            结束日期2
	 * @return
	 * @throws Exception
	 *             Map<String,Object>
	 */
	public static Map<String, Object> comparisonRQ(String str1, String str2, String str3, String str4)
			throws Exception {
		String mesg = "";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String startdate = "";
		String enddate = "";
		try {
			Date dt1 = df.parse(str1);
			Date dt2 = df.parse(str2);
			Date dt3 = df.parse(str3);
			Date dt4 = df.parse(str4);
			if (dt1.getTime() <= dt3.getTime() && dt3.getTime() <= dt2.getTime() && dt2.getTime() <= dt4.getTime()) {
				mesg = "f";// 重合
				startdate = str3;
				enddate = str2;
			}
			if (dt1.getTime() >= dt3.getTime() && dt3.getTime() <= dt2.getTime() && dt2.getTime() <= dt4.getTime()) {
				mesg = "f";// 重合
				startdate = str1;
				enddate = str2;
			}

			if (dt3.getTime() <= dt1.getTime() && dt1.getTime() <= dt4.getTime() && dt4.getTime() <= dt2.getTime()) {
				mesg = "f";// 重合
				startdate = str1;
				enddate = str4;
			}
			if (dt3.getTime() >= dt1.getTime() && dt1.getTime() <= dt4.getTime() && dt4.getTime() <= dt2.getTime()) {
				mesg = "f";// 重合
				startdate = str3;
				enddate = str4;
			}

			System.out.println(startdate + "----" + enddate);

		} catch (ParseException e) {
			e.printStackTrace();
			throw new ParseException(e.getMessage(), 0);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("startdate", startdate);
		map.put("enddate", enddate);
		return map;
	}

	/**
	 * 
	 * 方法描述 : 计算相差分钟数
	 * 
	 * @param startTime
	 * @param endTime
	 * @return int
	 */
	public static int daysBetween2(String startTime, String endTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		long time1 = 0;
		long time2 = 0;

		try {
			cal.setTime(sdf.parse(startTime));
			time1 = cal.getTimeInMillis();
			cal.setTime(sdf.parse(endTime));
			time2 = cal.getTimeInMillis();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long between_days = (time2 - time1) / (1000 * 60);

		return Integer.parseInt(String.valueOf(between_days));
	}

	/**
	 * 
	 * 方法描述 : 日期比较
	 * 
	 * @param DATE1
	 * @param DATE2
	 * @return int
	 */
	public static int compare_date(String DATE1, String DATE2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date dt1 = df.parse(DATE1);
			Date dt2 = df.parse(DATE2);
			if (dt1.getTime() > dt2.getTime()) {
				System.out.println("dt1 在dt2前");
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				System.out.println("dt1在dt2后");
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

	/**
	 * 
	 * 方法描述 : 获取字段的ID 创建者：ocean 项目名称： ecologyTest 类名： CwUtil.java 版本： v1.0 创建时间：
	 * 2013-9-14 下午3:05:53
	 * 
	 * @param workFlowId
	 *            流程id
	 * @param num
	 *            明细表
	 * @return Map
	 */
	public static Map getFieldId(int formid, String num) {

		formid = Math.abs(formid);
		String sql = "";
		if ("0".equals(num)) {
			sql = "select b.id,fieldname,detailtable from workflow_billfield b ,workflow_base a where b.billid=-"
					+ formid + " and a.formid=b.billid and (detailtable='' or detailtable is null)";
		} else {
			sql = "select b.id,fieldname,detailtable from workflow_billfield b ,workflow_base a where b.billid=-"
					+ formid + " and a.formid=b.billid and detailtable='formtable_main_" + formid + "_dt" + num + "'";
		}

		RecordSet rs = new RecordSet();
		rs.execute(sql);
		Map array = new HashMap();
		while (rs.next()) {
			array.put(Util.null2String(rs.getString("fieldname")).toLowerCase(), Util.null2String(rs.getString("id")));
		}
		return array;
	}

	/**
	 * 
	 * @Title: getlabelId @Description: TODO @param @param name 数据库字段名称
	 *         小写 @param @param formid 表单ID @param @param ismain 是否主表 0:主表
	 *         1：明细表 @param @param num 明细表序号 @param @return @return String @throws
	 */
	public static String getlabelId(String name, int formid, String ismain, String num) {
		name = name.toLowerCase();
		String id = "";
		String sql = "";
		formid = formid * -1;

		if ("0".equals(ismain)) {
			sql = "select id,fieldname,detailtable from workflow_billfield where billid=-" + formid
					+ " and (detailtable='' or detailtable is null) and lower(fieldname)='" + name + "'";
		} else {
			sql = "select id,fieldname,detailtable from workflow_billfield where billid=-" + formid
					+ " and detailtable='formtable_main_" + formid + "_dt" + num + "' and lower(fieldname)='" + name
					+ "'";
		}
		// System.out.println(sql);
		RecordSet rs = new RecordSet();
		rs.execute(sql);
		if (rs.next()) {
			id = Util.null2String(rs.getString("id"));
		}
		return id;

	}

	/**
	 * 选择框 显示名
	 * 
	 * @param val
	 * @param id
	 * @return
	 */
	public static String getselectName(String val, String id) {
		String name = "";

		if (val.equals("")) {

			return "";
		}
		RecordSet recordSet = new RecordSet();

		recordSet.executeSql("select selectname,fieldid from workflow_selectItem where fieldid ="
				+ Util.getIntValue(id, 0) + "  and selectvalue = " + Util.getIntValue(val, 0));
		if (recordSet.next()) {
			name = recordSet.getString("selectname");
		}

		return name;

	}

	/**
	 * 选择框 options
	 * 
	 * @param val
	 * @param id
	 * @return
	 */
	public static String getselectOptionStr(String val, String id) {

		String str = "<option value=''></option>";

		RecordSet recordSet = new RecordSet();
		recordSet.executeSql("select selectname,fieldid,selectvalue from workflow_selectItem  where fieldid = "
				+ Util.getIntValue(id, 0) + " order by selectvalue");
		while (recordSet.next()) {
			String name = Util.null2String(recordSet.getString("selectname"));
			String value = Util.null2String(recordSet.getString("selectvalue"));

			if (value.equals(val)) {
				str += "<option value='" + value + "' selected>" + name + "</option>";
			} else {
				str += "<option value='" + value + "'>" + name + "</option>";
			}
		}

		return str;
	}

	/**
	 * 判断节点是否为创建节点
	 * 
	 * @param nodeid
	 * @return
	 */
	public static boolean judgeNodeStart(String nodeid) {

		boolean falg = false;
		String isstart = "0";

		RecordSet rs = new RecordSet();
		rs.executeSql("select * from workflow_nodebase where id=" + Util.getIntValue(nodeid, 0));
		if (rs.next()) {
			isstart = rs.getString("isstart");
		}

		if ("1".equals(isstart)) {
			falg = true;
		}

		return falg;
	}

	/**
	 * 获取节点名称
	 * 
	 * @param nodeid
	 * @return
	 */
	public static String getNodename(String nodeid) {

		String nodename = "";

		RecordSet rs = new RecordSet();
		rs.executeSql("select id,nodename from workflow_nodebase where id=" + Util.getIntValue(nodeid, 0));
		if (rs.next()) {
			nodename = rs.getString("nodename");
		}
		return nodename;
	}

	/**
	 * 
	 * 方法描述 : 获取表单字段的map集合 流程/建模 均可适用 （字段全部小写）
	 * 
	 * @param formid
	 *            表单id
	 * @param num
	 *            明细表序号(1表示明细表1,2表示明细表2)
	 * @return Map
	 */
	public static Map getfieldmap(int formid, String num) {
		RecordSet rs = new RecordSet();
		formid = Math.abs(formid);
		String sql = "";
		if ("0".equals(num)) {
			sql = " SELECT id,fieldname,detailtable FROM workflow_billfield where billid=-" + formid
					+ " and detailtable is null ";
		} else {
			// 如果是明细表 有可能表名是自定义的，所有先查处表名
			rs.executeSql("select TABLENAME from WORKFLOW_BILL where id=-" + formid);
			rs.next();
			String tablename = Util.null2String(rs.getString("tablename"));

			sql = " SELECT id,fieldname,detailtable FROM workflow_billfield where billid=-" + formid
					+ " and detailtable='" + tablename + "_dt" + num + "' ";
		}

		rs.execute(sql);
		Map array = new HashMap();
		while (rs.next()) {
			array.put(Util.null2String(rs.getString("fieldname")).toLowerCase(), Util.null2String(rs.getString("id")));
		}
		return array;
	}
}
