package com.axon.crawler.bean;

import org.jsoup.nodes.Document;

public class infoBean {
    private String phone;
    private String url;
    private String biz;
    private String name;
    private String instruction;
    private String date ;
    private String tag ;
    
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	private Document doc;
    
	public Document getDoc() {
		return doc;
	}
	public void setDoc(Document doc) {
		this.doc = doc;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getBiz() {
		return biz;
	}
	public void setBiz(String biz) {
		this.biz = biz;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getInstruction() {
		return instruction;
	}
	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}
	@Override
	public String toString() {
		return "infoBean [phone=" + phone + ", url=" + url + ", biz=" + biz
				+ ", name=" + name + ", instruction=" + instruction + ", date="
				+ date + ", tag=" + tag + ", doc=" + doc + "]";
	}
    public infoBean(){}
	
    @Override
	public int hashCode() {
		final int a =31;
		return  this.biz.hashCode()*a;
	}
	@Override
	public boolean equals(Object obj) {
		infoBean info =null;
		if(obj instanceof infoBean)info=(infoBean) obj;
			
		if(this.biz.equals(info.getBiz())){
			return true;
		}else {
			return false;
		}
	}
	
}
