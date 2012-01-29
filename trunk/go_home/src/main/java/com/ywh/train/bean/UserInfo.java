/**************************************************
 * Filename: UserInfo.java
 * Version: v1.0
 * CreatedDate: 2011-11-27
 * Copyright (C) 2011 By cafebabe.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 *
 * If you would like to negotiate alternate licensing terms, you may do
 * so by contacting the author: talentyao@foxmail.com
 ***************************************************/

package com.ywh.train.bean;

import java.io.Serializable;



/**
 * 用户信息
 * 
 * @author cafebabe
 * @since 2011-11-27
 * @version 1.0
 */
public class UserInfo implements Serializable {
	/**字段注释*/
	private static final long serialVersionUID = 1L;
	
	// 乘车人信息
	private String ID;
	private String name;
	private String phone;

	private String seatType = "O"; // 座位类型
	private String tickType = "1"; // 车票类型
	private String cardType = "1"; // 证件类型
	private String idMode = "Y";

	/**
	 * @return Returns the iD.
	 */
	public String getID() {
		return ID;
	}

	/**
	 * @param iD The iD to set.
	 */
	public void setID(String iD) {
		ID = iD;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the phone.
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone The phone to set.
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return Returns the seatType.
	 */
	public String getSeatType() {
		return seatType;
	}

	/**
	 * @param seatType The seatType to set.
	 */
	public void setSeatType(String seatType) {
		this.seatType = seatType;
	}

	/**
	 * @return Returns the tickType.
	 */
	public String getTickType() {
		return tickType;
	}

	/**
	 * @param tickType The tickType to set.
	 */
	public void setTickType(String tickType) {
		this.tickType = tickType;
	}

	/**
	 * @return Returns the cardType.
	 */
	public String getCardType() {
		return cardType;
	}

	/**
	 * @param cardType The cardType to set.
	 */
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	/**
	 * @return Returns the idMode.
	 */
	public String getIdMode() {
		return idMode;
	}
	

	

	public String getText() {
		StringBuilder builder = new StringBuilder();
		builder.append(seatType).append(",").append(tickType).append(",")
				.append(getSimpleText()).append(",").append(idMode);
		return builder.toString();
	}
	
	public String getSimpleText() {
		StringBuilder builder = new StringBuilder();
		builder.append(name).append(",").append(cardType)
		.append(",").append(ID).append(",").append(phone);
		return builder.toString();
	}

	/**
	 * override 方法
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserInfo [ID=").append(ID).append(", name=")
				.append(name).append(", phone=").append(phone)
				.append(", rangDate=").append(", startDate=")
				.append(", seatType=").append(seatType)
				.append(", tickType=").append(tickType).append(", cardType=")
				.append(cardType).append(", idMode=").append(idMode)
				.append("]");
		return builder.toString();
	}

	

}
