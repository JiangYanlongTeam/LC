package weaver.interfaces.jiangyl.cw;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weaver.conn.RecordSet;
import weaver.general.Util;

public class CWReport extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String year = Util.null2String(req.getParameter("nd"));
		String cbzx = Util.null2String(req.getParameter("cbzx"));
		/**
		 * 设置第一行数据
		 * 
		 **/
		String[] headers = new String[13];
		for (int i = 0; i < headers.length; i++) {
			if (i == headers.length - 1) {
				headers[i] = "全年汇总";
			} else {
				headers[i] = (i + 1) + "月";
			}
		}

		/**
		 * 设置第二行数据
		 * 
		 **/
		String[] secondHeaders = new String[13];

		/**
		 * 获取科目列表
		 * 返回一个LinkedList集合，第一层是KMInfo对象，包含科目ID，科目名称，科目编码，科目等级，LinedList<KMInfo>对象（三级对象）
		 * 
		 **/
		LinkedList<KMInfo> kmLinkedList = getKMINFO();

		/**
		 * 计算数据，最终返回 LinkedHashMap<KMInfo, LinkedList<Object>> 结构
		 * 
		 * 1. 获取二级科目的预算数（需要根据二级科目的ID获取三级科目的预算总和）
		 * 
		 * 2. 根据二级科目的发生数（需要根据二级科目的ID获取三级科目的ID甚至四级科目的ID，获取总的发生数）
		 * 
		 **/

		LinkedHashMap<KMInfo, LinkedList<Object>> ddLinkedHashMap = getDataBySearchConditions(kmLinkedList, year, cbzx);

		StringBuffer s = new StringBuffer();
		s.append(" <table class=\"gridtable\" width=\"90%\">");
		s.append("<tr><th colspan=2 rowspan=2 >科目/科目编码</th>");
		for (int i = 0; i < headers.length; i++) {
			String map = headers[i];
			s.append("<th colspan=2 >" + map + "</th>");
		}
		s.append("</tr>");
		s.append("<tr>");
		for (int i = 0; i < secondHeaders.length; i++) {
			s.append("<th >预算数</th>");
			s.append("<th >发生数</th>");
		}
		s.append("</tr>");
		for (Entry<KMInfo, LinkedList<Object>> entry : ddLinkedHashMap.entrySet()) {
			s.append("<tr>");
			KMInfo key = entry.getKey();
			LinkedList<Object> list = entry.getValue();
			for (int n = 0; n < list.size(); n++) {
				if (key.getLevel().equals("2")) {
					s.append("<th >" + list.get(n) + "</th>");
				} else {
					s.append("<td nowrap >" + list.get(n) + "</td>");
				}
			}
			s.append("</tr>");
		}
		req.setAttribute("cwreport", s.toString());
		req.setAttribute("cwreportdata", ddLinkedHashMap);
		req.getRequestDispatcher("/interface/jiangyl/cwspbudget.jsp").forward(req, resp);
	}

	/**
	 * 获取科目信息
	 * 
	 * @return
	 */
	public LinkedList<KMInfo> getKMINFO() {
		LinkedList<KMInfo> kmLinkedList = new LinkedList<KMInfo>();
		// 1 先查询二级科目
		RecordSet rs = new RecordSet();
		RecordSet rs1 = new RecordSet();
		String sql = "select id,codeName,name,feelevel from fnabudgetfeetype where feelevel =2 order by id";
		rs.execute(sql);
		while (rs.next()) {
			KMInfo kminfo = new KMInfo();
			String supid = Util.null2String(rs.getString("id"));
			kminfo.setId(supid);
			kminfo.setCodename(Util.null2String(rs.getString("codeName")));
			kminfo.setName(Util.null2String(rs.getString("name")));
			kminfo.setLevel(Util.null2String(rs.getString("feelevel")));
			LinkedList<KMInfo> list = new LinkedList<KMInfo>();
			String sanjisql = "select id,codeName,name,feelevel from fnabudgetfeetype where feelevel =3 and supsubject = '"
					+ supid + "' order by id";
			rs1.execute(sanjisql);
			while (rs1.next()) {
				String id = Util.null2String(rs1.getString("id"));
				String codeName = Util.null2String(rs1.getString("codeName"));
				String name = Util.null2String(rs1.getString("name"));
				String feelevel = Util.null2String(rs1.getString("feelevel"));
				KMInfo kminfo1 = new KMInfo();
				kminfo1.setId(id);
				kminfo1.setCodename(codeName);
				kminfo1.setName(name);
				kminfo1.setLevel(feelevel);
				list.add(kminfo1);
			}
			kminfo.setList(list);
			kmLinkedList.add(kminfo);
		}
		return kmLinkedList;
	}

	public static void main(String[] args) {
		String cbzx = "2";
		String[] cbzxs = cbzx.split(",");
		String sql = "select sum(b.budgetaccount) count from fnabudgetinfo a , fnabudgetinfodetail b,fnabudgetfeetype c "
				+ "where a.id = b.budgetinfoid and a.status = 1 and c.id = b.budgettypeid and c.id = '121' "
				+ "and a.budgetperiods = '2' and b.budgetperiodslist = 1 and (";
		for (int i = 0; i < cbzxs.length; i++) {
			sql += " a.budgetorganizationid = " + cbzxs[i] + " ";
			if (i < cbzxs.length - 1) {
				sql += " or ";
			}
		}
		sql += ") ";
		System.out.println(sql);
	}

	/**
	 * 获取数据
	 * 
	 * @param kmLinkedList
	 * @param ddLinkedHashMap
	 * @param nd
	 * @param cbzx
	 * @return
	 */
	public LinkedHashMap<KMInfo, LinkedList<Object>> getDataBySearchConditions(LinkedList<KMInfo> kmLinkedList,
			String nd, String cbzx) {

		LinkedHashMap<KMInfo, LinkedList<Object>> resultmap = new LinkedHashMap<KMInfo, LinkedList<Object>>();
		DecimalFormat df = new DecimalFormat("######0.00");

		for (KMInfo kminfo : kmLinkedList) {
			LinkedList<Object> list = new LinkedList<Object>();

			String kmCode = kminfo.getCodename();
			String kmid = kminfo.getId();
			String kmname = kminfo.getName();
			list.add(kmname);
			list.add(kmCode);
			double ysTotal = 0;
			double fsTotal = 0;
			for (int i = 1; i < 13; i++) {
				String ys = getYSSByConditions(kmid, nd, i, cbzx);
				String fs = getFSSByConditions(kmid, nd, i, cbzx);
				ys = ys.equals("") ? "0" : ys;
				fs = fs.equals("") ? "0" : fs;
				String yss = df.format(Double.valueOf(ys));
				String fss = df.format(Double.valueOf(fs));
				ysTotal += Double.valueOf(yss);
				fsTotal += Double.valueOf(fss);
				list.add(yss);
				list.add(fss);
			}
			list.add(df.format(ysTotal));
			list.add(df.format(fsTotal));
			kminfo.setFsTotal(fsTotal);
			kminfo.setYsTotal(ysTotal);
			resultmap.put(kminfo, list);

			// 循环三级科目
			LinkedList<KMInfo> sanji = kminfo.getList();
			for (int j = 0; j < sanji.size(); j++) {
				KMInfo kmi = sanji.get(j);
				LinkedList<Object> list1 = new LinkedList<Object>();
				String id = kmi.getId();
				String name = kmi.getName();
				String codename = kmi.getCodename();
				list1.add(name);
				list1.add(codename);

				double ysTotal1 = 0;
				double fsTotal1 = 0;

				for (int i = 1; i < 13; i++) {
					String ys = getSJYSSByConditions(id, nd, i, cbzx);
					String fs = getFSSByConditions(id, nd, i, cbzx);
					ys = ys.equals("") ? "0" : ys;
					fs = fs.equals("") ? "0" : fs;
					String yss = df.format(Double.valueOf(ys));
					String fss = df.format(Double.valueOf(fs));
					ysTotal1 += Double.valueOf(yss);
					fsTotal1 += Double.valueOf(fss);
					list1.add(yss);
					list1.add(fss);

				}
				list1.add(df.format(ysTotal1));
				list1.add(df.format(fsTotal1));
				kmi.setFsTotal(ysTotal1);
				kmi.setYsTotal(fsTotal1);
				resultmap.put(kmi, list1);
			}
		}
		return resultmap;
	}

	/**
	 * 获取二级预算数
	 * 
	 * @param kmid
	 * @param level
	 * @param nd
	 * @param cbzx
	 * @return
	 */
	public String getYSSByConditions(String kmid, String nd, int yf, String cbzx) {
		RecordSet rs = new RecordSet();
		String getNdIdSQL = "select id from fnayearsperiods where fnayear = '" + nd + "'";
		rs.execute(getNdIdSQL);
		rs.next();
		String ndid = Util.null2String(rs.getString("id"));
		String[] cbzxs = cbzx.split(",");

		String sql = "select sum(b.budgetaccount) count from fnabudgetinfo a , fnabudgetinfodetail b,fnabudgetfeetype c "
				+ "where a.id = b.budgetinfoid and a.status = 1 and c.id = b.budgettypeid and c.supsubject = '" + kmid
				+ "' " + "and a.budgetperiods = '" + ndid + "' and b.budgetperiodslist = " + yf + " and (";
		for (int i = 0; i < cbzxs.length; i++) {
			sql += " a.budgetorganizationid = " + cbzxs[i] + " ";
			if (i < cbzxs.length - 1) {
				sql += " or ";
			}
		}
		sql += ") ";
		rs.execute(sql);
		rs.next();
		String count = Util.null2String(rs.getString("count"));
		return count;
	}

	/**
	 * 获取三级预算数
	 * 
	 * @param kmid
	 * @param level
	 * @param nd
	 * @param cbzx
	 * @return
	 */
	public String getSJYSSByConditions(String kmid, String nd, int yf, String cbzx) {
		RecordSet rs = new RecordSet();
		String getNdIdSQL = "select id from fnayearsperiods where fnayear = '" + nd + "'";
		rs.execute(getNdIdSQL);
		rs.next();
		String ndid = Util.null2String(rs.getString("id"));
		String[] cbzxs = cbzx.split(",");
		String sql = "select sum(b.budgetaccount) count from fnabudgetinfo a , fnabudgetinfodetail b,fnabudgetfeetype c "
				+ "where a.id = b.budgetinfoid and a.status = 1 and c.id = b.budgettypeid and c.id = '" + kmid + "' "
				+ "and a.budgetperiods = '" + ndid + "' and b.budgetperiodslist = " + yf + " and (";
		for (int i = 0; i < cbzxs.length; i++) {
			sql += " a.budgetorganizationid = " + cbzxs[i] + " ";
			if (i < cbzxs.length - 1) {
				sql += " or ";
			}
		}
		sql += ") ";
		rs.execute(sql);
		rs.next();
		String count = Util.null2String(rs.getString("count"));
		return count;
	}

	/**
	 * 获取二级发生数
	 * 
	 * @param kmid
	 * @param level
	 * @param nd
	 * @param cbzx
	 * @return
	 */
	public String getFSSByConditions(String kmid, String nd, int yf, String cbzx) {
		List<String> kms = getAllKmbyKmid(new ArrayList<String>(), kmid);
		StringBuffer sb = new StringBuffer(",");
		for (String s : kms) {
			sb.append("'");
			sb.append(s);
			sb.append("'");
			sb.append(",");
		}
		String kmss = sb.toString();
		String yfString = yf < 10 ? "0" + yf : String.valueOf(yf);
		String enddate = nd + "-" + yfString + "-31";
		String begindate = nd + "-" + yfString + "-01";
		String sql = "select sum(amount) count from fnaexpenseinfo where occurdate >= '" + begindate
				+ "' and occurdate <= '" + enddate + "' and (";
		String[] cbzxs = cbzx.split(",");
		for (int i = 0; i < cbzxs.length; i++) {
			sql += " organizationid = " + cbzxs[i] + " ";
			if (i < cbzxs.length - 1) {
				sql += " or ";
			}
		}
		sql += ") ";
		if (!",".equals(kmss)) {
			kmss = kmss.substring(1, kmss.length() - 1);
			sql += " and subject in ('" + kmid + "'," + kmss + ")";
		} else {
			sql += " and subject in ('" + kmid + "')";
		}
		RecordSet rs = new RecordSet();
		rs.execute(sql);
		rs.next();
		String count = Util.null2String(rs.getString("count"));
		return count;
	}

	/**
	 * 递归查询下级所有科目
	 * 
	 * @param paramArrayList
	 * @param paramString
	 * @return
	 */
	public List<String> getAllKmbyKmid(List<String> paramArrayList, String paramString) {
		RecordSet localRecordSet = new RecordSet();
		localRecordSet.executeSql("select id from fnabudgetfeetype where supsubject=" + paramString);
		while (localRecordSet.next()) {
			String str = Util.null2String(localRecordSet.getString(1));
			if ("".equals(str)) {
				continue;
			}
			paramArrayList.add(str);
			getAllKmbyKmid(paramArrayList, str);
		}
		return paramArrayList;
	}
}
