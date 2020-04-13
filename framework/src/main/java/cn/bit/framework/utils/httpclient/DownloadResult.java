package cn.bit.framework.utils.httpclient;

public class DownloadResult
{
	private int size;
	private String mimeType;

	public DownloadResult()
	{
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

}
