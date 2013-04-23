package charlie.marshall.pfsense;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

@SuppressWarnings("serial") //with this annotation we are going to hide compiler warning
public class Pfsense implements Serializable
{

	private String username ="";
	private String password ="";
	private String domain = "";
	private String protocol ="";
	private String port ="";
	
	private URL loginURL;
	
	static final String TAG = "pfsense_app";
	
	private Navigation links = null;
	
	private HttpCookieStore httpCookie = null;
	private HttpsCookieStore httpsCookie = null;
	
	/*
	 * Constructor - initialises with passed variables
	 */
	public Pfsense(String username, String password, String domain, String protocol, String port)
	{
		this.username = username;
		this.password = password;
		this.domain = domain;
		this.protocol = protocol;
		this.port = port;
		
		httpCookie = new HttpCookieStore();
		httpsCookie = new HttpsCookieStore();
		
		try {
			loginURL = new URL( protocol + "://" + domain + ":" + port );
		} catch (MalformedURLException e) {
			Log.d(TAG, "ERROR CREATING URL");
			e.printStackTrace();
		}
	}
	
	public URL getPfURL()
	{
		return loginURL;
	}
	
	public void setHttpCookieStore(HttpCookieStore cookieStore) 
	{
		httpCookie = cookieStore;
	}
	
	public void setHttpsCookieStore(HttpsCookieStore cookieStore) 
	{
		httpsCookie = cookieStore;
	}
	
	/*
	 * Get methods 
	 */
	
	public HttpCookieStore getHttpCookieStore() 
	{
		return httpCookie;
	}
	
	public HttpsCookieStore getHttpsCookieStore() 
	{
		return httpsCookie;
	}
	
	public String getUser()
	{
		return username;
	}
	
	public String getPassword()
	{
		return password;
	}

	public String getProtocol()
	{
		return protocol;
	}

	public String getDomain()
	{
		return domain;
	}

	public String getPort()
	{
		return port;
	}
	
	public String[] getLinks()
	{
		return links.getHeaderList();
	}
	
	public String getLinkHeader(int i)
	{
		return links.getHeader(i);
	}
	
	public SubDrop getSubDrops(int i)
	{
		return links.getSubDrops(i);
	}
	
	/*
	 * set methods 
	 */
	
	public void setLinks(Navigation links)
	{
		this.links = links;
	}

}


