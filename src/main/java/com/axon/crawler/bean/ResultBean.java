package com.axon.crawler.bean;

public class ResultBean {
	private String tel;
    private String biz;
    private String tag;
    private int fre ;
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getBiz() {
		return biz;
	}
	public void setBiz(String biz) {
		this.biz = biz;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public int getFre() {
		return fre;
	}
	public void setFre(int fre) {
		this.fre = fre;
	}
	@Override
	public String toString() {
		return "InfoBean [tel=" + tel + ", biz=" + biz + ", tag=" + tag
				+ ", fre=" + fre + "]";
	} 
}
