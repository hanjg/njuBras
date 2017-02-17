package com;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class GUIFrame extends JFrame{  
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HttpClient client;
	private String accout,password;
	private HttpResponse response;
	private HttpClientContext context;
	
	private static final String loginUrl="http://p.nju.edu.cn/portal_io/login";
	private static final String logoutUrl="http://p.nju.edu.cn/portal_io/logout";
	@SuppressWarnings("unused")
	private static final String userUrl="http://p.nju.edu.cn/portal/index.html?v=201510210840";
	//获取账户登录地点，余额，总时间的url
	private static final String listUrl="http://p.nju.edu.cn/portal_io/selfservice/volume/getlist";
	
	private JTextField[] textFields;
	
	public GUIFrame(String accout, String password) {
		initClient(accout, password);
        
        add(new JLabel("账户信息"), BorderLayout.NORTH);
        
        JPanel buttonPanel=new JPanel();
        add(buttonPanel,BorderLayout.SOUTH);
        JButton buttonIn=new JButton("登录");
        buttonIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				login();
			}
		});
        JButton buttonOut=new JButton("下线");
        buttonOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logout();
			}
		});
        buttonPanel.add(buttonIn);
        buttonPanel.add(buttonOut);
        
        //分别显示账户余额，登录地点，累计时长的面板
        JPanel contentPanel=new JPanel(new GridBagLayout());
        add(contentPanel, BorderLayout.CENTER);
        GridBagConstraints constraints=new GridBagConstraints();
        constraints.gridx=0;
        constraints.gridy=0;
        contentPanel.add(new JLabel("账户余额:"), constraints);
        constraints.gridx=0;
        constraints.gridy=1;
        contentPanel.add(new JLabel("登录地点:"), constraints);
        constraints.gridx=0;
        constraints.gridy=2;
        contentPanel.add(new JLabel("累计时长:"), constraints);
        textFields=new JTextField[3];
        for(int i=0;i<textFields.length;i++) {
        	textFields[i]=new JTextField(10);
        	constraints.gridx=1;
        	constraints.gridy=i;
        	contentPanel.add(textFields[i], constraints);
        }
        
        pack();
    }

	/**
	 * 初始化httpclient
	 * @param accout
	 * @param password
	 */
	private void initClient(String accout, String password) {
		client=HttpClients.createDefault();
		context = HttpClientContext.create();
		
        this.accout = accout;  
        this.password = password;
	} 
	
	/**
	 * 由抓取的totalTime得到累计时间
	 * @param totalTime
	 * @return
	 */
	private String getTime(int totalTime){
		int sec=totalTime%60;
		totalTime/=60;
		int min=totalTime%60;
		int hour=totalTime/60;
		return hour+"小时"+min+"分"+sec+"秒";
	}
  
    public void login() {
		try {
			//设定post参数
			ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
			postData.add(new BasicNameValuePair("username",accout));
			postData.add(new BasicNameValuePair("password", password));
			
			//构建post对象   
			HttpPost httpPost = new HttpPost(loginUrl); 
			httpPost.setEntity(new UrlEncodedFormEntity(postData));
			//执行登陆行为 
			response = client.execute(httpPost);
			
			String rawHtml = EntityUtils.toString(response.getEntity(), "utf-8");
			JSONObject jsonObject = new JSONObject(rawHtml);
			if(jsonObject.get("reply_code").toString().equals("6")){
			    System.out.println("已登录");
			}else if (jsonObject.get("reply_code").toString().equals("1")) {
				System.out.println("登录成功");
			}else {
				System.exit(0);
				System.err.println("登录失败");
			}
			JSONObject info=jsonObject.getJSONObject("userinfo");
			//账户余额
			int balance=info.getInt("balance");
			//登录地点
			String area_name=info.getString("area_name");
			
			httpPost=new HttpPost(listUrl);
			response=client.execute(httpPost);
			rawHtml=EntityUtils.toString(response.getEntity(),"utf-8");
			jsonObject=new JSONObject(rawHtml);
			//累计时长
			int total_time=jsonObject.getJSONArray("rows").getJSONObject(0).getInt("total_time");
			
			textFields[0].setText((double)balance/100+"元");
			textFields[1].setText(area_name);
			textFields[2].setText(getTime(total_time));
			
//		数据不在网页源文件中，以下方法取得空字符，因为从源文件总解析
//		HttpGet httpGet=new HttpGet(userUrl);
//		try {
//			response=client.execute(httpGet, context);
//			Document document=Jsoup.parse(EntityUtils.toString(response.getEntity()));
//			Elements elements=document.select("span[id=balance]");
//			String money=elements.first().text();
//			elements=document.select("span[id=area_name]");
//			String area=elements.first().text();
//			elements=document.select("span[id=use_time]");
//			String time=elements.first().text();
//			System.out.println("账户余额："+money+"\t所在地区:"+area+"\t累计时长"+time);
//		     
//		} catch (ClientProtocolException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void logout(){
    	HttpPost httpPost=new HttpPost(logoutUrl);
        try {
			response=client.execute(httpPost, context);
			String rawHtml = EntityUtils.toString(response.getEntity(), "utf-8");
			for(int i=0;i<textFields.length;i++){
				textFields[i].setText("");
			}
			System.out.println("response.getEntity="+rawHtml+
					"\nstatus:" + response.getStatusLine().getStatusCode());
			System.out.println("已下线");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }  
}
