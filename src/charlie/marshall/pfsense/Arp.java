package charlie.marshall.pfsense;

import java.io.Serializable;

import android.util.Log;

@SuppressWarnings("serial") //with this annotation we are going to hide compiler warning
public class Arp implements Serializable
{

	private String ip = "";
	private String mac = "";
	private String hostname = "";
	private String interf = "";
	
	static final String TAG = "pfsense_app";
	
	/*
	 * Constructor 
	 * Initialised with interface - this doesn't really make any sense but its the first variable we come across
	 */
	
	public Arp(String ip)
	{
		this.ip = ip;
	}
	
	/*
	 * Set methods
	 */
	
	public void setName(String ip)
	{
		this.ip = ip;
	}
	
	public void setMac(String mac)
	{
		this.mac = mac;
	}

	public void setHostName(String hostname)
	{
		this.hostname = hostname;
	}
	
	public void setInterface(String interf)
	{
		this.interf = interf;
	}
	
	/*
	 * Get methods 
	 */
	
	public String getIP()
	{
		return ip;
	}
	
	public String getMac()
	{
		return mac;
	}

	public String getHostname()
	{
		return hostname;
	}
	
	public String getInterface()
	{
		return interf;
	}
	
	/*
	 * Methods required for android spinner
	 */
	
	public String toString()
	{
	    return ip ;
	}
	
	/*
	 * Method for debugging
	 */
	
	public void printArp()
	{
		Log.d(TAG, "IP: " + ip);
		Log.d(TAG, "Mac: " + mac);
		Log.d(TAG, "Hostname: " + hostname);
		Log.d(TAG, "Interface: " + interf);
	}

}