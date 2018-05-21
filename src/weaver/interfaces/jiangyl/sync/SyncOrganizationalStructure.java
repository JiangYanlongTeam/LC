package weaver.interfaces.jiangyl.sync;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONObject;

import com.sdicons.json.mapper.MapperException;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.MD5;
import weaver.general.Util;
import weaver.interfaces.jiangyl.sync.model.HrmdepartmentModel;
import weaver.interfaces.jiangyl.sync.model.HrmresourceModel;
import weaver.interfaces.jiangyl.sync.model.JsonModel;
import weaver.interfaces.jiangyl.sync.model.SubCompanyModel;
import weaver.interfaces.jiangyl.sync.model.SystemModel;
import weaver.interfaces.jiangyl.util.JsonUtil;
import weaver.interfaces.schedule.BaseCronJob;

public class SyncOrganizationalStructure extends BaseCronJob{

	public SyncOrganizationalStructure(){}
	
	private BaseBean bean = new BaseBean();
	
	public void execute() {
		deal1();
	}
	
	public void deal2(String hrmid){
		//获取第三方系统信息
		List<SystemModel> systemModelList = new ArrayList<SystemModel>();
		RecordSet rs = new RecordSet();
		String getSystemInfoSQL = "select systemname,systemaddr,id,'' clientKey,'' clientPwd  from formtable_main_152 where sftb = '0'";
		rs.execute(getSystemInfoSQL);
		while(rs.next()) {
			String systemname = rs.getString("systemname");
			String systemaddr = rs.getString("systemaddr");
			String systemid = rs.getString("id");
			SystemModel model = new SystemModel(systemaddr, systemid, systemname);
			systemModelList.add(model);
		}
		//获取分部信息
		List<String> subCompanyList = new ArrayList<String>();
		List<SubCompanyModel> subCompanyModelList = new ArrayList<SubCompanyModel>();
		List<String> hrmdepartmentList = new ArrayList<String>();
		List<HrmdepartmentModel> hrmdepartmentModelList = new ArrayList<HrmdepartmentModel>();
		List<String> hrmresourceList = new ArrayList<String>();
		List<HrmresourceModel> HrmresourceModelList = new ArrayList<HrmresourceModel>();
		String hrmresourceSQL = "select a.id,a.lastname,a.sex,a.accounttype,a.email,a.loginid,a.managerID,(select workcode from hrmresource where id = a.managerid) managercode,a.workcode,"+
				"a.mobile,a.password,a.status,(select departmentcode from hrmdepartment where id = a.departmentid) departmentcode,"+
				"(select subcompanycode from hrmsubcompany where id = a.subcompanyid1 and id != 27) subcompanycode,a.subcompanyid1 "+
				"from hrmresource a where a.id = '"+hrmid+"'";
		rs.execute(hrmresourceSQL);
		rs.next();
		String id = rs.getString("id");
		String workcode = rs.getString("workcode");
		String lastname = rs.getString("lastname");
		String sex = rs.getString("sex");
		String accounttype = rs.getString("accounttype");
		String email = rs.getString("email");
		String loginid = rs.getString("loginid");
		String managerID = rs.getString("managercode");
		if(null == managerID || "".equals(managerID)) {
			managerID = "0";
		}
		String mobile = rs.getString("mobile");
		String password = rs.getString("password");
		String status = rs.getString("status");
		String departmentcode = rs.getString("departmentcode");
		String subcompanycode = rs.getString("subcompanycode");
		
		
		String subcompanyid1 = rs.getString("subcompanyid1");
		
		boolean isR = false;
		boolean isRight = true;
		do {
			subcompanyid1 = dealSubCompany(subcompanyid1);
			if(subcompanyid1.equals("27")) {
				isR = true;
				isRight = false;
			} else if(null == subcompanyid1 || subcompanyid1.equals("0") || "".equals(subcompanyid1)){
				isRight = false;
			}
		} while (isRight);
			
		if(isR) {
		} else {
			HrmresourceModel model3 = new HrmresourceModel(workcode, lastname, status, sex,
					loginid, password, departmentcode,
					subcompanycode, accounttype, email, managerID, mobile);
			HrmresourceModelList.add(model3);
			hrmresourceList.add(id);
			JsonModel mode = new JsonModel();
			mode.setSubCompany(subCompanyModelList);
			mode.setHrmDepartment(hrmdepartmentModelList);
			mode.setHrmResource(HrmresourceModelList);
			String jsonstring;
			try {
				jsonstring = JsonUtil.objectToJsonStr(mode);
				exec(systemModelList,jsonstring,hrmresourceList,hrmdepartmentList,subCompanyList);
			} catch (MapperException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void deal1(){
		//获取第三方系统信息
		List<SystemModel> systemModelList = new ArrayList<SystemModel>();
		RecordSet rs = new RecordSet();
		String getSystemInfoSQL = "select systemname,systemaddr,id,'' clientKey,'' clientPwd  from formtable_main_152 where sftb = '0'";
		rs.execute(getSystemInfoSQL);
		while(rs.next()) {
			String systemname = rs.getString("systemname");
			String systemaddr = rs.getString("systemaddr");
			String systemid = rs.getString("id");
			SystemModel model = new SystemModel(systemaddr, systemid, systemname);
			systemModelList.add(model);
		}
		if(systemModelList.isEmpty()) {
			return;
		}
		//获取分部信息
		List<String> subCompanyList = new ArrayList<String>();
		List<SubCompanyModel> subCompanyModelList = new ArrayList<SubCompanyModel>();
		String subcompanySQL = "select a.id,a.subcompanycode,a.subcompanyname,b.subcompanyCategory,"
				+ "'LCDF001' companyCode,(select subcompanycode from HrmSubCompany where id = a.supsubcomid) supsubcomCode,"
				+"(select id from HrmSubCompany where id = a.supsubcomid) supcomid,"
				+"(case when a.canceled = '1' then '1' else '0' end) canceled "
				+"from HrmSubCompany a,HrmSubcompanyDefined b where a.id = b.subcomid and b.tbbs = '待同步' and a.id != 27 and a.supsubcomid != 27";
		rs.execute(subcompanySQL);
		while(rs.next()) {
			String subcompanyid = rs.getString("id");
			String companyCode = rs.getString("companyCode");
			String subcompanyname = rs.getString("subcompanyname");
			String subcompanyCategory = rs.getString("subcompanyCategory");
			String subcompanyCode = rs.getString("subcompanyCode");
			String supsubcomCode = rs.getString("supsubcomCode");
			if(null == supsubcomCode || "".equals(supsubcomCode)) {
				supsubcomCode = "0";
			}
			String canceled = rs.getString("canceled");
			String supcomid = rs.getString("supcomid");
			boolean isR = false;
			if(null != supcomid && !"".equals(supcomid)) {
				boolean isRight = true;
				do {
					supcomid = dealSubCompany(supcomid);
					if(supcomid.equals("27")) {
						isR = true;
						isRight = false;
					} else if(null == supcomid || supcomid.equals("0") || "".equals(supcomid)){
						isRight = false;
					}
				} while (isRight);
			}
			if(isR) {
				continue;
			}
			
			SubCompanyModel subCompanyModel = new SubCompanyModel(subcompanyCode, subcompanyname, companyCode, supsubcomCode, subcompanyCategory, canceled);
			subCompanyModelList.add(subCompanyModel);
			subCompanyList.add(subcompanyid);
		}
		//获取部门信息
		List<String> hrmdepartmentList = new ArrayList<String>();
		List<HrmdepartmentModel> hrmdepartmentModelList = new ArrayList<HrmdepartmentModel>();
		String hrmdepartmentSQL = "select a.id,a.departmentCode,a.departmentname,(select subcompanycode from HrmSubCompany where id = a.subcompanyid1 and id != 27) subcompanyCode,"+
									"(select id from HrmSubCompany where id = a.subcompanyid1) subcompanyid1,"+
									"(select departmentcode from HrmDepartment where id = a.supdepid) subdepCode,(case when a.canceled = '1' then '1' else '0' end) canceled,"+
									"b.hrmdepartmentCategory "+
									"from hrmdepartment a,HrmDepartmentDefined b where a.id = b.deptid and b.tbbs = '待同步'";
		rs.execute(hrmdepartmentSQL);
		while(rs.next()) {
			String hrmdepartmentId = rs.getString("id");
			String departmentCode = rs.getString("departmentCode");
			String departmentname = rs.getString("departmentname");
			String subcompanyCode = rs.getString("subcompanyCode");
			String supdepCode = rs.getString("subdepCode");
			if(null == supdepCode || "".equals(supdepCode)) {
				supdepCode = "0";
			}
			String canceled = rs.getString("canceled");
			String hrmdepartmentCategory = rs.getString("hrmdepartmentCategory");
			String subcompanyid1 = rs.getString("subcompanyid1");
			
			boolean isR = false;
			boolean isRight = true;
			do {
				subcompanyid1 = dealSubCompany(subcompanyid1);
				if(subcompanyid1.equals("27")) {
					isR = true;
					isRight = false;
				} else if(null == subcompanyid1 || subcompanyid1.equals("0") || "".equals(subcompanyid1)){
					isRight = false;
				}
			} while (isRight);
				
			if(isR) {
				continue;
			}
			
			
			HrmdepartmentModel model = new HrmdepartmentModel(departmentCode, departmentname, supdepCode, subcompanyCode, canceled, hrmdepartmentCategory);
			hrmdepartmentList.add(hrmdepartmentId);
			hrmdepartmentModelList.add(model);
		}
		//获取人员信息
		List<String> hrmresourceList = new ArrayList<String>();
		List<HrmresourceModel> hrmresourceModelList = new ArrayList<HrmresourceModel>();
		String hrmresourceSQL = "select a.id,a.lastname,a.sex,a.accounttype,a.email,a.loginid,a.managerID,(select workcode from hrmresource where id = a.managerid) managercode,a.workcode,"+
								"a.mobile,a.password,a.status,(select departmentcode from hrmdepartment where id = a.departmentid) departmentcode,"+
								"(select subcompanycode from hrmsubcompany where id = a.subcompanyid1 and id != 27) subcompanycode,subcompanyid1 "+
								"from hrmresource a, cus_fielddata b where a.id = b.id and b.field3 = '待同步'";
		rs.execute(hrmresourceSQL);
		while(rs.next()) {
			String hrmresourceId = rs.getString("id");
			String workcode = rs.getString("workcode");
			String lastname = rs.getString("lastname");
			String sex = rs.getString("sex");
			String accounttype = rs.getString("accounttype");
			String email = rs.getString("email");
			String loginid = rs.getString("loginid");
			String managerID = rs.getString("managercode");
			if(null == managerID || "".equals(managerID)) {
				managerID = "0";
			}
			String mobile = rs.getString("mobile");
			String password = rs.getString("password");
			String status = rs.getString("status");
			String departmentCode = rs.getString("departmentcode");
			String subcompanyCode = rs.getString("subcompanycode");
			
			String subcompanyid1 = rs.getString("subcompanyid1");
			
			boolean isR = false;
			boolean isRight = true;
			do {
				subcompanyid1 = dealSubCompany(subcompanyid1);
				if(subcompanyid1.equals("27")) {
					isR = true;
					isRight = false;
				} else if(null == subcompanyid1 || subcompanyid1.equals("0") || "".equals(subcompanyid1)){
					isRight = false;
				}
			} while (isRight);
				
			if(isR) {
				continue;
			}
			
			HrmresourceModel model = new HrmresourceModel(workcode, lastname, status, sex, loginid, password, departmentCode, subcompanyCode, accounttype, email, managerID, mobile);
			hrmresourceModelList.add(model);
			hrmresourceList.add(hrmresourceId);			
		}
		JsonModel mode = new JsonModel();
		mode.setSubCompany(subCompanyModelList);
		mode.setHrmDepartment(hrmdepartmentModelList);
		mode.setHrmResource(hrmresourceModelList);
		String jsonstring;
		try {
			jsonstring = JsonUtil.objectToJsonStr(mode);
			exec(systemModelList,jsonstring,hrmresourceList,hrmdepartmentList,subCompanyList);
		} catch (MapperException e) {
			e.printStackTrace();
		}
	}
	
	public String dealSubCompany(String id) {
		RecordSet rs = new RecordSet();
		String sql = "select supsubcomid from HrmSubCompany where id = '"+id+"'";
		rs.execute(sql);
		rs.next();
		String supsubcomid = Util.null2String(rs.getString("supsubcomid"));
		return supsubcomid;
	}
	
	/**
	 * 处理信息并推送到其他系统
	 * 
	 * @param authPath
	 * @param jsonData
	 * @param hrmids
	 * @param departmentids
	 * @param subcompanyids
	 */
	private void exec(List<SystemModel> systemModelList,String jsonData,List<String> hrmids,List<String> departmentids,List<String> subcompanyids) {
		BaseBean bean = new BaseBean();
		if(hrmids.isEmpty() && departmentids.isEmpty() && subcompanyids.isEmpty()) {
			return;
		}
		RecordSet rs = new RecordSet();
		boolean syncflag = true;
		for(SystemModel systemModel : systemModelList) {
			boolean status = true;
			int count = 0;
			do {
				HttpPost post = null;
			    try {
			        DefaultHttpClient httpClient = new DefaultHttpClient();
			        // 设置超时时间
			        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
			        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 2000);
			            
			        post = new HttpPost(systemModel.getSystemURL());
			        // 构造消息头
			        post.setHeader("Content-type", "application/json; charset=utf-8");
					String seq = generateSeq();
					String md5 = generateMd5("oa", seq, "B6HMcfi35hcuz3NK");
			        post.setHeader("clientKey", "oa");
			        post.setHeader("clientPwd", "B6HMcfi35hcuz3NK");
			        post.setHeader("seq", seq);
			        post.setHeader("digest", md5);
			                    
			        // 构建消息实体
			        StringEntity entity = new StringEntity(jsonData.toString(), Charset.forName("UTF-8"));
			        entity.setContentEncoding("UTF-8");
			        // 发送Json格式的数据请求
			        entity.setContentType("application/json");
			        post.setEntity(entity);
			            
			        HttpResponse response = httpClient.execute(post);
			        InputStream in = response.getEntity().getContent();
			        int code = response.getStatusLine().getStatusCode();
			        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
					String addr = systemModel.getSystemID();
			        if(code == 200) {
			        	insertRecordToLogTable(date,time,addr,"200","更新成功",jsonData);
			        	status = true;
			        	count = 0;
			        } else {
			        	String jsonResult = getResponseString(in);
						JSONObject jObj = new JSONObject(jsonResult);
						String reponseID = jObj.getString("code");
						String message = jObj.getString("desc");
			        	//写对应系统推送失败日志信息 (日期，时间，系统地址，返回信息，返回消息,推送消息)
						insertRecordToLogTable(date,time,addr,reponseID,message,jsonData);
						status = false;
						syncflag = false;
						count++;
			        }
			    } catch (Exception e) {
			        e.printStackTrace();
			        status = false;
					syncflag = false;
					count++;
			    }finally{
			        if(post != null){
			            try {
			                post.releaseConnection();
			                Thread.sleep(500);
			            } catch (InterruptedException e) {
			                e.printStackTrace();
			            }
			        }
			    }
			} while (!status && count < 3);
		}
		
		if(syncflag) {
			//处理同步人员信息
			for(String hrmid : hrmids) {
				String sql = "update cus_fielddata set field3 = '已同步' where id = '"+hrmid+"'";
				rs.execute(sql);
			}
			//处理同步分部信息
			for(String subid : subcompanyids) {
				String sql = "update HrmSubcompanyDefined set tbbs = '已同步' where subcomid = '"+subid+"'";
				rs.execute(sql);
			}
			//处理部门信息
			for(String depid : departmentids) {
				String sql = "update HrmDepartmentDefined set tbbs = '已同步' where deptid = '"+depid+"'";
				rs.execute(sql);
			}
		}
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
	private void insertRecordToLogTable(String date,String time,String addrid,String responseID,String message,String jsonData) {
		RecordSet rs = new RecordSet();
		String insertSQL = "insert into formtable_main_153 (rq,sj,addr,responseid,responsemessage,formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime) "
				+ "values ('"+date+"','"+time.substring(0, 5)+"','"+addrid+"','"+responseID+"','"+message+"','13','1','0','"+date+"','"+time+"')";
		rs.execute(insertSQL);
		String selectMaxIdSQL = "select max(id) id from formtable_main_153";
		rs.execute(selectMaxIdSQL);
		rs.next();
		String id = rs.getString("id");
		ModeRightInfo ModeRightInfo = new ModeRightInfo();
		ModeRightInfo.editModeDataShare(1, 13, Integer.parseInt(id));
	}
	
	
	private static byte[] read(InputStream inStream) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outputStream.write(buffer);
		}
		inStream.close();
		return outputStream.toByteArray();
	}

	private static String getResponseString(InputStream inStream)
			throws Exception {
		byte[] data = read(inStream);
		String objectstring = new String(data,"UTF-8");
		return objectstring;
	}

	private static String generateSeq() {
		SimpleDateFormat simple = new SimpleDateFormat("yyyyMMddHHmmss");
		String yyyyMMddHHmmss = simple.format(new Date());
		String random = String
				.valueOf((int) ((Math.random() * 9 + 1) * 100000));
		return yyyyMMddHHmmss + random;
	}

	private static String generateMd5(String clientKey, String seq,
			String clientPwd) {
		String resource = clientKey + seq + clientPwd;
		MD5 m = new MD5();
		String mima = m.getMD5ofStr(resource);
		return mima;
	}

	public static boolean httpPostWithJson(String jsonObj,String url){
	    boolean isSuccess = false;
	    HttpPost post = null;
	    try {
	        DefaultHttpClient httpClient = new DefaultHttpClient();

	        // 设置超时时间
	        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
	        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 2000);
	            
	        post = new HttpPost(url);
	        // 构造消息头
	        post.setHeader("Content-type", "application/json; charset=utf-8");
			String seq = generateSeq();
			String md5 = generateMd5("oa", seq, "B6HMcfi35hcuz3NK");
	        post.setHeader("clientKey", "oa");
	        post.setHeader("clientPwd", "B6HMcfi35hcuz3NK");
	        post.setHeader("seq", seq);
	        post.setHeader("digest", md5);
	                    
	        // 构建消息实体
	        StringEntity entity = new StringEntity(jsonObj.toString(), Charset.forName("UTF-8"));
	        entity.setContentEncoding("UTF-8");
	        // 发送Json格式的数据请求
	        entity.setContentType("application/json");
	        post.setEntity(entity);
	            
	        HttpResponse response = httpClient.execute(post);
	        InputStream in = response.getEntity().getContent();
	        String result = getResponseString(in);
	        // 检验返回码
	        int statusCode = response.getStatusLine().getStatusCode();
	    } catch (Exception e) {
	        e.printStackTrace();
	        isSuccess = false;
	    }finally{
	        if(post != null){
	            try {
	                post.releaseConnection();
	                Thread.sleep(500);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    return isSuccess;
	}
}
