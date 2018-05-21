package weaver.interfaces.jiangyl.rest;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;

public class ProcessReport extends BaseBean {

	public List<Map<String, String>> getData(User user, Map<String, String> otherparams, HttpServletRequest request,
			HttpServletResponse response) {
		RecordSet rs = new RecordSet();
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		String modelid = getPropValue("flowreport", "modelid");
		String sql1 = "select w.tablename from modeinfo m,workflow_bill w where w.id=m.formid and m.id = '" + modelid
				+ "'";
		writeLog("根据MODELID获取表单建模表：" + sql1);
		rs.execute(sql1);
		rs.next();
		String tableName = Util.null2String(rs.getString("tablename"));
		
		String xtbs = Util.null2String(otherparams.get("xtbs"));
		String gsmc = Util.null2String(otherparams.get("gsmc"));
		String shijian = Util.null2String(otherparams.get("shijian"));
		String shijianz = Util.null2String(otherparams.get("shijianz"));
		String bzlx = Util.null2String(otherparams.get("bzlx"));
		String czzh = Util.null2String(otherparams.get("czzh"));

		DecimalFormat df = new DecimalFormat("######0.00");
		
		String getlccssql = "select count(b.id) from "+tableName+" b where b.czzh = a.czzh and b.sfcs = '是' ";
		
		if (!"".equals(shijian)) {
			getlccssql += " and b.clrq >= '" + shijian + "' ";
		}
		if (!"".equals(shijianz)) {
			getlccssql += " and b.clrq <= '" + shijianz + "' ";
		}
		if (!"".equals(bzlx)) {
			getlccssql += " and b.bzlx = '" + bzlx + "' ";
		}
		if (!"".equals(xtbs)) {
			getlccssql += " and b.xtbs = '" + xtbs + "'";
		}
		if (!"".equals(gsmc)) {
			getlccssql += " and b.czrgsmc = '" + gsmc + "'";
		}
		if (!"".equals(czzh)) {
			getlccssql += " and b.czzh = '" + czzh + "'";
		}
		
		String fromsql = "select a.czr,a.czzh,a.id,a.clsc,a.czrgsmc,("+getlccssql+") lccssl from "+tableName+" a where 1=1 ";
		if (!"".equals(shijian)) {
			fromsql += " and a.clrq >= '" + shijian + "' ";
		}
		if (!"".equals(shijianz)) {
			fromsql += " and a.clrq <= '" + shijianz + "' ";
		}
		if (!"".equals(bzlx)) {
			fromsql += " and a.bzlx = '" + bzlx + "' ";
		}
		if (!"".equals(xtbs)) {
			fromsql += " and a.xtbs = '" + xtbs + "'";
		}
		if (!"".equals(gsmc)) {
			fromsql += " and a.czrgsmc = '" + gsmc + "'";
		}
		if (!"".equals(czzh)) {
			fromsql += " and a.czzh = '" + czzh + "'";
		}

		String getsql = "select a.czr,a.czzh,a.czrgsmc,count(a.id) ycllcsl,"
				+ "(sum(a.clsc)/count(a.id)) pjclsc,lccssl " + "from (" + fromsql + ") a "
				+ "group by a.czr,a.czzh,a.czrgsmc,lccssl";
		writeLog("查询条件：xtbs[" + xtbs + "] gsmc[" + gsmc + "] [" + czzh + "] shijian[" + shijian + "] shijianz["
				+ shijianz + "] bzlx[" + bzlx + "] SQL:" + getsql);
		rs.execute(getsql);
		while (rs.next()) {
			Map<String, String> jo = new HashMap<String, String>();
			String xingming = Util.null2String(rs.getString("czr"));
			String zhanghao = Util.null2String(rs.getString("czzh"));
			String gongsimingcheng = Util.null2String(rs.getString("czrgsmc"));
			String ycllcsl = df.format(Double.parseDouble(Util.null2o(rs.getString("ycllcsl"))));
			String pjclsc = df.format(Double.parseDouble(Util.null2o(rs.getString("pjclsc"))));
			String cslcsl = df.format(Double.parseDouble(Util.null2o(rs.getString("lccssl"))));

			jo.put("xingming", xingming);
			jo.put("zhanghao", zhanghao);
			jo.put("gongsimingcheng", gongsimingcheng);
			jo.put("ycllcsl", ycllcsl);
			jo.put("pjclsc", pjclsc);
			jo.put("cslcsl", cslcsl);
			data.add(jo);
		}
		return data;
	}
}
