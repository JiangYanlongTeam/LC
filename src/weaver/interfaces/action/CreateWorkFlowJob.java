package weaver.interfaces.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.resource.ResourceComInfo;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.workflow.webservices.WorkflowBaseInfo;
import weaver.workflow.webservices.WorkflowMainTableInfo;
import weaver.workflow.webservices.WorkflowRequestInfo;
import weaver.workflow.webservices.WorkflowRequestTableField;
import weaver.workflow.webservices.WorkflowRequestTableRecord;
import weaver.workflow.webservices.WorkflowService;
import weaver.workflow.webservices.WorkflowServiceImpl;

public class CreateWorkFlowJob extends BaseCronJob {

	/**
	 * CUS_FIELDDATA 中scopeid 固定值
	 */
	public static final String SCOPEID = "3";

	/**
	 * HRMRESOURCE 表中 过滤的状态值，如果改为多个需要把 = 0 改为 in ('xx','xx')
	 */
	public static final String STATUS_STR = " = 0 ";

	/**
	 * CUS_FIELDDATA 表中人员入职自定义字段
	 */
	public static final String CUS_FIELDDATA_FIELD = "field1";

	/**
	 * 相隔天数
	 */
	public static final String DAY = "45";

	/**
	 * 需要发起流程的WORKFLOWID
	 */
	public static final String WORKFLOWID = "65";

	/**
	 * 流程标题
	 */
	public static final String REQUESTNAME = "试用期评估";

	private BaseBean baseBean = new BaseBean();

	@Override
	public void execute() {
		Map<String, String> hrmMap = new HashMap<String, String>();
		RecordSet recordSet = new RecordSet();
		// 1. 获取人员
		String getHrmResourceSQL = "select b.id hrmid,a." + CUS_FIELDDATA_FIELD + " " + CUS_FIELDDATA_FIELD
				+ " from cus_fielddata a, hrmresource b " + "where a.id = b.id and a.scopeid = " + SCOPEID
				+ " and b.status " + STATUS_STR + " " + "and CONVERT(VARCHAR(10),dateadd(day, " + DAY + ", a."
				+ CUS_FIELDDATA_FIELD + "),110) = CONVERT(VARCHAR(10),getdate(),110)";
		baseBean.writeLog("获取满足入职人员" + DAY + "天后发起流程的人员信息SQL：" + getHrmResourceSQL);
		recordSet.execute(getHrmResourceSQL);
		while (recordSet.next()) {
			String hrmID = Util.null2String(recordSet.getString("hrmid"));
			String field = Util.null2String(recordSet.getString(CUS_FIELDDATA_FIELD));
			hrmMap.put(hrmID, field);
		}
		// 2. 发起流程
		if (hrmMap.isEmpty()) {
			return;
		}
		String pgrq = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		for (Entry<String, String> entry : hrmMap.entrySet()) {
			String hrmid = entry.getKey();
			String field = entry.getValue();
			String hrmName = "";
			String hrmDepartmentID = "";
			String subCompanyID = "";
			String jobTitleID = "";
			String managerID = "";
			try {
				managerID = new ResourceComInfo().getManagerID(hrmid);
				hrmName = new ResourceComInfo().getLastname(hrmid);
				hrmDepartmentID = new ResourceComInfo().getDepartmentID(hrmid);
				subCompanyID = new ResourceComInfo().getSubCompanyID(hrmid);
				jobTitleID = new ResourceComInfo().getJobTitle(hrmid);
			} catch (Exception e) {
				e.printStackTrace();
			}
			baseBean.writeLog("人员：" + hrmName + "上级ID: " + managerID + "");
			String reqid = generateWorkflow(hrmName, hrmid, field, hrmDepartmentID, subCompanyID, jobTitleID,pgrq,managerID);
			baseBean.writeLog("人员：" + hrmName + "创建流程: " + reqid + " 成功");
		}
	}

	/**
	 * 生成流程
	 * 
	 * @param hrmname
	 * @param hrmid
	 * @param field
	 * @param departmentid
	 * @param subcompanyid
	 * @param jobtitleid
	 * @return
	 */
	public String generateWorkflow(String hrmname, String hrmid, String field, String departmentid, String subcompanyid,
			String jobtitleid, String pgrq, String managerid) {
		WorkflowRequestTableField[] wrti = new WorkflowRequestTableField[7];
		wrti[0] = new WorkflowRequestTableField();
		wrti[0].setFieldName("xm");
		wrti[0].setFieldValue(hrmid);
		wrti[0].setView(true);
		wrti[0].setEdit(true);

		wrti[1] = new WorkflowRequestTableField();
		wrti[1].setFieldName("bm");
		wrti[1].setFieldValue(departmentid);
		wrti[1].setView(true);
		wrti[1].setEdit(true);

		wrti[2] = new WorkflowRequestTableField();
		wrti[2].setFieldName("gs");
		wrti[2].setFieldValue(subcompanyid);
		wrti[2].setView(true);
		wrti[2].setEdit(true);

		wrti[3] = new WorkflowRequestTableField();
		wrti[3].setFieldName("sygw");
		wrti[3].setFieldValue(jobtitleid);
		wrti[3].setView(true);
		wrti[3].setEdit(true);

		wrti[4] = new WorkflowRequestTableField();
		wrti[4].setFieldName("rsrq");
		wrti[4].setFieldValue(field);
		wrti[4].setView(true);
		wrti[4].setEdit(true);
		
		wrti[5] = new WorkflowRequestTableField();
		wrti[5].setFieldName("pgrq");
		wrti[5].setFieldValue(pgrq);
		wrti[5].setView(true);
		wrti[5].setEdit(true);
		
		wrti[6] = new WorkflowRequestTableField();
		wrti[6].setFieldName("pgr");
		wrti[6].setFieldValue(managerid);
		wrti[6].setView(true);
		wrti[6].setEdit(true);

		WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];
		wrtri[0] = new WorkflowRequestTableRecord();
		wrtri[0].setWorkflowRequestTableFields(wrti);

		WorkflowMainTableInfo wmi = new WorkflowMainTableInfo();
		wmi.setRequestRecords(wrtri);

		WorkflowBaseInfo wbi = new WorkflowBaseInfo();
		wbi.setWorkflowId(WORKFLOWID);

		WorkflowRequestInfo wri = new WorkflowRequestInfo();
		wri.setCreatorId(hrmid);
		wri.setRequestLevel("0");
		wri.setRequestName(REQUESTNAME + "(被评估人：" + hrmname + " 入职日期：" + field + ")");
		wri.setWorkflowMainTableInfo(wmi);
		wri.setWorkflowBaseInfo(wbi);

		WorkflowService WorkflowServicePortTypeProxy = new WorkflowServiceImpl();
		String reqid = WorkflowServicePortTypeProxy.doCreateWorkflowRequest(wri, Integer.parseInt(hrmid));
		baseBean.writeLog("返回REQUESTID：" + reqid);
		return reqid;
	}
}
