package weaver.interfaces.jiangyl.sync.model;

public class HrmresourceModel {

	/**
	 * 人员ID
	 */
	private String hrmresourceId;

	/**
	 * 人员姓名
	 */
	private String lastname;

	/**
	 * 状态
	 */
	private String status;

	/**
	 * 性别
	 */
	private String sex;

	/**
	 * 登录账号
	 */
	private String loginid;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 部门编码
	 */
	private String departmentCode;

	/**
	 * 分部编码
	 */
	private String subcompanyCode;

	/**
	 * 账号类型
	 */
	private String accounttype;

	/**
	 * 电子邮件地址
	 */
	private String email;

	/**
	 * 上级人员ID
	 */
	private String managerID = "0";

	/**
	 * 移动电话
	 */
	private String mobile;

	public String getHrmresourceId() {
		return hrmresourceId;
	}

	public void setHrmresourceId(String hrmresourceId) {
		this.hrmresourceId = hrmresourceId;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getLoginid() {
		return loginid;
	}

	public void setLoginid(String loginid) {
		this.loginid = loginid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDepartmentCode() {
		return departmentCode;
	}

	public void setDepartmentCode(String departmentCode) {
		this.departmentCode = departmentCode;
	}

	public String getSubcompanyCode() {
		return subcompanyCode;
	}

	public void setSubcompanyCode(String subcompanyCode) {
		this.subcompanyCode = subcompanyCode;
	}

	public String getAccounttype() {
		return accounttype;
	}

	public void setAccounttype(String accounttype) {
		this.accounttype = accounttype;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getManagerID() {
		return managerID;
	}

	public void setManagerID(String managerID) {
		this.managerID = managerID;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public HrmresourceModel(String hrmresourceId, String lastname,
			String status, String sex, String loginid, String password,
			String departmentCode, String subcompanyCode, String accounttype,
			String email, String managerID, String mobile) {
		super();
		this.hrmresourceId = hrmresourceId;
		this.lastname = lastname;
		this.status = status;
		this.sex = sex;
		this.loginid = loginid;
		this.password = password;
		this.departmentCode = departmentCode;
		this.subcompanyCode = subcompanyCode;
		this.accounttype = accounttype;
		this.email = email;
		this.managerID = managerID;
		this.mobile = mobile;
	}

	public HrmresourceModel() {
	}
}
