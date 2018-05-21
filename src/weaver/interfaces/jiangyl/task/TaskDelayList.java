package weaver.interfaces.jiangyl.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;

public class TaskDelayList extends BaseBean {

	public List<Map<String, String>> getData(User user, Map<String, String> otherparams, HttpServletRequest request,
			HttpServletResponse response) {
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		String projectname = Util.null2String(otherparams.get("projectname")); // 项目ID
		String taskname = Util.null2String(otherparams.get("taskname")); // 任务名称
		String taskstatus = Util.null2String(otherparams.get("taskstatus"));// 任务状态
		String sssyq = Util.null2String(otherparams.get("sssyq")); // 所属事业群
		String xmlx = Util.null2String(otherparams.get("xmlx"));// 项目类型

		String sql = "";
//		String sql = "select a.id,a.prjid,b.kdfzr,b.prjtype,a.subject,a.hrmid,a.begindate,a.enddate,a.actualbegindate,a.actualenddate,a.taskstatus,a.finish,a.txts,a.workday, "
//				+ "				b.sssyq,b.ssyyzx,b.name,b.xmdx "
//				+ "				from prj_taskprocess a,prj_projectinfo b where 1=1 and a.level_n <= 10 and a.prjid = b.id "
//				+ " and "+"(t1.hrmid like '%,"+userid+",%' or t1.hrmid like '"+userid+",%' or t1.hrmid like '%,"+userid+"' or t1.hrmid = '"+userid+"' )";

		if (!"".equals(projectname)) {
			sql += " and b.name like '%" + projectname + "%'";
		}
		if (!"".equals(taskname)) {
			sql += " and a.subject like '%" + taskname + "%'";
		}
		if (!"".equals(taskstatus)) {
			sql += " and a.taskstatus = '" + taskstatus + "'";
		}
		if (!"".equals(sssyq)) {
			sql += " and a.sssyq = '" + sssyq + "'";
		}
		if (!"".equals(xmlx)) {
			sql += " and b.prjtype = '" + xmlx + "'";
		}
		sql += " order by a.prjid,to_number(a.taskindex) asc ";
		RecordSet rs = new RecordSet();
		rs.execute(sql);
		while (rs.next()) {
			Map<String, String> map = new HashMap<String, String>();
			String prjid = Util.null2String(rs.getString("prjid"));
			String subject = Util.null2String(rs.getString("subject"));
			String hrmid = Util.null2String(rs.getString("hrmid"));
			String id = Util.null2String(rs.getString("id"));
			String kdfzr = Util.null2String(rs.getString("kdfzr"));
			String workday = Util.null2o(rs.getString("workday"));
			String begindate = Util.null2String(rs.getString("begindate"));
			String enddate = Util.null2String(rs.getString("enddate"));
			String txts = Util.null2o(rs.getString("txts"));
			String actualBeginDate = Util.null2String(rs.getString("actualBeginDate"));
			String actualenddate = Util.null2String(rs.getString("actualenddate"));
			String taskstat = Util.null2String(rs.getString("taskstatus")); // 0 未启动 1 进行中 2 完成
			String taskstatusname = "";
			if ("0".equals(taskstat)) {
				taskstatusname = "未启动";
			}
			if ("1".equals(taskstat)) {
				taskstatusname = "进行中";
			}
			if ("2".equals(taskstat)) {
				taskstatusname = "完成";
			}
			String finish = Util.null2String(rs.getString("finish"));
			String sfyq = "";
			if ("0".equals(taskstat) || "".equals(taskstat)) {
				sfyq = "正常";
			}
			if ("1".equals(taskstat)) {
				sfyq = getSFYQ(taskstat, actualBeginDate, actualenddate, workday, txts);
			}
			if ("2".equals(taskstat)) {
				sfyq = getSFYQ(taskstat, actualBeginDate, actualenddate, workday, txts);
			}
			
			if("延期".equals(sfyq)) {
				map.put("prjid", prjid);
				map.put("id", id);
				map.put("kdfzr", kdfzr);
				map.put("subject", subject);
				map.put("hrmid", hrmid);
				map.put("begindate", begindate);
				map.put("enddate", enddate);
				map.put("actualBeginDate", actualBeginDate);
				map.put("taskstatus", taskstatusname);
				map.put("finish", finish);
				map.put("sfyq", sfyq);
				data.add(map);
			}
		}
		return data;
	}

	public String getSFYQ(String taskstat, String actualBeginDate, String actualenddate, String workday, String txts) {
		String currentdate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		if ("1".equals(taskstat)) { // 1 进行中
			int count = Util.dayDiff(actualBeginDate, currentdate);
			// 当前日期 - 实际开始日期 > 工期 = 延期
			if (count > Integer.parseInt(workday)) {
				return "延期";
			}
			// 当前日期 - 实际开始日期 < 工期 - 预警天数 = 正常
			if (count < (Integer.parseInt(workday) - Integer.parseInt(txts))) {
				return "正常";
			}
			// 当前日期 - 实际开始日期 >= 工期 - 预警天数 = 延期预警
			if (count >= (Integer.parseInt(workday) - Integer.parseInt(txts))) {
				return "延期预警";
			}
		}
		if ("2".equals(taskstat)) { // 2 完成
			int count = Util.dayDiff(actualenddate, actualBeginDate);
			// 实际结束日期 - 实际开始日期 > 工期 = 延期
			if (count > Integer.parseInt(workday)) {
				return "延期";
			}
			// 实际结束日期 - 实际开始日期 <= 工期 = 正常
			if (count <= Integer.parseInt(workday)) {
				return "正常";
			}
		}
		return "";
	}

	public static void main(String[] args) {
		System.out.println(Util.dayDiff("2018-01-02", "2018-01-10"));
	}
}
