package weaver.interfaces.jiangyl.hrm;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Cell;
import weaver.soa.workflow.request.DetailTable;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.Row;

public class HrmModifiedAction extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {
		RecordSet rs = new RecordSet();
		String rr = "";
		String hgw = "";
		String hbm = "";
		String hdw = "";
		String hsj = "";
		DetailTable[] detailtable = request.getDetailTableInfo().getDetailTable();// 获取所有明细表
		DetailTable dt = detailtable[0];// 指定明细表 0表示明细表1
		Row[] s = dt.getRow();// 当前明细表的所有数据,按行存储
		for (int j = 0; j < s.length; j++) {
			Row r = s[j];// 指定行
			Cell c[] = r.getCell();// 每行数据再按列存储
			for (int k = 0; k < c.length; k++) {
				Cell c1 = c[k];// 指定列
				String name = c1.getName();// 明细字段名称（对应明细表表单字段名称，如：mx_name）
				String value = c1.getValue();// 明细字段的值（对应明细表表单中的mx_name的值）
				if ("rr".equals(name)) {
					rr = value;
				}
				if ("hgw".equals(name)) {
					hgw = value;
				}
				if ("hbm".equals(name)) {
					hbm = value;
				}
				if ("hdw".equals(name)) {
					hdw = value;
				}
				if ("hsj".equals(name)) {
					hsj = value;
				}
			}
			String sql = "update hrmresource set jobtitle = '" + hgw + "', departmentid = '" + hbm
					+ "', subcompanyid1 = '" + hdw + "', managerid = '" + hsj + "' where id = '" + rr + "'";
			rs.execute(sql);
			String sql1 = "update cus_fielddata set field3 = '待同步' where id = '" + rr + "'";
			rs.execute(sql1);
		}
		return SUCCESS;
	}
}
