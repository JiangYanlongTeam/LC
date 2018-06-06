package weaver.interfaces.jiangyl.ys;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.jiangyl.util.ECUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 预算编制Action
 */
public class YSCreateAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo request) {
        RecordSet recordSet = new RecordSet();
        String requestid = Util.null2String(request.getRequestid());
        int formid = request.getRequestManager().getFormid();
        Map<String, Object> requestDataMap = ECUtil.getrequestdatamap(requestid, formid);
        // 获取主表信息
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) requestDataMap.get("maindatamap");
        String nf = Util.null2String(map.get("nf"));
        String cbzx = Util.null2String(map.get("cbzx"));
        String organizationtype = "18004";
        String budgetstatus = "1";
        String createrid = "1";
        String createdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String revision = "1";
        String status = "1";
        String budgetPeriods = getBudgetperiods(nf);

        String searchSQL = "SELECT id FROM FnaBudgetInfo WHERE budgetorganizationid = '"+cbzx+"' AND organizationtype = '"+organizationtype+"' AND budgetperiods = '"+budgetPeriods+"' AND status = '"+status+"'";
        writeLog("查询到成本中心在该年度是否编制预算SQL："+searchSQL);
        recordSet.execute(searchSQL);
        String maxid = "";
        boolean isExist = false;
        while(recordSet.next()) {
            isExist = true;
            maxid = Util.null2String(recordSet.getString("id"));
            writeLog("查询到成本中心在该年度已经编制预算，id为："+maxid);
        }

        if(!isExist) {
            String fnaBudgetInfoSQL = "INSERT INTO FnaBudgetInfo (budgetperiods, budgetorganizationid, organizationtype, budgetstatus, createrid, createdate, revision, status) VALUES " +
                    "('"+budgetPeriods+"','"+cbzx+"','"+organizationtype+"','"+budgetstatus+"','"+createrid+"','"+createdate+"','"+revision+"','"+status+"') ";
            writeLog("查询到成本中心在该年度没有编制预算，即将插入预算信息");
            writeLog("插入FnaBudgetInfo："+fnaBudgetInfoSQL);
            recordSet.execute(fnaBudgetInfoSQL);
            recordSet.execute("select max(id) ids from FnaBudgetInfo ");
            recordSet.next();
            maxid = Util.null2String(recordSet.getString("ids"));
        } else {
            String deleteSQL = "delete from fnabudgetinfodetail where budgetinfoid = '"+maxid+"'";
            writeLog("清除成本中心在该年度已经编制预算SQL："+deleteSQL);
            recordSet.execute(deleteSQL);
        }


        List<YSInfo> ysInfoList = new ArrayList<YSInfo>();
        List<Map<String, String>> dt1 = (List<Map<String, String>>) requestDataMap.get("dt1");
        for(int i = 0; i < dt1.size(); i++) {
            Map<String, String> dt2Map = dt1.get(i);
            String budgetaccountid = Util.null2String(dt2Map.get("budgetaccountid"));
            for(int j = 1; j < 13; j++) {
                YSInfo ysInfo = new YSInfo();
                ysInfo.setBudgetaccountid(budgetaccountid);
                ysInfo.setYf(String.valueOf(j));
                String val = "";
                if(j < 10) {
                    val += "0" + j;
                } else {
                    val = "" + j;
                }
                ysInfo.setJe(Util.null2o(dt2Map.get("yue"+val+"")));
                ysInfoList.add(ysInfo);
            }
        }
        if(!ysInfoList.isEmpty()) {
            for(YSInfo ysInfo : ysInfoList) {
                String kmid = ysInfo.getBudgetaccountid();
                String je = ysInfo.getJe();
                String yf = ysInfo.getYf();
                String fnaBudgetDetailInfoSQL = "INSERT INTO fnabudgetinfodetail (budgetinfoid,budgetperiods,budgettypeid,budgetresourceid,budgetcrmid,budgetprojectid,budgetaccount,budgetperiodslist) VALUES " +
                        "('"+maxid+"','"+budgetPeriods+"','"+kmid+"','0','0','0','"+je+"','"+yf+"')";
                writeLog("插入fnabudgetinfodetail："+fnaBudgetDetailInfoSQL);
                recordSet.execute(fnaBudgetDetailInfoSQL);
            }
        }
        return SUCCESS;
    }

    public String getBudgetperiods(String nd) {
        RecordSet recordSet = new RecordSet();
        recordSet.execute("select id from fnayearsperiods where fnayear = '"+nd+"' ");
        recordSet.next();
        return Util.null2String(recordSet.getString("id"));
    }
}
