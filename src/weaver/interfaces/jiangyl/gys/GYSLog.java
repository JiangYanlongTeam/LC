package weaver.interfaces.jiangyl.gys;

import com.weaver.general.Util;

import weaver.conn.RecordSet;
import weaver.hrm.HrmUserVarify;
import weaver.interfaces.jiangyl.gys.model.JSONMode;

public class GYSLog {

	public void insert(JSONMode mode) {
		String sql = new StringBuilder().append("insert into ").toString();
	}
	
	public String getTable(String modeid) {
		RecordSet localRecordSet = new RecordSet();
		String str3 = new StringBuilder()
				.append("select w.tablename,w.id from modeinfo m,workflow_bill w where w.id=m.formid and m.id=")
				.append(modeid).toString();
		localRecordSet.executeSql(str3);
		localRecordSet.next();
		String str7 = Util.null2String(localRecordSet.getString("tablename"));
		return str7;
	}
}

