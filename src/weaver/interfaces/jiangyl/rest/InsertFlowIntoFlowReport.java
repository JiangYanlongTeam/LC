/**
 * 
 */
package weaver.interfaces.jiangyl.rest;

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
 * 查询时间范围内的流程数据，把数据整理并插入到流程效率报表中
 * 
 * @author jiangyanlong
 *
 */
public class InsertFlowIntoFlowReport extends BaseCronJob {

	public String syn(String date1,String date2) {
		StringBuffer sb = new StringBuffer();
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		BaseBean bean = new BaseBean();
		String modelid = bean.getPropValue("flowreport", "modelid");
		String exid = Util.null2String(bean.getPropValue("flowreport", "excluded_modelid"));
		StringBuffer wfids = new StringBuffer(",");
		List<String> nodelist = new ArrayList<String>();
		String exTableName = "";
		if (!"".equals(exid)) {
			String exsql = "select w.tablename from modeinfo m,workflow_bill w where w.id=m.formid and m.id = '" + exid + "'";
			bean.writeLog("根据excluded_modelid " + exid + " 获取表名SQL："+exsql);
			rs.execute(exsql);
			rs.next();
			exTableName = Util.null2String(rs.getString("tablename"));
			String getwfidsql = "select liucheng from " + exTableName + "_dt2";
			bean.writeLog("获取需要排除的workflowid SQL："+getwfidsql);
			rs.execute(getwfidsql);
			while(rs.next()) {
				String liucheng = Util.null2String(rs.getString("liucheng"));
				wfids.append("'");
				wfids.append(liucheng);
				wfids.append("'");
				wfids.append(",");
			}
			String getnodeidSQL = "select node from " + exTableName + "_dt1";
			bean.writeLog("获取nodeid SQL："+getnodeidSQL);
			rs.execute(getnodeidSQL);
			while(rs.next()) {
				String node = Util.null2String(rs.getString("node"));
				nodelist.add(node);
			}
		}
		
		String sql = "select nodeid,workflowid,requestid,(select requestname from workflow_requestbase where requestid =workflow_currentoperator.requestid )requestname, "
				+ " (select workflowname from workflow_base where id = workflow_currentoperator.workflowid) workflowname, "
				+ " (select loginid from hrmresource where id = workflow_currentoperator.userid) loginid, "
				+ " (select lastname from hrmresource where id = workflow_currentoperator.userid) loginname, "
				+ " (select subcompanyname from HrmSubCompany where id = (select subcompanyid1 from hrmresource where id = workflow_currentoperator.userid)) companyname, "
				+ " isremark,''bzlx,receivedate,receivetime,operatedate,operatetime "
				+ " from workflow_currentoperator " + " where operatedate >= '" + date1
				+ "' and operatetime >= '00:00:00' " + " and operatedate <= '" + date2
				+ "' and operatetime <= '23:59:59' and isremark = 2 and preisremark = 0";
		if(!",".equals(wfids.toString())) {
			String ids = wfids.toString();
			ids = ids.substring(1, ids.length()-1);
			sql += " and workflowid not in ("+ids+") ";
		}
		bean.writeLog("根据条件查询当当天的流程SQL：" + sql);
		
		String sql1 = "select w.tablename from modeinfo m,workflow_bill w where w.id=m.formid and m.id = '" + modelid
				+ "'";
		bean.writeLog("根据MODELID获取表单建模表：" + sql1);
		rs1.execute(sql1);
		rs1.next();
		String tableName = Util.null2String(rs1.getString("tablename"));

		rs.execute(sql);
		while (rs.next()) {
			String xtbs = "OA";
			String nodeid = Util.null2String(rs.getString("nodeid"));
			String lcid = Util.null2String(rs.getString("requestid"));
			String lcbt = Util.null2String(rs.getString("requestname"));
			String gzlmc = Util.null2String(rs.getString("workflowname"));
			String czzh = Util.null2String(rs.getString("loginid"));
			String czr = Util.null2String(rs.getString("loginname"));
			String czrgsmc = Util.null2String(rs.getString("companyname"));
			String ddrq = Util.null2String(rs.getString("receivedate"));
			String ddsj = Util.null2String(rs.getString("receivetime"));
			ddsj = ddsj.substring(0,ddsj.lastIndexOf(":"));
			String clrq = Util.null2String(rs.getString("operatedate"));
			String clsj = Util.null2String(rs.getString("operatetime"));
			clsj = clsj.substring(0,clsj.lastIndexOf(":"));
			String xtrksj = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String modedatacreatedate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			String modedatacreatetime = new SimpleDateFormat("HH:mm:ss").format(new Date());
			String wybs = xtbs + lcid + czzh + ddrq + ddsj;
			if(exculeNodeId(nodeid,exTableName)) {
				continue;
			}
			String bzlx = getBZLX(nodeid,nodelist);
			String sql2 = "select * from " + tableName + " where wybs = '" + wybs + "'";
			bean.writeLog("根据WYBS获取表单建模表中是否存在数据：" + sql2);
			rs1.execute(sql2);
			int count = rs1.getCounts();
			if (count > 0) {
				continue;
			}
			String sql3 = "insert into "+tableName+" (xtbs,lcid,lcbt,gzlmc,czzh,czr,czrgsmc,bzlx,ddrq,ddsj,clrq,clsj,xtrksj,wybs,formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime) "
					+ "values('"+xtbs+"','" + lcid + "','" + lcbt + "','" + gzlmc + "','" + czzh + "','" + czr
					+ "','" + czrgsmc + "','" + bzlx + "','" + ddrq + "','" + ddsj + "','" + clrq + "','" + clsj + "','"
					+ xtrksj + "','" + wybs + "','" + modelid + "','1','0','" + modedatacreatedate + "','"
					+ modedatacreatetime + "')";
			bean.writeLog("插入数据到表单建模表：" + sql3);
			sb.append("插入数据，流程ID："+lcid);
			sb.append("<br>");
			rs1.execute(sql3);
		}
		return sb.toString();
	}
	
