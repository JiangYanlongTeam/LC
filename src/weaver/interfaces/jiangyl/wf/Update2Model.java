package weaver.interfaces.jiangyl.wf;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

public class Update2Model extends BaseBean implements Action {

	public final static String MODELID = "18";
	
	@Override
	public String execute(RequestInfo request) {
		RecordSet rs = new RecordSet();
		String exsql = "select w.tablename from modeinfo m,workflow_bill w where w.id=m.formid and m.id = '"+MODELID+"'";
		writeLog("根据固定资产建模ID获取表名SQL：" + exsql);
		rs.execute(exsql);
		rs.next();
		String gsdmTableName = Util.null2String(rs.getString("tablename"));
		String requestid = Util.null2String(request.getRequestid());
		String sql = "update " + gsdmTableName + " set fkzt = '1' where lcbt = '"+requestid+"'";
		rs.execute(sql);
		return SUCCESS;
	}
}
