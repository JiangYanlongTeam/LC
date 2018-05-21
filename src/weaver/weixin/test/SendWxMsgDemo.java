package weaver.weixin.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONObject;

/*
 * 
 * 第三方系统调用泛微云桥接口发送消息实例，请根据需要替换以下方法中的变量 进行调用即可向微信/钉钉等APP中推送消息内容
 * 云桥需要升级到  20150507版本及以上
 */
public class SendWxMsgDemo {

	public static void main(String[] args) {
		Map<String,String> result = sendWxMsg();
		String errcode = (String) result.get("msgcode");
		String msginfo = (String) result.get("msginfo");
		if ("0".equals(errcode)) {
			System.out.println("发送成功");
		} else {
			System.out.println("发送失败:" + msginfo);
		}
	}

	public static Map<String,String> sendWxMsg() {

		String msgcode = "-1"; // 最终错误码为 0 时表示发送成功
		String msginfo = "发送消息失败"; //

		String wxsysurl = "http://218.94.36.101:8088"; // 必须参数，云桥系统访问地址,如
												// http://127.0.0.1:8088
		String tpids = "ae00e80bc3fb4353b13a12b7dd1f0296"; // 必须参数，消息模板ID，如果有多个模板，请用 , 号隔开

		String userids = "longjiangkam"; // 必须参数，消息接收人员 ，使用用户在企业号/钉钉等APP中的唯一标识，多个用户用 ,号隔开（如果有outsysid，则此参数传递的可以是OA系统的人员ID值）
		
		String content = "您有一个业务审批[用地审批]需要批准"; // 必须参数，推送的内容支持文字

		String title = "测试消息提醒标题"; // 消息标题，只有消息模板为图文消息时有用
		String imgurl = ""; // 消息背景图，只有消息模板为图文消息时有用，必须是外网任意用户都可以访问的图片
		String linkurl = ""; // 非必须参数，消息链接地址，只有消息带有链接地址才需要，该地址需要是外网地址，如http://www.weaver.com.cn 设置后点击消息则自动跳转到此地址
													
		String outsysid = ""; // 外部系统ID，如果是泛微OA系统上开发调用的话则必须（获取方式：云桥-集成中心-泛微OA系统集成中配置的系统ID），其他系统调用暂时没用个，但是调用的时候建议将这个值作为可以配置的变量预留以便后续使用
		String accesstoken = ""; // 消息接口密码，如果是泛微OA系统上开发调用的话则必须（获取方式：云桥-集成中心-泛微OA系统集成接入设置中查看接口密码），其他系统调用暂时没用个，但是调用的时候建议将这个值作为可以配置的变量预留以便后续使用

		//其它参数可根据需要进行传递（具体查看说明文档）
		
		PostMethod postMethod = null;
		try {
			postMethod = new PostMethod(wxsysurl + "/wxthirdapi/sendWxMsg");
			NameValuePair[] param = new NameValuePair[9];
			param[0] = new NameValuePair("tpids", tpids);
			param[1] = new NameValuePair("userids", userids);
			param[2] = new NameValuePair("title", title);
			param[3] = new NameValuePair("content", content);
			param[4] = new NameValuePair("imgurl", imgurl);
			param[5] = new NameValuePair("linkurl", linkurl);
			param[6] = new NameValuePair("outsysid", outsysid);
			param[7] = new NameValuePair("accesstoken", accesstoken);
			param[8] = new NameValuePair("hasurl", "0");
			postMethod.setRequestBody(param);
			postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			HttpClient http = new HttpClient();
			http.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			http.getHttpConnectionManager().getParams().setSoTimeout(5000);
			http.executeMethod(postMethod);
			String result = postMethod.getResponseBodyAsString();
			JSONObject rjson = new JSONObject(result);
			if (!"0".equals(rjson.getString("errcode"))) {
				msgcode = "-1";
				msginfo = rjson.getString("errmsg");
				System.out.println("调用云桥系统接口发送消息失败,数据内容：" + content + "  用户列表：" + userids + " 模板：" + tpids + " 错误信息：" + rjson.getString("errmsg"));
			} else {
				msgcode = "0";
				msginfo = "发送成功";
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("调用云桥系统接口发送消息失败, 数据内容：" + content + "  用户列表：" + userids + " 模板：" + tpids + " 异常信息：" + e);
			msgcode = "-1";
			msginfo = "发送失败";
		} finally {
			if (postMethod != null) {
				postMethod.releaseConnection();
			}
		}
		Map<String,String> msgmap = new HashMap<String,String>();
		msgmap.put("msgcode", msgcode);
		msgmap.put("msginfo", msginfo);
		return msgmap;
	}
}
