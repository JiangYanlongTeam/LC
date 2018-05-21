package weaver.interfaces.action;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

public class UpdateModeRight extends BaseBean implements Action {

	@Override
	public String execute(RequestInfo request) {

		String tjr_value = "";
		String tjr_column = "tjr";

		String dabh_value = "";
		String dabh_column = "dabh";

		Property[] properties = request.getMainTableInfo().getProperty();// 获取表单主字段信息
		for (int i = 0; i < properties.length; i++) {
			String name = properties[i].getName();// 主字段名称
			String value = Util.null2String(properties[i].getValue());// 主字段对应的值
			if (name.equals(tjr_column)) {
				tjr_value = value;
			}
			if (name.equals(dabh_column)) {
				dabh_value = value;
			}
		}

		if ("".equals(tjr_value)) {
			return SUCCESS;
		}
		if ("".equals(dabh_value)) {
			return SUCCESS;
		}
		String sql = "select daqx from uf_dakp where id = '" + dabh_value + "'";
		RecordSet rs = new RecordSet();
		rs.execute(sql);
		rs.next();
		String daqx = Util.null2String(rs.getString("daqx"));
		if ("".equals(daqx)) {
			String sql1 = "update uf_dakp set daqx = '" + tjr_value + "' where id = '" + dabh_value + "'";
			rs.execute(sql1);
		} else {
			daqx = daqx + "," + tjr_value;
			String sql1 = "update uf_dakp set daqx = '" + daqx + "' where id = '" + dabh_value + "'";
			rs.execute(sql1);
		}

		ModeRightInfo ModeRightInfo = new ModeRightInfo();
		ModeRightInfo.editModeDataShare(1, 363, Integer.parseInt(dabh_value));
		ModeRightInfo.editModeDataShare(1, 364, Integer.parseInt(dabh_value));
		ModeRightInfo.editModeDataShare(1, 365, Integer.parseInt(dabh_value));
		ModeRightInfo.editModeDataShare(1, 404, Integer.parseInt(dabh_value));
		ModeRightInfo.editModeDataShare(1, 406, Integer.parseInt(dabh_value));
		ModeRightInfo.editModeDataShare(1, 501, Integer.parseInt(dabh_value));
		return SUCCESS;
	}

}
