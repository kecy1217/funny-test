package org.funytest.common.utils.http;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;

/**
 * HTTP工具类，封装HttpClient4.3.x来对外提供简化的HTTP请求
 * @author   yangjian1004
 * @Date     Aug 5, 2014     
 */
public class HttpClientExt {

	private HttpProxy proxy;
	
	private static HttpClientExt instance;
	
	/**
	 * 线程安全的单例模式
	 * @return
	 */
	public static HttpClientExt getInstance(){
		
		if (instance == null) {
			synchronized(HttpClientExt.class) {
				if (instance == null)
				instance = new HttpClientExt();
			}
		}
		
		return instance;
	}
	
	/**
	 * 设置代理访问网络
	 * @param proxy
	 */
	public void setProxy(HttpProxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * 是否启用SSL模式
	 * @param enabled
	 */
	public void enableSSL(boolean enabled) {
		HttpClientWrapper.enabledSSL(enabled);
	}

	/**
	 * 使用Get方式 根据URL地址，获取ResponseStatus对象
	 * 
	 * @param url
	 *            完整的URL地址
	 * @return ResponseStatus 如果发生异常则返回null，否则返回ResponseStatus对象
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public ResponseStatus get(String url) throws HttpException, IOException {
		HttpClientWrapper hw = new HttpClientWrapper(proxy);
		return hw.sendRequest(url);
	}
	
	
	public ResponseStatus get(String url, Map<String, String> headers) throws HttpException, IOException {
		HttpClientWrapper hw = new HttpClientWrapper(proxy);
		return hw.sendRequest(url, headers);
	} 

	/**
	 * 使用Get方式 根据URL地址，获取ResponseStatus对象
	 * 
	 * @param url
	 *            完整的URL地址
	 * @param urlEncoding
	 *            编码，可以为null
	 * @return ResponseStatus 如果发生异常则返回null，否则返回ResponseStatus对象
	 */
	public ResponseStatus get(String url, String urlEncoding) {
		HttpClientWrapper hw = new HttpClientWrapper(proxy);
		ResponseStatus response = null;
		try {
			response = hw.sendRequest(url, urlEncoding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 将参数拼装在url中，进行post请求。
	 * 
	 * @param url
	 * @return
	 */
	public ResponseStatus post(String url) {
		HttpClientWrapper hw = new HttpClientWrapper(proxy);
		ResponseStatus ret = null;
		try {
			setParams(url, hw);
			ret = hw.postNV(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private void setParams(String url, HttpClientWrapper hw) {
		String[] paramStr = url.split("[?]", 2);
		if (paramStr == null || paramStr.length != 2) {
			return;
		}
		String[] paramArray = paramStr[1].split("[&]");
		if (paramArray == null) {
			return;
		}
		for (String param : paramArray) {
			if (param == null || "".equals(param.trim())) {
				continue;
			}
			String[] keyValue = param.split("[=]", 2);
			if (keyValue == null || keyValue.length != 2) {
				continue;
			}
			hw.addNV(keyValue[0], keyValue[1]);
		}
	}

	public ResponseStatus post(String url, Map<String, Object> paramsMap, Map<String, String> headers) {
		HttpClientWrapper hw = new HttpClientWrapper(proxy);
		ResponseStatus ret = null;
		try {
			setParams(url, hw);
			Iterator<String> iterator = paramsMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				Object value = paramsMap.get(key);
				if (value instanceof File) {
					FileBody fileBody = new FileBody((File) value);
					hw.getContentBodies().add(fileBody);
				} else if (value instanceof byte[]) {
					byte[] byteVlue = (byte[]) value;
					ByteArrayBody byteArrayBody = new ByteArrayBody(byteVlue, key);
					hw.getContentBodies().add(byteArrayBody);
				} else {
					if (value != null && !"".equals(value)) {
						hw.addNV(key, String.valueOf(value));
					} else {
						hw.addNV(key, "");
					}
				}
			}
			ret = hw.postEntity(url, headers);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * 上传文件（包括图片）
	 * 
	 * @param url
	 *            请求URL
	 * @param paramsMap
	 *            参数和值
	 * @return
	 */
	public ResponseStatus post(String url, Map<String, Object> paramsMap) {
		HttpClientWrapper hw = new HttpClientWrapper(proxy);
		ResponseStatus ret = null;
		try {
			setParams(url, hw);
			Iterator<String> iterator = paramsMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				Object value = paramsMap.get(key);
				if (value instanceof File) {
					FileBody fileBody = new FileBody((File) value);
					hw.getContentBodies().add(fileBody);
				} else if (value instanceof byte[]) {
					byte[] byteVlue = (byte[]) value;
					ByteArrayBody byteArrayBody = new ByteArrayBody(byteVlue, key);
					hw.getContentBodies().add(byteArrayBody);
				} else {
					if (value != null && !"".equals(value)) {
						hw.addNV(key, String.valueOf(value));
					} else {
						hw.addNV(key, "");
					}
				}
			}
			ret = hw.postEntity(url, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 使用post方式，发布对象转成的json给Rest服务。
	 * 
	 * @param url
	 * @param jsonBody
	 * @return
	 */
	public ResponseStatus post(String url, String jsonBody) {
		return post(url, jsonBody, "application/json");
	}

	/**
	 * 使用post方式，发布对象转成的xml给Rest服务
	 * 
	 * @param url
	 *            URL地址
	 * @param xmlBody
	 *            xml文本字符串
	 * @return ResponseStatus 如果发生异常则返回空，否则返回ResponseStatus对象
	 */
	public ResponseStatus postXml(String url, String xmlBody) {
		return post(url, xmlBody, "application/xml");
	}

	private ResponseStatus post(String url, String body, String contentType) {
		HttpClientWrapper hw = new HttpClientWrapper(proxy);
		ResponseStatus ret = null;
		try {
			hw.addNV("body", body);
			ret = hw.postNV(url, contentType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static void main(String[] args) throws HttpException, IOException {
		testGet();
		//testUploadFile();
	}

	//test
	public static void testGet() throws HttpException, IOException {
		String url = "http://www.baidu.com/";
		HttpClientExt c = new HttpClientExt();
		ResponseStatus r = c.get(url);
		System.out.println(r.getContent());
	}

	//test
	public static void testUploadFile() {
		try {
			HttpClientExt c = new HttpClientExt();
			String url = "http://localhost:8280/jfly/action/admin/user/upload.do";
			Map<String, Object> paramsMap = new HashMap<String, Object>();
			paramsMap.put("userName", "jj");
			paramsMap.put("password", "jj");
			paramsMap.put("filePath", new File("C:\\Users\\yangjian1004\\Pictures\\default (1).jpeg"));
			ResponseStatus ret = c.post(url, paramsMap);
			System.out.println(ret.getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}