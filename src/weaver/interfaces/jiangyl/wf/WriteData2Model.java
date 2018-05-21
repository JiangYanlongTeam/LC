package weaver.interfaces.jiangyl.wf;

import java.text.SimpleDateFormat;
import java.util.Date;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

public class WriteData2Model extends BaseBean implements Action {

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
		String sqr = "";
		String sqrq = "";
		String fyzje = "";
		String gys = "";
		String ssbm = "";
		String zflb = "";
		Property[] properties = request.getMainTableInfo().getProperty();// 获取表单主字段信息
		for (int i = 0; i < properties.length; i++) {
			String name = properties[i].getName();// 主字段名称
			String value = Util.null2String(properties[i].getValue());// 主字段对应的值
			if (name.equals("sqr")) {
				sqr = value;
			}
			if (name.equals("sqrq")) {
				sqrq = value;
			}
			if (name.equals("fyzje")) {
				fyzje = value;
			}
			if (name.equals("gys")) {
				gys = value;
			}
			if (name.equals("ssbm")) {
				ssbm = value;
			}
			if (name.equals("zflb")) {
				zflb = value;
			}
		}
		if("".equals(gys) || null == gys || "0".equals(zflb)) {
			return SUCCESS;
		}
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
		insertRecordToLogTable(gsdmTableName, gys, sqr, sqrq, ssbm, fyzje, "0", fyzje, requestid, "0", date, time);

		return SUCCESS;
	}

	/**
	 * 插入到纪录表
	 * 
	 * @param date
	 * @param time
	 * @param addrid
	 * @param responseID
	 * @param message
	 * @param jsonData
	 */
	private void insertRecordToLogTable(String gsdmTableName, String gysmc, String sqr, String sqrq, String sqbm,
			String htje, String yfje, String wfje, String lcbt, String fkzt, String date, String time) {
		RecordSet rs = new RecordSet();
		String insertSQL = "insert into " + gsdmTableName
				+ " (gysmc,sqr,sqrq,sqbm,htje,yfje,wfje,lcbt,fkzt,formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime) "
				+ "values ('" + gysmc + "','" + sqr + "','" + sqrq + "','" + sqbm + "'," + htje + "," + yfje + ","
				+ wfje + ",'" + lcbt + "','" + fkzt + "','"+MODELID+"','"+sqr+"','0','" + date + "','" + time + "')";
		writeLog("插入纪录表SQL：" + insertSQL);
		rs.execute(insertSQL);
		String selectMaxIdSQL = "select max(id) id from " + gsdmTableName + "";
		rs.execute(selectMaxIdSQL);
		rs.next();
		String id = rs.getString("id");
		ModeRightInfo ModeRightInfo = new ModeRightInfo();
		ModeRightInfo.editModeDataShare(1, Integer.parseInt(MODELID), Integer.parseInt(id));
	}
}
