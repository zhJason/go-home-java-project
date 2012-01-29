/**************************************************
 * Filename: LogicThread.java
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

package com.ywh.train.logic;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import tk.mystudio.ocr.OCR;

import com.ywh.train.Config;
import com.ywh.train.Constants;
import com.ywh.train.ResManager;
import com.ywh.train.bean.Result;
import com.ywh.train.bean.TrainQueryInfo;
import com.ywh.train.bean.UserInfo;
import com.ywh.train.gui.RobTicket;

/**
 * 订票逻辑
 * 
 * @author cafebabe
 * @since 2011-11-27
 * @version 1.0
 */
public class LogicThread extends Thread {

	private TrainClient client;
	private RobTicket rob;
	 private volatile Thread blinker = this;
	/**
	 * 构造函数
	 * 
	 * @param robTicket
	 */
	public LogicThread(TrainClient client, RobTicket rob) {
		this.client = client;
		this.rob = rob;
	}

	/**
	 * override 方法<p>
	 * 对run方法进行改进:<p>
	 * 1.增加帐号密码错误的判断，防止做无用的登录操作<p>
	 * 2.改进验证码输入，不必每次都输，大大提高登录效率，可以在短时间内进行大量登录 <p>
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (blinker == thisThread) {
			try {
				Result rs = new Result();			
				int count = 0;
				if (!Constants.isLoginSuc) {
					rob.console(MessageFormat.format(ResManager.getString("LogicThread.0"), rob.getUsername())); //$NON-NLS-1$
					Constants.randCode = getRandCodeDailog(Constants.LOGIN_CODE_URL);
				}
				while (!Constants.isLoginSuc && blinker == thisThread) {
					rs = client.login(rob.getUsername(), rob.getPassword(),
							Constants.randCode);
					if (rs.getState() == Result.SUCC) {
						rob.console(rs.getMsg());
						Constants.isLoginSuc = true;
					} else if (rs.getState() == Result.RAND_CODE_ERROR) {
						rob.console(Constants.CODE_ERROR);
						Constants.randCode = getRandCodeDailog(Constants.LOGIN_CODE_URL);
					} else if (rs.getState() == Result.ACC_ERROR
							|| rs.getState() == Result.PWD_ERROR) {
						rob.console(Constants.USER_ERR);
					} else {
						rob.console(rs.getMsg());
					}
					Thread.sleep(Config.getSleepTime());
					count++;
					
				}
				if (Constants.isLoginSuc) {
					if (count == 0) {
						rob.console(ResManager.getString("LogicThread.1")); //$NON-NLS-1$
					} else if (count < 10) {
						rob.console(MessageFormat.format(ResManager.getString("LogicThread.2"),count)); //$NON-NLS-1$
					} else {
						rob.console(MessageFormat.format(ResManager.getString("LogicThread.3"),count)); //$NON-NLS-1$
					}
				}
				
				while (Constants.isLoginSuc && blinker == thisThread) {
					String randCode = null;
					rob.console(ResManager.getString("LogicThread.4")); //$NON-NLS-1$
					List<TrainQueryInfo> allTrain = client.queryTrain(
							rob.getFromCity(), rob.getToCity(), rob.getStartDate(),
							rob.getRangDate());
					
					AutoTrainAI ai = new AutoTrainAI(allTrain, rob.getTrainSet(),
							rob.getTrainNo(), rob.getFromCity(), rob.getToCity());
					if (ai.getAllTrains().size() == 0) {
						rob.console(MessageFormat.format(ResManager.getString("LogicThread.5") ,rob.getStartDate(),rob.getRangDate(),rob.getFromCity(), rob.getToCity())); //$NON-NLS-1$
						if (rob.getTrainSet()[Constants.isLockTrain]) {
							Thread.sleep(Config.getSleepTime());
							continue;
						}
					} else {
						rob.console(MessageFormat.format(ResManager.getString("LogicThread.6"),rob.getStartDate(),rob.getRangDate(),rob.getFromCity(),rob.getToCity(),ai.getTrainNoView(ai.getCandidateTrains()))); //$NON-NLS-1$
					}			
					TrainQueryInfo goHomeTrain = null;
					Map<String, TrainQueryInfo> spTrains = ai.getSpecificTrains();
					for (String trainNo : rob.getTrainNo()) {
						TrainQueryInfo spTrain = spTrains.get(trainNo);
						if (spTrain == null) {
							rob.console(MessageFormat.format(ResManager.getString("LogicThread.7"), trainNo)); //$NON-NLS-1$
						} else if (Constants.getTrainSeatName(ai.getSpecificSeatAI(spTrain)) == null) {
							rob.console(MessageFormat.format(ResManager.getString("LogicThread.8"),spTrain.getTrainNo(), ai.getTrainSeatView(spTrain))); //$NON-NLS-1$
						} else {
							goHomeTrain = spTrain;
							break;
						}
					}
					
					if (goHomeTrain == null && rob.getTrainSet()[Constants.isLockTrain]){
						Thread.sleep(Config.getSleepTime());
						continue;
					}  else if(!rob.getTrainSet()[Constants.isLockTrain] && goHomeTrain == null){
						rob.console(ResManager.getString("LogicThread.9")); //$NON-NLS-1$
						Map<String, TrainQueryInfo> caTrains = ai.getCandidateTrains();
						for (TrainQueryInfo train : caTrains.values()) {
							if(ai.getSpecificSeatTrains().containsKey(train.getTrainNo())) {
								goHomeTrain = train;
								break;
							} else {
								rob.console(MessageFormat.format(ResManager.getString("LogicThread.10"),train.getTrainNo(),ai.getTrainSeatView(train))); //$NON-NLS-1$
							}
						}
					}
					
					if (goHomeTrain == null) {
						rob.console(ResManager.getString("LogicThread.11")); //$NON-NLS-1$
						Thread.sleep(3000);
						continue;
					}
					
					rob.console(MessageFormat.format(ResManager.getString("LogicThread.12"),goHomeTrain.getTrainNo(),goHomeTrain.getFromStation(), goHomeTrain.getStartTime(),goHomeTrain.getToStation(), goHomeTrain.getEndTime(),goHomeTrain.getTakeTime())); //$NON-NLS-1$
					String seat = ai.getSpecificSeatAI(goHomeTrain);
					rob.console(MessageFormat.format(ResManager.getString("LogicThread.13"),Constants.getTrainSeatName(seat))); //$NON-NLS-1$
					if (seat.equals(Constants.NONE_SEAT)) {
						for (UserInfo ui : rob.getUsers()) {
							ui.setSeatType(Constants.HARD_SEAT);
						}
					} else {
						for (UserInfo ui : rob.getUsers()) {
							ui.setSeatType(seat);
						}
					}
					Thread.sleep(1000);
					//*
					rob.console(ResManager.getString("LogicThread.14")); //$NON-NLS-1$
					rs = client.book(rob.getRangDate(), rob.getStartDate(), goHomeTrain);
					randCode = getRandCodeDailog(Constants.ORDER_CODE_URL);
					String token = client.getToken();
					rob.console(ResManager.getString("LogicThread.15")); //$NON-NLS-1$
					rs = client.submiOrder(randCode, token, rob.getUsers(), goHomeTrain);
					while (rs.getState() == Result.RAND_CODE_ERROR && blinker == thisThread) {
						rob.console(rs.getMsg());
						rs = client.book(rob.getRangDate(), rob.getStartDate(), goHomeTrain);
						randCode = getRandCodeDailog(Constants.ORDER_CODE_URL);
						token = client.getToken();
						rs = client.submiOrder(randCode, token, rob.getUsers(), goHomeTrain);
						Thread.sleep(1000);
						continue;
					}
					rob.console(rs.getMsg());
					if (rs.getState() == Result.CANCEL_TIMES_TOO_MUCH) {
						JOptionPane.showMessageDialog(rob.getFrame(),
								ResManager.getString("LogicThread.16")); //$NON-NLS-1$
						rob.reset(true);
						return;
					}
					if (rs.getState() == Result.REPEAT_BUY_TICKET) {
						JOptionPane.showMessageDialog(rob.getFrame(), 
								ResManager.getString("LogicThread.17")); //$NON-NLS-1$
						rob.reset(true);
						return;
					}
					if (rs.getState() == Result.ERROR_CARD_NUMBER) {
						String info = ResManager.getString("LogicThread.18"); //$NON-NLS-1$
						rob.console(info);
						continue;
					}
					
					if (rs.getState() != Result.UNCERTAINTY) {
						continue;
					}
					
					rob.console(ResManager.getString("LogicThread.19")); //$NON-NLS-1$
					rs = client.queryOrder();
					rob.console(rs.getMsg());
					if (rs.getState() == Result.HAVE_NO_PAY_TICKET) {
						JOptionPane.showMessageDialog(rob.getFrame(),
								ResManager.getString("LogicThread.20")); //$NON-NLS-1$
						rob.reset(true);
						break;
					} else {
						rob.console(ResManager.getString("LogicThread.21")); //$NON-NLS-1$
					}
					//*/
					continue;
				}
			} catch (Exception e) {
//				System.out.println("stop");
			} finally {
				rob.console(ResManager.getString("LogicThread.22")); //$NON-NLS-1$	
			}
		}
	}

	/**
	 * 获得自动识别的验证or用户输入
	 */
	public String getRandCodeDailog(String url) {
		byte[] image = client.getCodeByte(url);
		String randCodeByRob = "";
		int count = 10; // 避免死循环
		while (randCodeByRob.length() != 4 && count-- > 0) {
			randCodeByRob = OCR.read(image);
		}
		if (!rob.isAutocode()) {
			JLabel label = new JLabel(ResManager.getString("LogicThread.23"), JLabel.CENTER); //$NON-NLS-1$
			label.setIcon(new ImageIcon(image));
			label.setText(ResManager.getString("LogicThread.24") + randCodeByRob); //$NON-NLS-1$
			CodeMouseAdapter cma = new CodeMouseAdapter(randCodeByRob);
			label.addMouseListener(cma);
			String input = JOptionPane.showInputDialog(rob.getFrame(), label,
					ResManager.getString("LogicThread.25"), JOptionPane.DEFAULT_OPTION); //$NON-NLS-1$
			if (input == null || input.isEmpty()) {
				randCodeByRob = cma.getRandCodeByRob();
			} else {
				randCodeByRob = input;
			}
		}
		return randCodeByRob;
	}

	class CodeMouseAdapter extends MouseAdapter {
		private String randCodeByRob;

		/**
		 * 构造函数
		 * 
		 * @param CodeMouseAdapter
		 */

		public CodeMouseAdapter(String randCodeByRob) {
			this.randCodeByRob = randCodeByRob;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			byte[] image = client.getCodeByte(Constants.ORDER_CODE_URL);
			randCodeByRob = OCR.read(image);
			JLabel label = (JLabel) e.getSource();
			label.setIcon(new ImageIcon(image));
			label.setText(ResManager.getString("LogicThread.26") + randCodeByRob); //$NON-NLS-1$
		}

		public String getRandCodeByRob() {
			return randCodeByRob;
		}
	}

	/**
	 * @return Returns the client.
	 */
	public TrainClient getClient() {
		return client;
	}

	/**
	 * @param client
	 *            The client to set.
	 */
	public void setClient(TrainClient client) {
		this.client = client;
	}

	/**
	 * @param isEnd
	 *            The isEnd to set.
	 */
	public void setEnd(boolean isEnd) {
		 blinker = null;
		 this.interrupt();
	}
}
