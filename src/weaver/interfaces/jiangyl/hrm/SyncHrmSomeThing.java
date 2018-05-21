package weaver.interfaces.jiangyl.hrm;

import java.text.SimpleDateFormat;
import java.util.Date;


import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

public class SyncHrmSomeThing extends BaseCronJob {

	@Override
	public void execute() {
		String currentDateTime = new SimpleDateFormat("MM-dd").format(new Date());
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		// 根据出生日期更新年龄
		String[] tableNames = new String[] { "formtable_main_261", "formtable_main_251", "formtable_main_263",
				"formtable_main_266" };
		for (int i = 0; i < tableNames.length; i++) {
			String tbName = tableNames[i];
			String sql = "select id, csrq, nl, cjgzsj, jrsj, gln, sln from " + tbName + "";
			rs.execute(sql);
			while (rs.next()) {
				String id = Util.null2String(rs.getString("id"));
				String csrq = Util.null2String(rs.getString("csrq"));
				String cjgzsj = Util.null2String(rs.getString("cjgzsj"));
				String jrsj = Util.null2String(rs.getString("jrsj"));

				// 如果出生日期为空，不算生日
				if (!"".equals(csrq)) {
					String csrqmonth = csrq.substring(5, csrq.length());
					if (csrqmonth.equals(currentDateTime)) {
						String updateNLSQL = "update " + tbName + " set nl=ISNULL(nl,0)+1 where id = '" + id + "'";
						rs1.execute(updateNLSQL);
					}
				}
				// 如果参加工作时间为空，不算工龄
				if (!"".equals(cjgzsj)) {
					String cjgzsjmonth = cjgzsj.substring(5, cjgzsj.length());
					if (cjgzsjmonth.equals(currentDateTime)) {
						String updateNLSQL = "update " + tbName + " set gln=ISNULL(gln,0)+1 where id = '" + id + "'";
						rs1.execute(updateNLSQL);
					}
				}
				// 如果加入时间为空，不算司龄
				if (!"".equals(jrsj)) {
					String day = jrsj.substring(8, jrsj.length());
					if (day.equals("01")) {
						String updateNLSQL = "update " + tbName + " set sln=ISNULL(sln,0)+1 where id = '" + id + "'";
						rs1.execute(updateNLSQL);
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		String jrsj = "2017-01-02";
		System.out.println(jrsj.substring(8, jrsj.length()).equals("02"));
	}
}
