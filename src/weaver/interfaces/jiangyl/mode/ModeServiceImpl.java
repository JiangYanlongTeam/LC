package weaver.interfaces.jiangyl.mode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.weaver.general.Util;

import weaver.conn.RecordSet;
import weaver.formmode.webservices.ModeDataServiceImpl;
import weaver.formmode.webservices.ModeDateService;
import weaver.general.BaseBean;

public class ModeServiceImpl extends BaseBean implements ModeService {

	@Override
	public String saveModeData(String data) {

		writeLog("接收参数：" + data);
		// 接收xml
		SAXBuilder localSAXBuilder = new SAXBuilder();
		Document localDocument;
		try {
			localDocument = localSAXBuilder.build(new ByteArrayInputStream(data.getBytes("UTF-8")));
			Element localElement1 = localDocument.getRootElement();

			Element localElement2 = localElement1.getChild("header");
			String str4 = localElement2.getChildText("userid");
			String str5 = localElement2.getChildText("modeid");

			StringBuffer sb = new StringBuffer();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ROOT><header><userid>" + str4 + "</userid><modeid>"
					+ str5
					+ "</modeid><id></id></header><search><condition /><right>Y</right></search><data id=\"\"><maintable>");

			if ("9".equals(str5)) { // 供应商
				Element localElement4 = localElement1.getChild("data").getChild("maintable");
				List localList1 = localElement4.getChildren("field");
				for (int i = 0; i < localList1.size(); ++i) {
					String str14 = Util.null2String(((Element) localList1.get(i)).getChildText("filedname"));
					String str16 = Util.null2String(((Element) localList1.get(i)).getChildText("filedvalue"));
					String str12 = Util.null2String(((Element) localList1.get(i)).getChildText("fileddbtype"))
							.toLowerCase();
					if ("ProviderCode".equalsIgnoreCase(str14) && getCode(str16, str5, "ProviderCode")) {
						return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<ROOT>" + "<return>" + "<id>null</id>"
								+ "<returnnode>-1</returnnode>" + "<returnmessage>供应商编码：" + str16 + "重复</returnmessage>"
								+ "</return>" + "</ROOT>";
					}
					sb.append(add(str14, str12, str16));
				}
				sb.append("</maintable></data></ROOT>");
				writeLog("转换后参数：" + sb.toString());
				ModeDateService service = new ModeDataServiceImpl();
				String xmlReturn = service.saveModeData(sb.toString());
				return xmlReturn;
			}

			if ("10".equals(str5)) { // 合同
				String xmmcvalue = "";
				String xmmcname = "";
				String xmmctype = "";
				String procode = "";
				String proname = "";
				String protype = "";
				Element localElement4 = localElement1.getChild("data").getChild("maintable");
				List localList1 = localElement4.getChildren("field");
				for (int i = 0; i < localList1.size(); ++i) {
					String str14 = Util.null2String(((Element) localList1.get(i)).getChildText("filedname"));
					String str16 = Util.null2String(((Element) localList1.get(i)).getChildText("filedvalue"));
					String str12 = Util.null2String(((Element) localList1.get(i)).getChildText("fileddbtype"))
							.toLowerCase();
					if ("ContractCode".equalsIgnoreCase(str14) && getCode(str16, str5, "ContractCode")) {
						return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<ROOT>" + "<return>" + "<id>null</id>"
								+ "<returnnode>-1</returnnode>" + "<returnmessage>合同编码：" + str16 + "重复</returnmessage>"
								+ "</return>" + "</ROOT>";
					}
					if ("xmmc".equalsIgnoreCase(str14)) {
						xmmcvalue = trunslate(str16);
						xmmcname = str14;
						xmmctype = str12;
					} else if ("ProviderCode".equalsIgnoreCase(str14)) {
						procode = trunslateGYS(str16, "9");
						proname = str14;
						protype = str12;
						sb.append(add(str14, str12, str16));
					} else {
						sb.append(add(str14, str12, str16));
					}
				}
				sb.append(add(xmmcname, xmmctype, xmmcvalue));
				sb.append(add("gf", protype, procode));
				sb.append("</maintable></data></ROOT>");
				writeLog("转换后参数：" + sb.toString());
				ModeDateService service = new ModeDataServiceImpl();
				String xmlReturn = service.saveModeData(sb.toString());
				return xmlReturn;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<ROOT>" + "<return>" + "<id>null</id>"
				+ "<returnnode>-1</returnnode>" + "<returnmessage>处理失败！</returnmessage>" + "</return>" + "</ROOT>";
	}

	public boolean getCode(String code, String modeid, String column) {
		RecordSet localRecordSet = new RecordSet();
		String str3 = new StringBuilder()
				.append("select w.tablename,w.id from modeinfo m,workflow_bill w where w.id=m.formid and m.id=")
				.append(modeid).toString();
		localRecordSet.executeSql(str3);
		localRecordSet.next();
		String str7 = Util.null2String(localRecordSet.getString("tablename"));

		String str4 = new StringBuilder().append("select * from " + str7 + " where " + column + " = '" + code + "'")
				.toString();
		localRecordSet.executeSql(str4);
		if (localRecordSet.next()) {
			return true;
		}
		return false;
	}

	public static String add(Object name, Object type, Object value) {
		String xml = "";
		xml += "<field>";
		xml += "<filedname>" + name + "</filedname>";// 数据库名称
		xml += "<fileddbtype>" + type + "</fileddbtype>";// 数据库类型
		xml += "<filedvalue>" + value + "</filedvalue>";// 字段的值
		xml += "</field>";
		return xml;
	}

	public String trunslate(String xmmc) {
		String sql = "select id from hrmsubcompany where subcompanyname = '" + xmmc + "'";
		RecordSet localRecordSet = new RecordSet();
		localRecordSet.executeSql(sql);
		localRecordSet.next();
		String str7 = Util.null2String(localRecordSet.getString("id"));
		return str7;
	}

	public String trunslateGYS(String gys, String modeid) {
		RecordSet localRecordSet = new RecordSet();
		String str3 = new StringBuilder()
				.append("select w.tablename,w.id from modeinfo m,workflow_bill w where w.id=m.formid and m.id=")
				.append(modeid).toString();
		localRecordSet.executeSql(str3);
		localRecordSet.next();
		String str7 = Util.null2String(localRecordSet.getString("tablename"));
		String sql = "select id from " + str7 + " where ProviderCode = '" + gys + "'";
		localRecordSet.executeSql(sql);
		localRecordSet.next();
		String str8 = Util.null2String(localRecordSet.getString("id"));
		return str8;
	}
}
