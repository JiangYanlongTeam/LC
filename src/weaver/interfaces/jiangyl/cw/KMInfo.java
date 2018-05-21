package weaver.interfaces.jiangyl.cw;

import java.io.Serializable;
import java.util.LinkedList;

public class KMInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String level;
	private Double ysTotal;
	private Double fsTotal;
	private String codename;
	private LinkedList<KMInfo> list;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public Double getYsTotal() {
		return ysTotal;
	}
	public void setYsTotal(Double ysTotal) {
		this.ysTotal = ysTotal;
	}
	public Double getFsTotal() {
		return fsTotal;
	}
	public void setFsTotal(Double fsTotal) {
		this.fsTotal = fsTotal;
	}
	public LinkedList<KMInfo> getList() {
		return list;
	}
	public void setList(LinkedList<KMInfo> list) {
		this.list = list;
	}
	public String getCodename() {
		return codename;
	}
	public void setCodename(String codename) {
		this.codename = codename;
	}
}
