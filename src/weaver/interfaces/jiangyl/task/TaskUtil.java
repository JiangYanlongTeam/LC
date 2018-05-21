package weaver.interfaces.jiangyl.task;

import java.util.ArrayList;
import java.util.List;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;

public class TaskUtil extends BaseBean {
	
	public String getWfName(String id,String uid) {
		if(null == id || id.equals("")){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		String[] strs = id.split(",");
		RecordSet rs = new RecordSet();
		for(int i = 0; i < strs.length; i++) {
			try{
				String wfid = "";
				String currentnodeid = "";
				String creater = "";
				rs.executeSql("select creater,workflowid,currentnodeid from workflow_requestbase where requestid="+strs[i]);
				if(rs.next()){
					creater = rs.getString(1);
					wfid = rs.getString(2);
					currentnodeid = rs.getString(3);
				}
				rs.executeSql("select 1 from workflow_currentoperator where requestid="+strs[i]+" and userid="+uid);
				if(!rs.next()){
					String sql = "select wfid,userid from Workflow_SharedScope where wfid ="+wfid +" and requestid = "+strs[i]+" and iscanread = 1 and operator = '"+creater+"' and currentnodeid = "+currentnodeid+" and userid = "+uid ;
					rs.executeSql(sql)   ;
					if(!rs.next()){
					   sql = " insert into Workflow_SharedScope (wfid,requestid,permissiontype,userid,iscanread,operator,currentnodeid) values ("+wfid+","+strs[i]+",5,"+uid+",1,"+creater+","+currentnodeid+")"   ;
					   rs.executeSql(sql);
					   rs.executeSql("update workflow_base set isshared = 1 where id="+wfid);
					}
				}
			}catch(Exception e){
				
			}
			
			rs.execute("select requestname from workflow_requestbase where requestid = '"+strs[i]+"'");
			rs.next();
			String requestname = rs.getString("requestname");
			sb.append("<a href=\"/workflow/request/ViewRequest.jsp?isovetiem=0&requestid="+strs[i]+"\" target=\"_blank\">"+requestname+"</a>");
			if(i < strs.length -1) {
				sb.append("<br>");
			}
		}
		return sb.toString();
	}
	
	public String getDocName(String id) {
		if(null == id || id.equals("")){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		String[] strs = id.split(",");
		RecordSet rs = new RecordSet();
		for(int i = 0; i < strs.length; i++) {
			rs.execute("select docsubject from docdetail where id = '"+strs[i]+"'");
			rs.next();
			String docsubject = rs.getString("docsubject");
			sb.append("<a href=\"/docs/docs/DocDsp.jsp?id="+strs[i]+"\" target=\"_blank\">"+docsubject+"</a>");
			if(i < strs.length -1) {
				sb.append("<br>");
			}
		}
		return sb.toString();
	}
	
	public String getTaskNameWithHref(String name,String id){
		return "<a href=\"/proj/process/ViewTask.jsp?taskrecordid="+id+"\" target=\"_blank\">"+name+"</a>";
	}
	
	public String getProjectNameWithHref(String id){
		if("".equals(id)) {
			return "";
		}
		RecordSet rs = new RecordSet();
		rs.execute("select name from prj_projectinfo where id = '"+id+"'");
		rs.next();
		String name = Util.null2String(rs.getString("name"));
		return "<a href=\"/proj/data/ViewProject.jsp?ProjID="+id+"\" target=\"_blank\">"+name+"</a>";
	}
	
	public List getCanDeleList(String id,String status) {
		//完成 延期 停滞 正常 新建协作 新建日程 编辑 删除 共享设置 相关交流
		ArrayList list = new ArrayList();
        RecordSet rs = new RecordSet();
        if(status.equals("0")) {
        	list.add("false");
        	list.add("false");
        	list.add("false");
        	list.add("false");
        	list.add("false");
        	list.add("false");
        	list.add("false");
        	list.add("false");
        	list.add("false");
        	list.add("false");
        }else if(status.equals("1")){
        	list.add("true");
    		list.add("false");
        	list.add("true");
        	list.add("false");
        	list.add("true");
        	list.add("true");
        	list.add("true");
    		list.add("false");
    		list.add("true");
    		list.add("true");
        } else if(status.equals("2")){
        	list.add("false");
        	list.add("false");
        	list.add("false");
        	list.add("false");
        	list.add("true");
        	list.add("true");
        	list.add("false");
        	list.add("false");
        	list.add("true");
        	list.add("true");
        } else if(status.equals("3")){
        	//完成 延期 停滞 正常 新建协作 新建日程 编辑 删除 共享设置 相关交流
        	list.add("true");
        	list.add("false");
        	list.add("false");
        	list.add("true");
        	list.add("true");
        	list.add("true");
        	list.add("true");
        	list.add("false");
        	list.add("true");
        	list.add("true");
        }
        return list;
	}
	public String calCE(String realDay,String sjksrq){
		String[] sjksrqs = sjksrq.split("\\+");
		RecordSet rs = new RecordSet();
		String s = calActualDay(realDay,sjksrqs[0]);
		String sql = "select (" + sjksrqs[1] + "-" + s + ") day";
		rs.execute(sql);
		rs.next();
		String day = rs.getString("day");
		return day;
	}

	public String calActualDay(String realDay,String sjksrq) {
		if(toInt(realDay) && null != sjksrq && !"".equals(sjksrq.trim())) {
			RecordSet rs = new RecordSet();
			String sql = "SELECT DATEDIFF(DAY,'"+sjksrq+"',getdate()) day";
			rs.execute(sql);
			rs.next();
			String day = rs.getString("day");
			return day;
		}
		return realDay;
	}
	
	private static boolean toInt(String s ) {
		if(s.equals("0")) {
			s = "0";
		}
		if(s.equals("0.00")) {
			s = "0";
		}
		return Float.parseFloat(s) == 0.0;
	}
	
	public static void main(String[] args) {
	}
}
