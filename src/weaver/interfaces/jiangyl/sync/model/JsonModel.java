package weaver.interfaces.jiangyl.sync.model;

import java.util.List;

public class JsonModel {

	private List<SubCompanyModel> subCompany;
	
	private List<HrmdepartmentModel> hrmDepartment;
	
	private List<HrmresourceModel> hrmResource;

	public List<SubCompanyModel> getSubCompany() {
		return subCompany;
	}

	public void setSubCompany(List<SubCompanyModel> subCompany) {
		this.subCompany = subCompany;
	}

	public List<HrmdepartmentModel> getHrmDepartment() {
		return hrmDepartment;
	}

	public void setHrmDepartment(List<HrmdepartmentModel> hrmDepartment) {
		this.hrmDepartment = hrmDepartment;
	}

	public List<HrmresourceModel> getHrmResource() {
		return hrmResource;
	}

	public void setHrmResource(List<HrmresourceModel> hrmResource) {
		this.hrmResource = hrmResource;
	}
	
	
	
	
}
