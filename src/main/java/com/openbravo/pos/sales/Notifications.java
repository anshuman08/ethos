package com.openbravo.pos.sales;

public enum Notifications implements Notification {

	INFORMATION("com/openbravo/images/info.png", "#2C54AB"),
	NOTICE("com/openbravo/images/notice.png", "#8D9695"),
	SUCCESS("com/openbravo/images/success.png", "#009961"),
	WARNING("com/openbravo/images/warning.png", "#E23E0A"),
	ERROR("com/openbravo/images/error.png", "#CC0033");

	private final String urlResource;
	private final String paintHex;

	Notifications(String urlResource, String paintHex) {
		this.urlResource = urlResource;
		this.paintHex = paintHex;
	}

	@Override
	public String getURLResource() {
		return urlResource;
	}

	@Override
	public String getPaintHex() {
		return paintHex;
	}

}
