package weaver.interfaces.jiangyl.doc;

import weaver.conn.ConnStatement;
import weaver.conn.RecordSet;
import weaver.docs.category.CategoryManager;
import weaver.docs.category.DocTreelistComInfo;
import weaver.docs.category.MainCategoryComInfo;
import weaver.docs.category.SecCategoryComInfo;
import weaver.docs.category.SecCategoryCustomSearchComInfo;
import weaver.docs.category.SecCategoryDocPropertiesComInfo;
import weaver.docs.category.SecCategoryManager;
import weaver.docs.category.SubCategoryComInfo;
import weaver.docs.docs.SecShareableCominfo;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;
import weaver.systeminfo.SysMaintenanceLog;

public class CreateDocDictionaryAction implements Action {

	@Override
	public String execute(RequestInfo paramRequestInfo) {
		String parentid = "";
		String mlname = "";
		User user = paramRequestInfo.getRequestManager().getUser();
		String ip = paramRequestInfo.getRequestManager().getIp();
		Property[] properties = paramRequestInfo.getMainTableInfo().getProperty();// 获取表单主字段信息
		for (int i = 0; i < properties.length; i++) {
			String name = properties[i].getName();// 主字段名称
			String value = Util.null2String(properties[i].getValue());// 主字段对应的值
			if (name.equals("parentid")) {
				parentid = value;
			}
			if (name.equals("mlmc")) {
				mlname = value;
			}
		}
		if ("".equals(parentid)) {
			paramRequestInfo.getRequestManager().setMessageid("Failed");
			paramRequestInfo.getRequestManager().setMessagecontent("创建文档目录失败：" + "上级目录不能为空");
			return SUCCESS;
		}
		String[] parentids = parentid.split("_");
		parentid = parentids[parentids.length-1];
		RecordSet rs = new RecordSet();
		String sql = "select id,CATEGORYNAME from DOCSECCATEGORY where id = '" + parentid + "' union "
				+ "select id,CATEGORYNAME from DOCMAINCATEGORY where id = '" + parentid + "'";
		rs.execute(sql);
		if (!rs.next()) {
			paramRequestInfo.getRequestManager().setMessageid("Failed");
			paramRequestInfo.getRequestManager().setMessagecontent("创建文档目录失败：" + "父文档目录ID：" + parentid + "不存在");
			return SUCCESS;
		}

		String message = "";
		try {
			message = create(user, parentid, ip, mlname);
			if (!message.equals("成功")) {
				paramRequestInfo.getRequestManager().setMessageid("Failed");
				paramRequestInfo.getRequestManager().setMessagecontent("创建文档目录失败：" + message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			paramRequestInfo.getRequestManager().setMessageid("Failed");
			paramRequestInfo.getRequestManager().setMessagecontent("创建文档目录失败：" + e.getMessage());
		}
		return SUCCESS;
	}

	public static String create(User user, String parentid, String ip, String categoryname) throws Exception {
		RecordSet RecordSet = new RecordSet();
		RecordSet RecordSet1 = new RecordSet();
		SecCategoryManager scm = new SecCategoryManager();
		SysMaintenanceLog log = new SysMaintenanceLog();
		String operation = "add";
		char flag = Util.getSeparator();
		int userid = user.getUID();
		CategoryManager cm = new CategoryManager();

		if (operation.equalsIgnoreCase("add")) {
			int secid = -1;
			String subcategoryid = "-1";
			String coder = "";
			String docmouldid = "";// TODO 确认
			if (docmouldid.equals(""))
				docmouldid = "0";

			/* added by wdl 2006.7.3 TD.4617 start */
			String wordmouldid = "";// TODO 确认
			if (wordmouldid.equals(""))
				wordmouldid = "0";
			/* added end */

			String publishable = "";// TODO 确认
			if (publishable.equals(""))
				publishable = "0";
			String replyable = "";// TODO 确认
			if (replyable.equals(""))
				replyable = "0";
			String shareable = "";
			if (shareable.equals(""))
				shareable = "0";

			String cusertype = "";
			String cuserseclevel = "";
			/* 下面这行代码2003年6月6日由谭小鹏注释掉 */
			// if(cuserseclevel.equals("")) shareable="0";
			String cdepartmentid1 = "";
			String cdepseclevel1 = "";
			String cdepartmentid2 = "";
			String cdepseclevel2 = "";
			String croleid1 = "";
			String crolelevel1 = "";
			String croleid2 = "";
			String crolelevel2 = "";
			String croleid3 = "";
			String crolelevel3 = "";
			String approvewfid = "";

			String hasaccessory = "";
			if (hasaccessory.equals(""))
				hasaccessory = "0";
			String accessorynum = "";
			String hasasset = "";
			if (hasasset == null || hasasset.isEmpty()) {
				hasasset = "1";// 默认值
			}
			String assetlabel = "";
			String hasitems = "";
			String itemlabel = "";
			String hashrmres = "";
			if (hashrmres == null || hashrmres.isEmpty()) {
				hashrmres = "1";// 默认值
			}
			String hrmreslabel = "";
			String hascrm = "";
			if (hascrm == null || hascrm.isEmpty()) {
				hascrm = "1";// 默认值
			}
			String crmlabel = "";
			String hasproject = "";
			if (hasproject == null || hasproject.isEmpty()) {
				hasproject = "1";// 默认值
			}
			String projectlabel = "";
			String hasfinance = "";
			String financelabel = "";

			// 增加是否此目录打分，以及是否匿名打分等字段
			int markable = 0;
			int markAnonymity = 0;
			int orderable = 0;
			int defaultLockedDoc = 0;
			int isSetShare = 0;

			int allownModiMShareL = 0;
			int allownModiMShareW = 0;
			int maxUploadFileSize = 0;

			if (maxUploadFileSize < 0) {
				maxUploadFileSize = 0;
			}

			int noDownload = 0;
			int noRepeatedName = 0;
			int isControledByDir = 0;
			int pubOperation = 0;
			int pushOperation = 0;
			if (pushOperation == 1) {
				// 推送方式
				StringBuffer pushwaystemp = new StringBuffer();
				String pushtoMobile = "";
				if ("1".equals(pushtoMobile)) {
					pushwaystemp.append("," + pushtoMobile);
				} else {
					pushwaystemp.append(",0");
				}
				String pushtoEmessage = "";
				if ("1".equals(pushtoEmessage)) {
					pushwaystemp.append("," + pushtoEmessage);
				} else {
					pushwaystemp.append(",0");
				}
				String pushtoEmail = "";
				if ("1".equals(pushtoEmail)) {
					pushwaystemp.append("," + pushtoEmail);
				} else {
					pushwaystemp.append(",0");
				}
				String pushtoMessage = "";
				if ("1".equals(pushtoMessage)) {
					pushwaystemp.append("," + pushtoMessage);
				} else {
					pushwaystemp.append(",0");
				}
			} else {
			}

			int childDocReadRemind = 0;

			String uploadExt = "";
			if ("".equals(uploadExt)) {
				uploadExt = "*.*";
			}

			String isLogControl = "";

			int readOpterCanPrint = 0;

			// TD2858 新的需求: 添加与文档创建人相关的默认共享

			float secorder = 0;// 目录顺序

			String isUseFTPOfSystem = "";// ecology系统使用FTP服务器设置功能 true:启用 false:不使用
			String isUseFTP = "";// 指定文档子目录是否启用FTP服务器设置
			int FTPConfigId = -1;// FTP服务器

			int subcompanyId = -1;

			if (Util.getIntValue(parentid) > 0) {
				subcompanyId = 0;
			}

			int level = 0;

			level = new SecCategoryComInfo().getLevel(parentid, true);

			if (operation.equalsIgnoreCase("add")) {
				String extendParentAttr = "";
				String checkSql = "select count(id) from DocSecCategory where categoryname = '" + categoryname + "'";
				if (Util.getIntValue(parentid) > 0) {
					checkSql = checkSql + " and parentid=" + parentid;
				} else {
					checkSql = checkSql + " and (parentid is null or parentid<=0) ";
				}
				RecordSet.executeSql(checkSql);
				if (RecordSet.next()) {
					if (RecordSet.getInt(1) > 0) {
						return "目录名称已经存在.";
					}
				}

				String ParaStr = subcategoryid + flag + categoryname + flag + docmouldid + flag + publishable + flag
						+ replyable + flag + shareable + flag + cusertype + flag + cuserseclevel + flag + cdepartmentid1
						+ flag + cdepseclevel1 + flag + cdepartmentid2 + flag + cdepseclevel2 + flag + croleid1 + flag
						+ crolelevel1 + flag + croleid2 + flag + crolelevel2 + flag + croleid3 + flag + crolelevel3
						+ flag + hasaccessory + flag + accessorynum + flag + hasasset + flag + assetlabel + flag
						+ hasitems + flag + itemlabel + flag + hashrmres + flag + hrmreslabel + flag + hascrm + flag
						+ crmlabel + flag + hasproject + flag + projectlabel + flag + hasfinance + flag + financelabel
						+ flag + approvewfid + flag + markable + flag + markAnonymity + flag + orderable + flag
						+ defaultLockedDoc + flag + "" + allownModiMShareL + flag + "" + allownModiMShareW + flag
						+ maxUploadFileSize + flag + wordmouldid + flag + isSetShare + flag + noDownload + flag
						+ noRepeatedName + flag + isControledByDir + flag + pubOperation + flag + childDocReadRemind
						+ flag + readOpterCanPrint + flag + isLogControl;
				if (extendParentAttr.equals("1")) {
					ParaStr = scm.copyAttrFromParent(ParaStr, parentid, categoryname, noRepeatedName);
				}

				ParaStr = ParaStr + flag + subcompanyId + flag + level + flag + parentid + flag + secorder;
				RecordSet.executeProc("Doc_SecCategory_Insert_New", ParaStr);

				if (!RecordSet.next()) {
					return "插入数据时失败";
				}
				int id = RecordSet.getInt(1);
				int newid = RecordSet.getInt(1);
				/* 是否允许订阅的处理 start */
				if (orderable == 1) {
					RecordSet1.executeSql("update docdetail set orderable='1' where seccategory = " + id);
				}
				if (extendParentAttr.equals("1")) {
				}
				RecordSet1.executeSql(
						"update DocSecCategory set secorder=" + secorder + ",coder='" + coder + "' where id = " + id);
				new SecShareableCominfo().addSecShareInfoCache("" + id);
				secid = newid;
				cm.addSecidToSuperiorSubCategory(newid);
				log.resetParameter();
				log.setRelatedId(newid);
				log.setRelatedName(categoryname);
				log.setOperateType("1");
				log.setOperateDesc("Doc_Sec_Insert_for_safe");
				log.setOperateItem("3");
				log.setOperateUserid(userid);
				log.setClientAddress(ip);
				log.setSysLogInfo();

				// TD2858 新的需求: 添加与文档创建人相关的默认共享 开始
				if (!extendParentAttr.equals("1")) {
					String strSqlInsert = "insert into DocSecCategoryShare (seccategoryid,sharetype,sharelevel,downloadlevel,operategroup)values("
							+ newid + ",1,3,1,1)";
					RecordSet.executeSql(strSqlInsert);
					strSqlInsert = "insert into DocSecCategoryShare (seccategoryid,sharetype,sharelevel,downloadlevel,operategroup)values("
							+ newid + ",2,1,1,1)";
					RecordSet.executeSql(strSqlInsert);
					strSqlInsert = "insert into DocSecCategoryShare (seccategoryid,sharetype,sharelevel,downloadlevel,operategroup)values("
							+ newid + ",1,3,1,2)";
					RecordSet.executeSql(strSqlInsert);
					strSqlInsert = "insert into DocSecCategoryShare (seccategoryid,sharetype,sharelevel,downloadlevel,operategroup)values("
							+ newid + ",2,1,1,2)";
					RecordSet.executeSql(strSqlInsert);

					// System.out.println(strSqlInsert);
					// TD2858 新的需求: 添加与文档创建人相关的默认共享 结束

					ConnStatement statement = new ConnStatement();
					try {

						// 更新FTP服务器设置信息
						String refreshChildren = "";
						if ("1".equals(isUseFTPOfSystem)) {
							String sql_FTPConfig = "insert into DocSecCatFTPConfig(secCategoryId,isUseFTP,FTPConfigId,refreshChildren) values(?,?,?,?)";
							statement.setStatementSql(sql_FTPConfig);
							statement.setInt(1, secid);
							statement.setString(2, isUseFTP);
							statement.setInt(3, FTPConfigId);
							statement.setString(4, refreshChildren);
							statement.executeUpdate();
						}

					} catch (Exception e) {
						throw e;
					} finally {
						try {
							statement.close();
						} catch (Exception ex) {
						}
					}
				}
				new MainCategoryComInfo().removeMainCategoryCache();
				new SubCategoryComInfo().removeMainCategoryCache();
				new SecCategoryComInfo().removeMainCategoryCache();
				new SecCategoryDocPropertiesComInfo().removeCache();
				new SecCategoryDocPropertiesComInfo().addDefaultDocProperties(secid);
				new SecCategoryCustomSearchComInfo().checkDefaultCustomSearch(secid);
				new DocTreelistComInfo().removeGetDocListInfordCache();
			}
		}
		return "成功";
	}
}
