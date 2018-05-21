package weaver.interfaces.jiangyl.cw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import km.org.apache.poi.hssf.usermodel.HSSFCell;
import km.org.apache.poi.hssf.usermodel.HSSFCellStyle;
import km.org.apache.poi.hssf.usermodel.HSSFFont;
import km.org.apache.poi.hssf.usermodel.HSSFRow;
import km.org.apache.poi.hssf.usermodel.HSSFSheet;
import km.org.apache.poi.hssf.usermodel.HSSFWorkbook;
import km.org.apache.poi.hssf.util.CellRangeAddress;
import km.org.apache.poi.hssf.util.HSSFColor;
import weaver.conn.RecordSet;
import weaver.general.Util;

public class CWExport {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/**
		 * 获取从当前请求中的查询参数
		 * 
		 **/
		String year = Util.null2String(request.getParameter("nd"));
		String cbzx = Util.null2String(request.getParameter("cbzx"));

		/**
		 * 拼接当前需要导出的表格名称
		 * 
		 **/
		String title = year + "年预算发生情况";

		/**
		 * 设置第一行数据
		 * 
		 **/
		String[] headers = new String[28];
		headers[0] = "科目/科目编码";
		headers[1] = "";
		int mount = 1;
		for (int i = 2; i < headers.length - 2; i += 2) {
			if (i % 2 == 0) {
				headers[i] = mount + "月";
				mount++;
			}

		}
		headers[26] = "全年汇总";
		headers[27] = "全年汇总";

		/**
		 * 设置第二行数据
		 * 
		 **/
		String[] secondHeaders = new String[28];
		secondHeaders[0] = "";
		secondHeaders[1] = "";
		for (int i = 2; i < secondHeaders.length; i++) {
			if (i % 2 == 0) {
				secondHeaders[i] = "预算数";
			} else {
				secondHeaders[i] = "发生数";
			}
		}

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

		/**
		 * 生成Excel表格
		 * 
		 **/
		generateExcelFileForCW(headers, secondHeaders, ddLinkedHashMap, response, title);

		/**
		 * 下载生成好的Excel表格
		 * 
		 **/
		try {
			download("D://WEAVER//ecology//interface//jiangyl//cwexcel//" + title + ".xls", response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成Excel文件
	 * 
	 * @param headers
	 *            第一行科目/部门
	 * @param secondHeaders
	 *            第二行预算和付款
	 * @param yslist
	 *            邮箱预算科目
	 * @param o
	 *            邮箱预算科目对应的预算金额和银行付款金额
	 * @param orglist
	 *            获取所有部门
	 */
	public void generateExcelFileForCW(String[] headers, String[] secondHeaders,
			LinkedHashMap<KMInfo, LinkedList<Object>> ddLinkedHashMap, HttpServletResponse response, String title) {
		DecimalFormat df = new DecimalFormat("######0.00");
		OutputStream out = null;
		try {
			out = new FileOutputStream("D://WEAVER//ecology//interface//jiangyl//cwexcel//" + title + ".xls");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 生成一个表格
		HSSFSheet sheet = workbook.createSheet("预算与发生情况");
		// 设置表格默认列宽度为15个字节
		sheet.setDefaultColumnWidth((short) 20);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.WHITE.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.BLACK.index);
		font.setFontHeightInPoints((short) 12);
		font.setBoldweight(HSSFFont.DEFAULT_CHARSET);
		font.setFontName("微软雅黑");
		// 把字体应用到当前的样式
		style.setFont(font);

		// 生成并设置另一个样式
		HSSFCellStyle style2 = workbook.createCellStyle();
		style2.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		// 生成一个字体
		HSSFFont font2 = workbook.createFont();
		font2.setColor(HSSFColor.BLACK.index);
		font2.setFontHeightInPoints((short) 14);
		font2.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font2.setFontName("微软雅黑");
		// 把字体应用到当前的样式
		style2.setFont(font2);

		int total = headers.length;
		for (int k = 2; k < total; k += 2) {
			CellRangeAddress cellRange = new CellRangeAddress(0, 0, k, k + 1);
			sheet.addMergedRegion(cellRange);
		}

		CellRangeAddress cellRange = new CellRangeAddress(0, 1, 0, 1);
		sheet.addMergedRegion(cellRange);

		HSSFRow row0 = sheet.createRow(0);
		for (short i = 0; i < headers.length; i++) {
			HSSFCell cell0 = row0.createCell(i);
			cell0.setCellStyle(style2);
			cell0.setCellValue(headers[i]);
		}

		// 产生表格标题行
		HSSFRow row1 = sheet.createRow(1);
		for (short i = 0; i < secondHeaders.length; i++) {
			HSSFCell cell = row1.createCell(i);
			cell.setCellStyle(style2);
			cell.setCellValue(secondHeaders[i]);
		}

		int count = 0;
		for (Entry<KMInfo, LinkedList<Object>> entry : ddLinkedHashMap.entrySet()) {
			if (count == 0) {
				count += 2;
			} else {
				count += 1;
			}
			HSSFRow row2 = sheet.createRow(count);
			KMInfo key = entry.getKey();
			LinkedList<Object> list = entry.getValue();
			for (int n = 0; n < list.size(); n++) {
				HSSFCell cell0 = row2.createCell(n);
				String val = Util.null2String(list.get(n));
				val = val.equals("") ? "0.00" : val;
				if (key.getLevel().equals("2")) {
					cell0.setCellStyle(style2);
				} else {
					cell0.setCellStyle(style);
				}
				if (n > 1) {
					cell0.setCellValue(Util.getDoubleValue(val));
				} else {
					cell0.setCellValue(val);
				}
			}
		}
		try {
			workbook.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下载Excel文件
	 * 
	 * @param path
	 * @param response
	 */
	public void download(String path, HttpServletResponse response) {
		OutputStream toClient = null;
		File file = null;
		try {
			// path是指欲下载的文件的路径。
			file = new File(path);
			// 取得文件名。
			String filename = file.getName();
			// 以流的形式下载文件。
			InputStream fis = new BufferedInputStream(new FileInputStream(path));
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
			fis.close();
			// 清空response
			response.reset();
			// 设置response的Header
			response.setHeader("Content-disposition",
					"attachment; filename=" + new String(filename.getBytes("GB2312"), "ISO8859-1"));
			// 设定输出文件头
			response.setContentType("application/msexcel");
			toClient = new BufferedOutputStream(response.getOutputStream());
			toClient.write(buffer);
			toClient.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				toClient.close();
				toClient = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
