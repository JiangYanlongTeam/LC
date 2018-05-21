package weaver.interfaces.jiangyl.gys;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.tempuri.MyProviderServiceSoapProxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.weaver.general.Util;

import weaver.general.BaseBean;
import weaver.general.MD5;
import weaver.interfaces.jiangyl.gys.model.JSONMode;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

public class GYSModifyAction extends BaseBean implements Action {
	@Override
	public String execute(RequestInfo request) {

		String ProviderCode = "";
		String lb = "";
		String mc = "";
		String Province = "";
		String Corporation = "";
		String Fax = "";
		String NationalTaxCode = "";
		String jyfw = "";
		String RegisterFund = "";
		String TaxpayerIdentificationNumber = "";
		String TaxpayerQualification = "";
		String BankAddress = "";
		String BankAccount = "";
		String BankName = "";
		String CorpQualification = "";
		String WorkADDress = "";
		String ProjectExperience = "";
		String FloatingFund = "";
		String EmployeeQty = "";
		String WorkerQty = "";
		String TechnicianQty = "";
		String YearSaleAmount = "";
		String Last5YearsProject = "";
		String CurrentProject = "";
		String EmployeeName = "";
		String JobPosition = "";
		String MobilePhone = "";
		String jbrxm = "";
		String jbrzw = "";
		String jbrsj = "";
		String Email = "";
		String xmjlxm = "";
		String xmjlzw = "";
		String xmjlsj = "";
		String dxxmal = "";
		
		int billid = Util.getIntValue(request.getRequestid()); // 数据ID
		String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
		String resource = "MY" + date + "OA";
		writeLog("发送明源转换之前的KEY：" + resource);
		String md5 = generateMd5(resource);
		writeLog("发送明源转换之后的KEY：" + md5);
		
		Property[] properties = request.getMainTableInfo().getProperty();// 获取表单主字段信息
		for (int i = 0; i < properties.length; i++) {
			String name = properties[i].getName();// 主字段名称
			String value = Util.null2String(properties[i].getValue());// 主字段对应的值
			if(name.equalsIgnoreCase("ProviderCode")){
				ProviderCode = value;
			}
			if(name.equalsIgnoreCase("leibie")){
				lb = value;
			}
			if(name.equalsIgnoreCase("mc")){
				mc = value;
			}
			if(name.equalsIgnoreCase("Province")){
				Province = value;
			}
			if(name.equalsIgnoreCase("Corporation")){
				Corporation = value;
			}
			if(name.equalsIgnoreCase("Fax")){
				Fax = value;
			}
			if(name.equalsIgnoreCase("NationalTaxCode")){
				NationalTaxCode = value;
			}
			if(name.equalsIgnoreCase("jyfw")){
				jyfw = value;
			}
			if(name.equalsIgnoreCase("RegisterFund")){
				RegisterFund = value;
			}
			if(name.equalsIgnoreCase("TaxpayerIdentificationNumber")){
				TaxpayerIdentificationNumber = value;
			}
			if(name.equalsIgnoreCase("TaxpayerQualification")){
				TaxpayerQualification = value;
			}
			if(name.equalsIgnoreCase("BankAddress")){
				BankAddress = value;
			}
			if(name.equalsIgnoreCase("BankAccount")){
				BankAccount = value;
			}
			if(name.equalsIgnoreCase("BankName")){
				BankName = value;
			}
			if(name.equalsIgnoreCase("CorpQualification")){
				CorpQualification = value;
			}
			if(name.equalsIgnoreCase("WorkADDress")){
				WorkADDress = value;
			}
			if(name.equalsIgnoreCase("ProjectExperience")){
				ProjectExperience = value;
			}
			if(name.equalsIgnoreCase("FloatingFund")){
				FloatingFund = value;
			}
			if(name.equalsIgnoreCase("EmployeeQty")){
				EmployeeQty = value;
			}
			if(name.equalsIgnoreCase("WorkerQty")){
				WorkerQty = value;
			}
			if(name.equalsIgnoreCase("TechnicianQty")){
				TechnicianQty = value;
			}
			if(name.equalsIgnoreCase("YearSaleAmount")){
				YearSaleAmount = value;
			}
			if(name.equalsIgnoreCase("Last5YearsProject")){
				Last5YearsProject = value;
			}
			if(name.equalsIgnoreCase("CurrentProject")){
				CurrentProject = value;
			}
			if(name.equalsIgnoreCase("EmployeeName")){
				EmployeeName = value;
			}
			if(name.equalsIgnoreCase("JobPosition")){
				JobPosition = value;
			}
			if(name.equalsIgnoreCase("MobilePhone")){
				MobilePhone = value;
			}
			if(name.equalsIgnoreCase("jbrxm")){
				jbrxm = value;
			}
			if(name.equalsIgnoreCase("jbrzw")){
				jbrzw = value;
			}
			if(name.equalsIgnoreCase("jbrsj")){
				jbrsj = value;
			}
			if(name.equalsIgnoreCase("Email")){
				Email = value;
			}
			if(name.equalsIgnoreCase("xmjlxm")){
				xmjlxm = value;
			}
			if(name.equalsIgnoreCase("xmjlzw")){
				xmjlzw = value;
			}
			if(name.equalsIgnoreCase("xmjlsj")){
				xmjlsj = value;
			}
			if(name.equalsIgnoreCase("dxxmal")){
				dxxmal = value;
			}
		}
		
		
		JSONMode mode = new JSONMode();
		mode.setProviderCode(ProviderCode);
		mode.setLeibie(lb);
		mode.setMc(mc);
		mode.setProvince(Province);
		mode.setCorporation(Corporation);
		mode.setFax(Fax);
		mode.setNationalTaxCode(NationalTaxCode);
		mode.setJyfw(jyfw);
		mode.setRegisterFund(RegisterFund);
		mode.setTaxpayerIdentificationNumber(TaxpayerIdentificationNumber);
		mode.setTaxpayerQualification(TaxpayerQualification);
		mode.setBankAddress(BankAddress);
		mode.setBankAccount(BankAccount);
		mode.setBankName(BankName);
		mode.setCorpQualification(CorpQualification);
		mode.setWorkADDress(WorkADDress);
		mode.setProjectExperience(ProjectExperience);
		mode.setFloatingFund(FloatingFund);
		mode.setEmployeeQty(EmployeeQty);
		mode.setWorkerQty(WorkerQty);
		mode.setTechnicianQty(TechnicianQty);
		mode.setYearSaleAmount(YearSaleAmount);
		mode.setLast5YearsProject(Last5YearsProject);
		mode.setCurrentProject(CurrentProject);
		mode.setEmployeeName(EmployeeName);
		mode.setJobPosition(JobPosition);
		mode.setMobilePhone(MobilePhone);
		mode.setJbrxm(jbrxm);
		mode.setJbrzw(jbrzw);
		mode.setJbrsj(jbrsj);
		mode.setEmail(Email);
		mode.setXmjlxm(xmjlxm);
		mode.setXmjlzw(xmjlzw);
		mode.setXmjlsj(xmjlsj);
		mode.setDxxmal(dxxmal);
		
		String jsonstr = JSON.toJSON(mode).toString();
		writeLog("发送明源JSON：" + jsonstr);
		MyProviderServiceSoapProxy proxy = new MyProviderServiceSoapProxy();
		String result = "";
		String message = "";
		try {
			result = proxy.updateMyProvider(md5, jsonstr);
		} catch (RemoteException e) {
			message = e.getMessage();
			e.printStackTrace();
			writeLog("调用明源接口地址失败：" + e.getMessage());
			request.getRequestManager().setMessageid("" + billid);
			request.getRequestManager().setMessagecontent("调用明源接口地址失败：" + e.getMessage());
			return "调用明源接口地址失败：" + e.getMessage();
		}
		writeLog("调用明源接口返回消息：" + result);
		if ("".equals(result)) {
			request.getRequestManager().setMessageid("" + billid);
			request.getRequestManager().setMessagecontent("调用明源接口地址失败：" + message);
			return "调用明源接口地址失败：" + message;
		} else {
			JSONObject ss = (JSONObject) JSON.parse(result);
			Boolean r = (Boolean) ss.get("Result");
			String m = (String) ss.get("Message");
			writeLog("解析明源接口返回消息Result：" + r);
			writeLog("解析明源接口返回消息Message：" + m);
			if (!r) {
				request.getRequestManager().setMessageid("" + billid);
				request.getRequestManager().setMessagecontent("调用明源接口地址失败：" + m);
				return "调用明源接口地址失败：" + m;
			}
		}
		return SUCCESS;
	}

	private static String generateMd5(String resource) {
		MD5 m = new MD5();
		String mima = m.getMD5ofStr(resource);
		return mima.toLowerCase();
	}
}
