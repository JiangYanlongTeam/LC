package weaver.interfaces.jiangyl.jt;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import cn.com.weaver.services.webservices.BtLoginServicesPortTypeProxy;
import weaver.general.BaseBean;

public class JtService {

	public String execute(String loginid, String typeid) {
		BaseBean bean = new BaseBean();
		BtLoginServicesPortTypeProxy proxy = new BtLoginServicesPortTypeProxy();
		String token = "";
		try {
			bean.writeLog("传入参数loginid：" + loginid);
			String result = proxy.createSSOToken(loginid, "bluetown");
			bean.writeLog("返回XML：" + result);
			Map<String, String> map = readXML(result);
			bean.writeLog("解析返回结构MAP：" + map.toString());
			token = map.get("token");

			bean.writeLog("解析返回结构MAP中token：" + token);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (null == token || "".equals(token)) {
			return null;
		}
		if ("1".equals(typeid)) {// 1: 公文 2: 企业文化 3: 邮件 4: 通知公告 5: 待办流程 6: 邮件more 7: 通知公告more 8:待办流程more 9:公文more
									// 10:企业文化more
			return "http://portal.ibtcloud.cn/login/btsso.jsp?uid=" + loginid + "&token=" + token
					+ "&gopage=http://portal.ibtcloud.cn/homepage/Homepage.jsp%3Fhpid=25%26subCompanyId=48%26isfromportal=1%26isfromhp=0";
		}
		if ("2".equals(typeid)) {// 1: 公文 2: 企业文化 3: 邮件 4: 通知公告 5: 待办流程 6: 邮件more 7: 通知公告more 8:待办流程more 9:公文more
									// 10:企业文化more
			return "http://blueoa.ibtcloud.cn/wh/servlet/MainServer?cmd=login&source_sys=sso&login_dept_type=2&user="
					+ loginid + "&loginFlag=sso&forward_comp=showNews&ssotoken=" + token + "";
		}
		if ("3".equals(typeid)) {// 1: 公文 2: 企业文化 3: 邮件 4: 通知公告 5: 待办流程 6: 邮件more 7: 通知公告more 8:待办流程more 9:公文more
									// 10:企业文化more
			return "http://portal.ibtcloud.cn/login/btsso.jsp?uid=" + loginid + "&token=" + token
					+ "&gopage=http://portal.ibtcloud.cn/homepage/Homepage.jsp%3Fhpid=26%26subCompanyId=48%26isfromportal=1%26isfromhp=0";
		}
		if ("4".equals(typeid)) {// 1: 公文 2: 企业文化 3: 邮件 4: 通知公告 5: 待办流程 6: 邮件more 7: 通知公告more 8:待办流程more 9:公文more
									// 10:企业文化more
			return "http://blueoa.ibtcloud.cn/wh/servlet/MainServer?cmd=login&source_sys=sso&login_dept_type=2&user="
					+ loginid + "&loginFlag=sso&forward_comp=showNotice&ssotoken=" + token + "";
		}
		if ("5".equals(typeid)) {// 1: 公文 2: 企业文化 3: 邮件 4: 通知公告 5: 待办流程 6: 邮件more 7: 通知公告more 8:待办流程more 9:公文more
									// 10:企业文化more
			return "http://blueoa.ibtcloud.cn/wh/servlet/MainServer?cmd=login&source_sys=sso&login_dept_type=2&user="
					+ loginid + "&loginFlag=sso&forward_comp=showTasks&ssotoken=" + token + "";
		}
		if ("6".equals(typeid)) {// 1: 公文 2: 企业文化 3: 邮件 4: 通知公告 5: 待办流程 6: 邮件more 7: 通知公告more 8:待办流程more 9:公文more
									// 10:企业文化more
			return "http://portal.ibtcloud.cn/login/btsso.jsp?uid=" + loginid + "&token=" + token
					+ "&gopage=http://portal.ibtcloud.cn/homepage/Homepage.jsp%3Fhpid=27%26subCompanyId=48%26isfromportal=1%26isfromhp=0";
		}
		if ("7".equals(typeid)) {// 1: 公文 2: 企业文化 3: 邮件 4: 通知公告 5: 待办流程 6: 邮件more 7: 通知公告more 8:待办流程more 9:公文more
									// 10:企业文化more
			return "http://blueoa.ibtcloud.cn/wh/servlet/MainServer?cmd=login&source_sys=sso&login_dept_type=2&user="
					+ loginid + "&loginFlag=sso&forward_comp=showMoreNotice&ssotoken=" + token + "";
		}
		if ("8".equals(typeid)) {// 1: 公文 2: 企业文化 3: 邮件 4: 通知公告 5: 待办流程 6: 邮件more 7: 通知公告more 8:待办流程more 9:公文more
									// 10:企业文化more
			return "http://blueoa.ibtcloud.cn/wh/servlet/MainServer?cmd=login&source_sys=sso&login_dept_type=2&user="
					+ loginid + "&loginFlag=sso&forward_comp=showMoreTasks&ssotoken=" + token + "";
		}
		if ("9".equals(typeid)) {// 1: 公文 2: 企业文化 3: 邮件 4: 通知公告 5: 待办流程 6: 邮件more 7: 通知公告more 8:待办流程more 9:公文more
									// 10:企业文化more
			return "http://blueoa.ibtcloud.cn/wh/servlet/MainServer?cmd=login&source_sys=sso&login_dept_type=2&user="
					+ loginid + "&loginFlag=sso&forward_comp=showMoreDoc&ssotoken=" + token + "";
		}
		if ("10".equals(typeid)) {// 1: 公文 2: 企业文化 3: 邮件 4: 通知公告 5: 待办流程 6: 邮件more 7: 通知公告more 8:待办流程more 9:公文more
									// 10:企业文化more
			return "http://blueoa.ibtcloud.cn/wh/servlet/MainServer?cmd=login&source_sys=sso&login_dept_type=2&user="
					+ loginid + "&loginFlag=sso&forward_comp=showMoreNews&ssotoken=" + token + "";
		}
		return "http://portal.ibtcloud.cn/login/btsso.jsp?uid=" + loginid + "&token=" + token + "";
	}

	/**
	 * 解析集团返回参数
	 * 
	 * @param xml
	 * @return
	 */
	public static Map<String, String> readXML(String xml) {
		Map<String, String> map = new HashMap<String, String>();
		// 创建一个新的字符串
		StringReader reader = new StringReader(xml);
		InputSource source = new InputSource(reader);
		SAXBuilder sax = new SAXBuilder();
		try {
			Document doc = sax.build(source);
			Element root = doc.getRootElement();
			List<?> node = root.getChildren();
			Element el = null;
			for (int i = 0; i < node.size(); i++) {
				el = (Element) node.get(i);
				String nodename = el.getName();
				String nodetext = el.getValue();
				if ("returnCode".equals(nodename)) {
					map.put("returnCode", nodetext);
				}
				if ("returnMsg".equals(nodename)) {
					map.put("returnMsg", nodetext);
				}
				if ("token".equals(nodename)) {
					map.put("token", nodetext);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
}