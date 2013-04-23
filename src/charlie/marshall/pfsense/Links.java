package charlie.marshall.pfsense;

import java.io.Serializable;

@SuppressWarnings("serial") //with this annotation we are going to hide compiler warning
public class Links implements Serializable 
{

	static final String TAG = "pfsense_app";
	
	private String title;
	private String url;
	

	public Links(String title, String url)
	{		
		this.title = title;
		this.url = url;

	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public void setUrl(String url)
	{
		this.url = url ;
	}
	public String getTitle()
	{
		return title;
	}
	
	public String getUrl()
	{
		return url;
	}
	

}