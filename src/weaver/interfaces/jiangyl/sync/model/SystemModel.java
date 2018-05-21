package weaver.interfaces.jiangyl.sync.model;

public class SystemModel {

	/**
	 * 第三方系统接口地址
	 */
	private String systemURL;

	/**
	 * 第三方系统ID
	 */
	private String systemID;
	
	/**
	 * 第三方系统名称标识
	 */
	private String systemName;
	
	public String getSystemURL() {
		return systemURL;
	}

	public void setSystemURL(String systemURL) {
		this.systemURL = systemURL;
	}

	public String getSystemID() {
		return systemID;
	}

	public void setSystemID(String systemID) {
		this.systemID = systemID;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public SystemModel() {
	}

	public SystemModel(String systemURL, String systemID, String systemName) {
		super();
		this.systemURL = systemURL;
		this.systemID = systemID;
		this.systemName = systemName;
	}
	
}
