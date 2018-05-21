package weaver.interfaces.jiangyl.sync.model;

public class HrmdepartmentModel {

	/**
	 * 部门编码
	 */
	private String departmentCode;
	
	/**
	 * 部门名称
	 */
	private String departmentname;
	
	/**
	 * 上级部门编码
	 */
	private String supdepCode;
	
	/**
	 * 所属分部编码
	 */
	private String subcompanyCode;
	
	/**
	 * 是否封存，如果封存为1
	 */
	private String canceled;
	
	private String hrmdepartmentCategory;
	

	public String getHrmdepartmentCategory() {
		return hrmdepartmentCategory;
	}

	public void setHrmdepartmentCategory(String hrmdepartmentCategory) {
		this.hrmdepartmentCategory = hrmdepartmentCategory;
	}

	public String getDepartmentCode() {
		return departmentCode;
	}

	public void setDepartmentCode(String departmentCode) {
		this.departmentCode = departmentCode;
	}

	public String getDepartmentname() {
		return departmentname;
	}

	public void setDepartmentname(String departmentname) {
		this.departmentname = departmentname;
	}

	public String getSupdepCode() {
		return supdepCode;
	}

	public void setSupdepCode(String supdepCode) {
		this.supdepCode = supdepCode;
	}

	public String getSubcompanyCode() {
		return subcompanyCode;
	}

	public void setSubcompanyCode(String subcompanyCode) {
		this.subcompanyCode = subcompanyCode;
	}

	public String getCanceled() {
		return canceled;
	}

	public void setCanceled(String canceled) {
		this.canceled = canceled;
	}

	public HrmdepartmentModel(String departmentCode, String departmentname,
			String supdepCode, String subcompanyCode, String canceled,String hrmdepartmentCategory) {
		super();
		this.departmentCode = departmentCode;
		this.departmentname = departmentname;
		this.supdepCode = supdepCode;
		this.subcompanyCode = subcompanyCode;
		this.canceled = canceled;
		this.hrmdepartmentCategory = hrmdepartmentCategory;
	}
	
	public HrmdepartmentModel(){}
}
