package weaver.interfaces.jiangyl.task;

import java.util.ArrayList;

import weaver.conn.RecordSet;
import weaver.docs.docs.DocComInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.share.ShareinnerInfo;

public class ProjectDoc extends BaseBean {

	public String getDocName(String fj, String userid) {
		DocComInfo com = new DocComInfo();
		String docName = com.getMuliDocName2(fj);
		addAccesoryShare(fj, userid);
		return docName;
	}

	/**
	 * 项目附件添加共享
	 * 
	 * @param projectfiles
	 * @param hrmids02
	 */
	public void addAccesoryShare(String projectfiles, String hrmids02) {
		writeLog("fj:"+projectfiles);
		writeLog("userid:"+hrmids02);
		RecordSet rs = new RecordSet();
		ShareinnerInfo shareInfo = new ShareinnerInfo();
		// 文档
		String procPara = "";
		ArrayList docidList = Util.TokenizerString(projectfiles, ",");
		char flag = Util.getSeparator();
		for (int i = 0; i < docidList.size(); i++) {
			int docid = Util.getIntValue((String) docidList.get(i), 0);
			if (docid == 0) {
				continue;
			}
			String[] crmids = Util.TokenizerString2(hrmids02, ",");
			for (int cx = 0; cx < crmids.length; cx++) {
				int resourceid = Util.getIntValue((String) crmids[cx], 0);
				if (resourceid == 0) {
					continue;
				}
				procPara = "" + docid;
				procPara += flag + "1";
				procPara += flag + "0";
				procPara += flag + "0";
				procPara += flag + "1";
				procPara += flag + ("" + resourceid);
				procPara += flag + "0";
				procPara += flag + "0";
				procPara += flag + "0";
				procPara += flag + "0";
				procPara += flag + ("" + resourceid);// CRMID
				procPara += flag + "1";// @sharesource
				rs.executeProc("DocShare_FromDocSecCategoryI", procPara);
				shareInfo.AddShare(docid, 1, resourceid, 0, 1, 1, resourceid, "shareinnerdoc", 1);
				writeLog("fj:"+projectfiles);
				writeLog("userid:"+hrmids02);
			}
		}
	}
}
