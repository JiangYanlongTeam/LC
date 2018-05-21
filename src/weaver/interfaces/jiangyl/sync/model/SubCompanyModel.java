package weaver.interfaces.jiangyl.sync.model;

public class SubCompanyModel {

	/**
	 * 编码
	 */
	private String subcompanyCode;

	/**
	 * 分部名称
	 */
	private String subcompanyname;

	/**
	 * 所属总部编码
	 */
	private String companyCode;

	/**
	 * 上级分部编码
	 */
	private String supsubcomCode;

	/**
	 * 分部类别
	 */
	private String subcompanyCategory;

	/**
	 * 封存
	 */
	private String canceled;

	public String getSubcompanyCode() {
		return subcompanyCode;
	}

	public void setSubcompanyCode(String subcompanyCode) {
		this.subcompanyCode = subcompanyCode;
	}

	public String getSubcompanyname() {
		return subcompanyname;
	}

	public void setSubcompanyname(String subcompanyname) {
		this.subcompanyname = subcompanyname;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getSupsubcomCode() {
		return supsubcomCode;
	}

	public void setSupsubcomCode(String supsubcomCode) {
		this.supsubcomCode = supsubcomCode;
	}

	public String getSubcompanyCategory() {
		return subcompanyCategory;
	}

	public void setSubcompanyCategory(String subcompanyCategory) {
		this.subcompanyCategory = subcompanyCategory;
	}

	public String getCanceled() {
		return canceled;
	}

	public void setCanceled(String canceled) {
		this.canceled = canceled;
	}

	public SubCompanyModel(String subcompanyCode, String subcompanyname,
			String companyCode, String supsubcomCode,
			String subcompanyCategory, String canceled) {
		super();
		this.subcompanyCode = subcompanyCode;
		this.subcompanyname = subcompanyname;
		this.companyCode = companyCode;
		this.supsubcomCode = supsubcomCode;
		this.subcompanyCategory = subcompanyCategory;
		this.canceled = canceled;
	}

	public SubCompanyModel() {
	}

}
