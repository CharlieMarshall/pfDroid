package charlie.marshall.pfsense;

import android.util.Log;

public class Wol
{

	private String interfaceName = "";
	private String interfaceAlias = "";
	private String mac ="";
	private String desc = "";
	
	static final String TAG = "pfsense_app";
	
	/*
	 * Constructor 
	 * Initialised with interface - this doesn't really make any sense but its the first variable we come across
	 */
	
	public Wol(String interfAlias, String interfName)
	{
		this.interfaceAlias = interfAlias;
		this.interfaceName = interfName;
	}
	
	/*
	 * Set methods
	 */
	
	public void setInterfaceName(String interfaceName)
	{
		this.interfaceName = interfaceName;
	}
	
	public void setInterfaceAlias(String interfAlias)
	{
		this.interfaceAlias = interfAlias;
	}
	
	public void setMac(String mac)
	{
		this.mac = mac;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}
	
	/*
	 * Get methods 
	 */
	
	public String getInterfaceName()
	{
		return interfaceName;
	}
	
	public String getInterfaceAlias()
	{
		return interfaceAlias;
	}
	
	public String getMac()
	{
		return mac;
	}

	public String getDesc()
	{
		return desc;
	}
	
	/*
	 * Methods required for android spinner
	 */
	
	public String toString()
	{
	    return desc ;
	}
	
	/*
	 * Method for debugging
	 */
	
	public void printWol()
	{
		Log.d(TAG, "Interface: " + interfaceAlias);
		Log.d(TAG, "PFsense interface Name: " + interfaceName);
		Log.d(TAG, "Mac: " + mac);
		Log.d(TAG, "Desc: " + desc);
	}

}