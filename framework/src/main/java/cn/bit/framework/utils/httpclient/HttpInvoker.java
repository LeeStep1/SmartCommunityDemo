package cn.bit.framework.utils.httpclient;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class HttpInvoker
{
	private static Logger logger = LoggerFactory.getLogger(HttpInvoker.class);
	
	private final static int TIMEOUT = 60000;
	private final static int MAX_TOTAL = 200;
	private final static int MAX_PER_ROUTE = 200;
	
	private final PoolingHttpClientConnectionManager connMgr;
	private final CloseableHttpClient httpClient;
	
	private static HttpInvoker invoker;
	
	public static HttpInvoker getInstance()
	{
		if (invoker == null)
		{
			invoker = new HttpInvoker();
		}
		return invoker;
	}
	
	private HttpInvoker()
	{
		SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
		MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200).setMaxLineLength(2000).build();
		ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .setMessageConstraints(messageConstraints)
                .build();
		connMgr = new PoolingHttpClientConnectionManager();
		connMgr.setDefaultSocketConfig(socketConfig);
		connMgr.setDefaultConnectionConfig(connectionConfig);
		connMgr.setMaxTotal(MAX_TOTAL);
		connMgr.setDefaultMaxPerRoute(MAX_PER_ROUTE);
		
		RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(TIMEOUT)
					.setConnectTimeout(TIMEOUT)
					.setConnectionRequestTimeout(TIMEOUT)
					.build();
		
		httpClient = HttpClients.custom()
							.setConnectionManager(connMgr)
							.setDefaultRequestConfig(requestConfig)
							.build();
	}
	
	
	/**
	 * HTTP 调用 GET 方法
	 * 
	 * @param url 网址
	 * @return HTTP 调用返回字符串内容
	 * @throws IOException IO异常时抛出
	 * @throws HttpException HTTP调用异常时抛出
	 */
	public String get(String url) throws IOException, HttpException
	{
		HttpGet request = new HttpGet(url);
		
		return this.request(request);
	}
	
	/**
	 * HTTP 调用 POST 方法
	 * 
	 * @param url 网址
	 * @param param POST提交内容
	 * @return HTTP 调用返回字符串内容
	 * @throws IOException IO异常时抛出
	 * @throws HttpException HTTP调用异常时抛出
	 */
	public String post(String url, String param) throws IOException, HttpException
	{
		StringEntity entity = new StringEntity(param, Consts.UTF_8);
		
		HttpPost request = new HttpPost(url);
		request.setEntity(entity);
		
		return this.request(request);
	}
	
	/**
	 * HTTP 调用 POST 方法
	 * 
     * @param url 网址
     * @param param POST提交内容
	 * @param handler 返回内容处理器
     * @throws IOException IO异常时抛出
     * @throws HttpException HTTP调用异常时抛出
	 */
	public void post(String url, String param, Consumer<InputStream> handler) throws IOException, HttpException
	{
	    StringEntity entity = new StringEntity(param, Consts.UTF_8);
        HttpPost request = new HttpPost(url);
        request.setEntity(entity);
        
        try (CloseableHttpResponse response = httpClient.execute(request);)
        {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK)
                throw new HttpException("调用失败：" + statusLine.toString());
            
            HttpEntity resultEntity = response.getEntity();
            handler.accept(resultEntity.getContent());
        }
	}
	
	/**
	 * HTTP 调用 POST 方法
	 * 
	 * @param url 网址
	 * @param param POST提交内容
	 * @return HTTP 调用返回字符串内容
	 * @throws IOException IO异常时抛出
	 * @throws HttpException HTTP调用异常时抛出
	 */
	public String post(String url, Map<String, Object> param) throws IOException, HttpException
	{
		List<NameValuePair> pairList = new ArrayList<>();
		for (Map.Entry<String, Object> entry : param.entrySet())
		{
			pairList.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairList, Consts.UTF_8);
		
		HttpPost request = new HttpPost(url);
		request.setEntity(entity);
		
		return this.request(request);
	}
	
	/**
	 * HTTP 下载文件
	 * 
	 * @param url 网址
	 * @param file 保存文件
	 * @throws IOException IO异常时抛出
	 * @throws HttpException HTTP调用异常时抛出
	 */
	public DownloadResult download(String url, File file) throws IOException, HttpException
	{
		File dir = file.getParentFile();
		if (!dir.exists())
		{
			if (!dir.mkdirs())
				throw new IOException("创建目录失败：" + dir.getAbsolutePath());
		}
		
		DownloadResult result = new DownloadResult();
		HttpGet request = new HttpGet(url);
		try (CloseableHttpResponse response = httpClient.execute(request);)
		{
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) // http调用失败
				throw new HttpException("下载失败：" + statusLine.toString());
			
			HttpEntity entity = response.getEntity();
			if (!entity.isStreaming())
				throw new HttpException("下载失败：" + EntityUtils.toString(entity, Consts.UTF_8));
				
			result.setSize((int)entity.getContentLength());
			Header type = entity.getContentType();
			logger.debug(type.toString());
			if (type != null)
			{
				int pos = type.getValue().indexOf(';');
				if (pos == -1)
					result.setMimeType(type.getValue());
				else
					result.setMimeType(type.getValue().substring(0, pos));
			}
			try (FileOutputStream fos = new FileOutputStream(file);)
			{
				entity.writeTo(fos);
			}
		}
		return result;
	}
	

	/**
	 * HTTP 统一请求
	 * @param request 请求方式，GET或POST
	 * @return HTTP 调用返回字符串内容
	 * @throws IOException IO异常时抛出
	 * @throws HttpException HTTP调用异常时抛出
     */
	private String request(HttpRequestBase request) throws IOException, HttpException
	{
		try (CloseableHttpResponse response = httpClient.execute(request);)
		{
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK)
				throw new HttpException("调用失败：" + statusLine.toString());
			
			String result = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
			return result;
		}
	}
	
	
	public void destroy()
	{
		try
		{
			httpClient.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		connMgr.shutdown();
	}
}
