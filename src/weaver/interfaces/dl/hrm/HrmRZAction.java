package weaver.interfaces.dl.hrm;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.MD5;
import weaver.general.Util;
import weaver.hrm.resource.ResourceComInfo;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.Property;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 入职
 * 
 */
public class HrmRZAction extends BaseBean implements Action {

	public String execute(RequestInfo request) {
		RecordSet rs = new RecordSet();
		String gh = "";
		String xm = "";
		String bm = "";
		String fb = "";
		String gw = "";
		String zjsj = "";
		String bgdd = "";
		String xb = "";//0 男
		String rzrq = "";
		String lxfs = "";
		String mima = "";
		String workcode = "";
		String tjr = "";
		String tjrq = "";
		
		String requestid = request.getRequestid();
		String tableName = Util.null2String(request.getRequestManager().getBillTableName());
		Property[] properties = request.getMainTableInfo().getProperty();// 获取表单主字段信息
		for (int i = 0; i < properties.length; i++) {
			String name = properties[i].getName();// 主字段名称
			String value = Util.null2String(properties[i].getValue());// 主字段对应的值
			if(name.equals("gh")){
				gh = value;
				workcode = value;
			}
			if(name.equals("xm")){
				xm = value;
			}
			if(name.equals("bm")){
				bm = value;
            }
			if(name.equals("fb")){
				fb = value;
            }
			if(name.equals("gw")){
				gw = value;
            }
			if(name.equals("zjsj")){
				zjsj = value;
            }
			if(name.equals("bgdd")){
				bgdd = value;
            }
			if(name.equals("xb")){
				xb = value;
            }
			if(name.equals("rzrq")){
				rzrq = value;
            }
			if(name.equals("lxfs")){
				lxfs = value;
            }
			if(name.equals("tjr")){
				tjr = value;
            }
			if(name.equals("tjrq")){
				tjrq = value;
            }
		}
		String isExist = "select * from hrmresource where loginid = '"+gh+"'";
		rs.execute(isExist);
		if(rs.next()) {
			request.getRequestManager().setMessageid("Failed");
			request.getRequestManager().setMessagecontent("工号："+ gh + " 已经存在.");
			return SUCCESS;
		}
		
		String getmaxid = "select max(id) maxid from hrmresource";
		rs.execute(getmaxid);
		rs.next();
		String maxid = rs.getString("maxid");
		maxid = String.valueOf(Integer.parseInt(maxid) + 1);
		MD5 m = new MD5();
		mima = m.getMD5ofStr(gh);
		String insert = "insert into hrmresource (id,lastname,systemlanguage,sex,workcode,managerid,departmentid,"
				+ "subcompanyid1,jobtitle,status,loginid,password,mobile,locationid,createrid,createdate,seclevel) " +
				"values('"+maxid+"','"+xm+"','7','"+xb+"','"+workcode+"','"+zjsj+"','"+bm+"','"+fb+"','"+gw+"','0','"+gh+"','"+mima+"','"+lxfs+"','"+bgdd+"','"+tjr+"','"+tjrq+"','10')";
		writeLog("插入人员信息表SQL："+insert);
		rs.execute(insert);
		
		String existSQL = "select * from cus_fielddata where id = '"+maxid+"'";
		rs.execute(existSQL);
		if(rs.next()) {
			String sql = "update cus_fielddata set field0 = '"+rzrq+"' where id = '"+maxid+"'";
			rs.execute(sql);
		} else {
			String insert2 = "insert into cus_fielddata (id,scopeid,field0,scope,seqorder) values("+maxid+",1,'"+rzrq+"','HrmCustomFieldByInfoType',(select max(seqorder)+1 from cus_fielddata))";
			rs.execute(insert2);
		}
		
		String update = "update "+tableName+" set tzxyg = '"+maxid+"' where requestid = '"+requestid+"'";
		writeLog("更新"+tableName+"员工姓名 "+xm+" SQL："+update);
		rs.execute(update);
        try {
            ResourceComInfo resource = new ResourceComInfo();
            resource.addResourceInfoCache(maxid);
        } catch (Exception e) {
            e.printStackTrace();
        }
		String _sql = "update SequenceIndex set currentid = (select max(id) from hrmresource) + 1  where indexdesc='resourceid'";
		rs.execute(_sql);
		return SUCCESS;
	}
}
