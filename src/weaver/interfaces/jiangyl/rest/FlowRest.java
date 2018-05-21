package weaver.interfaces.jiangyl.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import weaver.conn.RecordSet;
import weaver.formmode.webservices.ModeDataServiceImpl;
import weaver.formmode.webservices.ModeDateService;
import weaver.general.BaseBean;
import weaver.general.Util;

@Path("/report")
public class FlowRest extends BaseBean {

	public static final String CODE_HASNODATA = "00000001";
	public static final String CODE_HASNODATA_MESSAGE = "数据非法";
	public static final String CODE_ANALYSE = "00000002";
	public static final String CODE_ANALYSE_MESSAGE = "解析错误";
	public static final String CODE_ANALYSESHAO = "00000003";
	public static final String CODE_ANALYSESHAO_MESSAGE = "缺少唯一标识字段wybs";
	public static final String CODE_ANALYSEFAIDED = "00000004";
	public static final String CODE_ANALYSEFAIDED_MESSAGE = "数据非法";
	public static final String CODE_SUCCESS = "200";
	public static final String CODE_SUCCESS_MESSAGE = "插入成功";
	public static final String CODE_SUCCESS_MESSAGE2 = "数据库已经存在，忽略处理成功";

	@POST
	@Path("/insert")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String show(@Context HttpServletRequest req) {
		BaseBean bean = new BaseBean();
		String modelid = bean.getPropValue("flowreport", "modelid");
		StringBuffer data = new StringBuffer(); // 初始化新的字符串对象
		InputStream is = null;
		try {
			is = req.getInputStream();
			// 返回的一个代表实体内容的输入流对象
			InputStreamReader isr = new InputStreamReader(is,"UTF-8"); // 将输入流字节转化为字符
			BufferedReader br = new BufferedReader(isr); // 将字符读入缓冲区
			String s = "";
			try {
				while ((s = br.readLine()) != null) { // 逐行读取数据，将数据
					data.append(s);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String str = data.toString(); // 把对象转化为字符串
		JSONObject info = null;
		JSONArray lines = null;
		writeLog("接收参数："+str);
		try {
			info = new JSONObject(str);
			lines = info.getJSONArray("line");
			if (null == lines || lines.length() == 0) {
				return setFailMessage(CODE_HASNODATA, CODE_HASNODATA_MESSAGE).toString();
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return setFailMessage(CODE_HASNODATA, CODE_HASNODATA_MESSAGE).toString();
		}
		// {"line":[{"filedname":"xtbs","fileddbtype":"varchar(50)","filedvalue":"OA测试"},{"filedname":"lcid","fileddbtype":"varchar(100)","filedvalue":"流程ID"}]},{"line":[{"filedname":"xtbs","fileddbtype":"varchar(50)","filedvalue":"OA测试1"},{"filedname":"lcid","fileddbtype":"varchar(100)","filedvalue":"流程ID2"}]}
		
		// 系统标志+流程ID+操作者帐号+接收日期+接收时间 wybs
		RecordSet rs = new RecordSet();
		String sql = "select w.tablename from modeinfo m,workflow_bill w where w.id=m.formid and m.id = '"+modelid+"'";
		writeLog("查询建模表SQL："+sql);
		rs.execute(sql);
		rs.next();
		String tableName = Util.null2String(rs.getString("tablename"));
		writeLog("查询建模表名："+tableName);
		if("".equals(tableName)) {
			return setFailMessage(CODE_ANALYSE, CODE_ANALYSE_MESSAGE).toString();
		}
		
		List<String> cols = init(tableName);
		
		String wybs = "";
		for(int m = 0; m < lines.length(); m++) {
			try {
				JSONObject l = (JSONObject) lines.get(m);
				String filedname = Util.null2String(l.get("filedname"));
				String filedvalue = Util.null2String(l.get("filedvalue"));
				if("wybs".equalsIgnoreCase(filedname)) {
					wybs = filedvalue;
				}
				if("".equals(filedname)) {
					return setFailMessage(CODE_ANALYSEFAIDED, CODE_ANALYSEFAIDED_MESSAGE + ":filedname不能为空").toString();
				}
				if("".equals(filedvalue)) {
					return setFailMessage(CODE_ANALYSEFAIDED, CODE_ANALYSEFAIDED_MESSAGE + ":filedvalue不能为空").toString();
				}
				if(!cols.contains(filedname)) {
					return setFailMessage(CODE_ANALYSEFAIDED, CODE_ANALYSEFAIDED_MESSAGE + ":filedname["+filedname+"]非法").toString();
				}
 			} catch (JSONException e) {
				e.printStackTrace();
				return setFailMessage(CODE_ANALYSE, CODE_ANALYSE_MESSAGE).toString();
			}
		}
		if("".equals(wybs)) {
			return setFailMessage(CODE_ANALYSESHAO, CODE_ANALYSESHAO_MESSAGE+",缺少唯一标志").toString();
		}
		
		
		String sql1 = "select id from " + tableName + " where wybs = '"+wybs+"'";
		writeLog("查询建模表wybs："+sql1);
		rs.execute(sql1);
		int exitsCount = rs.getCounts();
		if(exitsCount > 0) {
			writeLog("唯一标识："+wybs + "在数据库中已经存在，本次传输数据【"+str +"】忽略传输");
			return setFailMessage(CODE_SUCCESS, CODE_SUCCESS_MESSAGE2).toString();
		}
		StringBuffer sb = new StringBuffer();
		sb.append(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><ROOT><header><userid>1</userid><modeid>"+modelid+"</modeid><id></id></header><search><condition /><right>Y</right></search><data id=\"\"><maintable>");
		for (int i = 0; i < lines.length(); i++) {
			try {
				JSONObject l = (JSONObject) lines.get(i);
				Object filedname = l.get("filedname");
				Object fileddbtype = l.get("fileddbtype");
				Object filedvalue = l.get("filedvalue");
				sb.append(add(filedname, fileddbtype, filedvalue));
			} catch (JSONException e) {
				e.printStackTrace();
				return setFailMessage(CODE_ANALYSE, CODE_ANALYSE_MESSAGE).toString();
			}
		}
		sb.append("</maintable></data></ROOT>");
		ModeDateService service = new ModeDataServiceImpl();
		writeLog("传入参数：" + sb.toString());
		Map<String, String> map = ana(service.saveModeData(sb.toString()));
		if (map.isEmpty() || null == map.get("id")) {
			return setFailMessage(CODE_ANALYSE, CODE_ANALYSE_MESSAGE).toString();
		}
		return setFailMessage(CODE_SUCCESS, CODE_SUCCESS_MESSAGE).toString();
	}

	public JSONObject setFailMessage(String code, String flag) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("code", code);
			jsonObject.put("desc", flag);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
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

	/**
	 * 解析返回XML
	 * 
	 * @param xml
	 * @return
	 */
	public Map<String, String> ana(String xml) {
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
				if ("return".equals(nodename)) {
					String value = el.getChild("id").getValue();
					String returnnode = el.getChild("returnnode").getValue();
					String returnmessage = el.getChild("returnmessage").getValue();
					map.put("id", value);
					map.put("returnnode", returnnode);
					map.put("returnmessage", returnmessage);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public List<String> init(String tableName) {
		RecordSet rs = new RecordSet();
		List<String> list = new ArrayList<String>();
		String sql = "select name from syscolumns where id = object_id('"+tableName+"')";
		writeLog("获取表列SQL："+sql);
		rs.execute(sql);
		while(rs.next()) {
			String name = Util.null2String(rs.getString("name"));
			list.add(name);
		}
		return list;
	}
}
