package weaver.interfaces.jiangyl.doc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import weaver.conn.RecordSet;
import weaver.docs.category.SecCategoryComInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

public class WriteDocToDisk extends BaseCronJob {

	private static final String FILESTOREPATH = "D:/temp/";
	
	@Override
	public void execute() {
		// 条件：
		// 开始日期-结束日期
		// 文档目录
		String beginDate = "";
		String endDate = "";
		String docDirID = "";
		
		exec(beginDate, endDate, docDirID);
	}

	/**
	 * 读取zip内容，并把内容写入到其他文件中
	 * @param bean
	 * @throws IOException
	 */
	public static void write(DocBean bean) throws IOException {
		File fil = new File(bean.getFILEREALPATH());
		ZipInputStream zipIn = null;
		try {
			zipIn = new ZipInputStream(new FileInputStream(fil));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		ZipEntry zipEn = null;
		ZipFile zfil = null;
		try {
			zfil = new ZipFile(bean.getFILEREALPATH());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			while ((zipEn = zipIn.getNextEntry()) != null) {
				if (!zipEn.isDirectory()) {
					FileUtils.copyInputStreamToFile(zfil.getInputStream(zipEn), new File(bean.getFILESTOREPATH()));
				}
				zipIn.closeEntry();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				zfil.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	public String getDirs(String seccategory) throws Exception {
		String docsecname = new SecCategoryComInfo().getAllParentName("" + seccategory, true);
		docsecname = Util.replace(docsecname, "&amp;quot;", "\"", 0);
		docsecname = Util.replace(docsecname, "&quot;", "\"", 0);
		docsecname = Util.replace(docsecname, "&lt;", "<", 0);
		docsecname = Util.replace(docsecname, "&gt;", ">", 0);
		docsecname = Util.replace(docsecname, "&apos;", "'", 0);
		return docsecname;
	}

	public static void main(String[] args) {
		DocBean bean = new DocBean();
		bean.setFILEMONTH("11");
		bean.setFILENAME("录用通知书（案场客服-邹娟）.doc");
		bean.setFILEREALPATH(
				"/Users/wangshanshan/Documents/toshiba/开发项目/工作项目/绿城/project/显示顺序/9239b8b2-5147-4686-8f6a-4d4f6f0558d9.zip");
		bean.setFILESTOREPATH("/Users/wangshanshan/Documents/toshiba/开发项目/工作项目/绿城/project/显示顺序/录用通知书（案场客服-邹娟）.doc");
		bean.setFILEYEAR("2017");
		try {
			write(bean);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String exec(String beginDate, String endDate, String docDirID) {
		RecordSet rs = new RecordSet();
		BaseBean bean = new BaseBean();
		int count = 0;
		// 过程：
		// 根据搜索条件查询到符合条件的数据 sql
		String sql = "select b.imagefilename,b.filerealpath,b.filesize,b.aescode,c.seccategory,c.doccreatedate,c.id,c.docsubject "
				+ "from DocImageFile a,imagefile b, docdetail c where a.docid = c.id and a.imagefileid = b.imagefileid and a.docid in (select id from docdetail where 1=1 ";
		if (!"".equals(beginDate)) {
			sql += " and doccreatedate >= '" + beginDate + "' ";
		}
		if (!"".equals(endDate)) {
			sql += " and doccreatedate <= '" + endDate + "' ";
		}
		if (!"".equals(docDirID)) {
			String[] ids = docDirID.split("_");
			docDirID = ids[ids.length - 1];
			List<String> paramArrayList = new ArrayList<String>();
			paramArrayList.add(docDirID);
			paramArrayList = getNextDirID(paramArrayList, docDirID);
			StringBuffer sb = new StringBuffer(",");
			for(int i = 0; i < paramArrayList.size(); i++) {
				sb.append("'" + paramArrayList.get(i) + "'");
				sb.append(",");
			}
			String all = sb.toString();
			all = all.substring(1,all.length()-1);
			sql += " and seccategory in (" + all + ") ";
		} else {
			String allDirID = getAllDirID();
			if(null != allDirID) {
				sql += " and seccategory in (" + allDirID + ") ";
			}
		}
		sql += ")";
		
		bean.writeLog("获取文档路径SQL：" + sql);
		rs.execute(sql);
		List<DocBean> list = new ArrayList<DocBean>();
		while (rs.next()) {
			String seccategory = Util.null2String(rs.getString("seccategory"));
			String imagefilename = Util.null2String(rs.getString("imagefilename"));
			String filerealpath = Util.null2String(rs.getString("filerealpath"));
			String doccreatedate = Util.null2String(rs.getString("doccreatedate"));
			String docsubject = Util.null2String(rs.getString("docsubject"));
			try {
				String dirname = getDirs(seccategory);
				String fileYear = "";
				String fileMonth = "";
				if ("".equals(doccreatedate)) {
					bean.writeLog("文档：" + docsubject + " 没有创建日期");
					continue;
				}
				fileYear = doccreatedate.split("-")[0];
				fileMonth = doccreatedate.split("-")[1];
				DocBean docBean = new DocBean();
				docBean.setFILEMONTH(fileMonth);
				docBean.setFILENAME(imagefilename);
				docBean.setFILEREALPATH(filerealpath);
				docBean.setFILESTOREPATH(FILESTOREPATH + dirname + File.separator + imagefilename);
				docBean.setFILEYEAR(fileYear);
				list.add(docBean);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for(int i = 0; i < list.size(); i++) {
			try {
				write(list.get(i));
				count++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return String.valueOf(count);
	}
	
	public String getAllDirID() {
		StringBuffer sb = new StringBuffer(",");
		String sql = "select * from DocSecCategory";
		RecordSet rs = new RecordSet();
		rs.execute(sql);
		while(rs.next()) {
			String id = Util.null2String(rs.getString("id"));
			sb.append(id);
			sb.append(",");
		}
		String allIDS = sb.toString();
		if (",".equals(allIDS)) {
			return null;
		}
		return allIDS.substring(1,allIDS.length()-1);
	}
	
	/**
	 * 递归查询下级所有目录
	 * 
	 * @param paramArrayList
	 * @param paramString
	 * @return
	 */
	public List<String> getNextDirID(List<String> paramArrayList, String paramString) {
		RecordSet localRecordSet = new RecordSet();
		localRecordSet.executeSql("select id from DOCSECCATEGORY where PARENTID = '" + paramString + "'");
		while (localRecordSet.next()) {
			String str = Util.null2String(localRecordSet.getString(1));
			if ("".equals(str)) {
				continue;
			}
			paramArrayList.add(str);
			getNextDirID(paramArrayList, str);
		}
		return paramArrayList;
	}
}