	public void execute() {
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		String date = getIncomeDate3(new Date(), -1);
		BaseBean bean = new BaseBean();
		String modelid = bean.getPropValue("flowreport", "modelid");
		String exid = Util.null2String(bean.getPropValue("flowreport", "excluded_modelid"));
		StringBuffer wfids = new StringBuffer(",");
		List<String> nodelist = new ArrayList<String>();
		String exTableName = "";
		if (!"".equals(exid)) {
			String exsql = "select w.tablename from modeinfo m,workflow_bill w where w.id=m.formid and m.id = '" + exid + "'";
			bean.writeLog("根据excluded_modelid " + exid + " 获取表名SQL："+exsql);
			rs.execute(exsql);
			rs.next();
			exTableName = Util.null2String(rs.getString("tablename"));
			String getwfidsql = "select liucheng from " + exTableName + "_dt2";
			bean.writeLog("获取需要排除的workflowid SQL："+getwfidsql);
			rs.execute(getwfidsql);
			while(rs.next()) {
				String liucheng = Util.null2String(rs.getString("liucheng"));
				wfids.append("'");
				wfids.append(liucheng);
				wfids.append("'");
				wfids.append(",");
			}
			String getnodeidSQL = "select node from " + exTableName + "_dt1";
			bean.writeLog("获取nodeid SQL："+getnodeidSQL);
			rs.execute(getnodeidSQL);
			while(rs.next()) {
				String node = Util.null2String(rs.getString("node"));
				nodelist.add(node);
			}
		}
		
		String sql = "select nodeid,workflowid,requestid,(select requestname from workflow_requestbase where requestid =workflow_currentoperator.requestid )requestname, "
				+ " (select workflowname from workflow_base where id = workflow_currentoperator.workflowid) workflowname, "
				+ " (select loginid from hrmresource where id = workflow_currentoperator.userid) loginid, "
				+ " (select lastname from hrmresource where id = workflow_currentoperator.userid) loginname, "
				+ " (select subcompanyname from HrmSubCompany where id = (select subcompanyid1 from hrmresource where id = workflow_currentoperator.userid)) companyname, "
				+ " isremark,''bzlx,receivedate,receivetime,operatedate,operatetime "
				+ " from workflow_currentoperator " + " where operatedate >= '" + date
				+ "' and operatetime >= '00:00:00' " + " and operatedate <= '" + date
				+ "' and operatetime <= '23:59:59' and isremark = 2 and preisremark = 0";
		if(!",".equals(wfids.toString())) {
			String ids = wfids.toString();
			ids = ids.substring(1, ids.length()-1);
			sql += " and workflowid not in ("+ids+") ";
		}
		bean.writeLog("根据条件查询当当天的流程SQL：" + sql);
		
		String sql1 = "select w.tablename from modeinfo m,workflow_bill w where w.id=m.formid and m.id = '" + modelid
				+ "'";
		bean.writeLog("根据MODELID获取表单建模表：" + sql1);
		rs1.execute(sql1);
		rs1.next();
		String tableName = Util.null2String(rs1.getString("tablename"));

		rs.execute(sql);
		while (rs.next()) {
			String xtbs = "OA";
			String nodeid = Util.null2String(rs.getString("nodeid"));
			String lcid = Util.null2String(rs.getString("requestid"));
			String lcbt = Util.null2String(rs.getString("requestname"));
			String gzlmc = Util.null2String(rs.getString("workflowname"));
			String czzh = Util.null2String(rs.getString("loginid"));
			String czr = Util.null2String(rs.getString("loginname"));
			String czrgsmc = Util.null2String(rs.getString("companyname"));
			String ddrq = Util.null2String(rs.getString("receivedate"));
			String ddsj = Util.null2String(rs.getString("receivetime"));
			ddsj = ddsj.substring(0,ddsj.lastIndexOf(":"));
			String clrq = Util.null2String(rs.getString("operatedate"));
			String clsj = Util.null2String(rs.getString("operatetime"));
			clsj = clsj.substring(0,clsj.lastIndexOf(":"));
			String xtrksj = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String modedatacreatedate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			String modedatacreatetime = new SimpleDateFormat("HH:mm:ss").format(new Date());
			String wybs = xtbs + lcid + czzh + ddrq + ddsj;
			if(exculeNodeId(nodeid,exTableName)) {
				continue;
			}
			String bzlx = getBZLX(nodeid,nodelist);
			String sql2 = "select * from " + tableName + " where wybs = '" + wybs + "'";
			bean.writeLog("根据WYBS获取表单建模表中是否存在数据：" + sql2);
			rs1.execute(sql2);
			int count = rs1.getCounts();
			if (count > 0) {
				continue;
			}
			String sql3 = "insert into "+tableName+" (xtbs,lcid,lcbt,gzlmc,czzh,czr,czrgsmc,bzlx,ddrq,ddsj,clrq,clsj,xtrksj,wybs,formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime) "
					+ "values('"+xtbs+"','" + lcid + "','" + lcbt + "','" + gzlmc + "','" + czzh + "','" + czr
					+ "','" + czrgsmc + "','" + bzlx + "','" + ddrq + "','" + ddsj + "','" + clrq + "','" + clsj + "','"
					+ xtrksj + "','" + wybs + "','" + modelid + "','1','0','" + modedatacreatedate + "','"
					+ modedatacreatetime + "')";
			bean.writeLog("插入数据到表单建模表：" + sql3);
			rs1.execute(sql3);
		}
	}

	/**
	 * 排除NodeID
	 * 
	 * @param nodeID
	 * @return
	 */
	public boolean exculeNodeId(String nodeID, String exTableName) {
		if("".equals(exTableName)) {
			return false;
		}
		RecordSet rs = new RecordSet();
		String sql = "select jd from " + exTableName + "_dt3 where jd = '"+nodeID+"'";
		rs.execute(sql);
		int count = rs.getCounts();
		return count > 0;
	}
	
	/**
	 * 根据nodeid获取审批或者审核
	 * 
	 * @param nodeid
	 * @param requestid
	 * @return
	 */
	public String getBZLX(String nodeid,List<String> nodeids) {
		if(nodeids.contains(nodeid)) {
			return "审批";
		}
		return "审核";
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
}
