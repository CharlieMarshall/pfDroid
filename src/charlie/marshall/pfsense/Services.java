package charlie.marshall.pfsense;

import java.io.Serializable;

import android.util.Log;

@SuppressWarnings("serial") //with this annotation we are going to hide compiler warning
public class Services implements Serializable
{

	private String name = "";
	private String status = "";
	private String desc = "";
	private String start = "";
	private String stop = "";
	
	static final String TAG = "pfsense_app";
	
	/*
	 * Constructor 
	 */
	
	public Services(String name)
	{
		this.name = name;
	}
	
	/*
	 * Set methods
	 */
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}
	
	public void setStart(String start)
	{
		this.start = start;
	}
	
	public void setStop(String stop)
	{
		this.stop = stop;
	}
	
	/*
	 * Get methods 
	 */
	
	public String getName()
	{
		return name;
	}
	
	public String getStatus()
	{
		return status;
	}

	public String getDesc()
	{
		return desc;
	}
	
	public String getStart()
	{
		return start;
	}
	
	public String getStop()
	{
		return stop;
	}
	
	/*
	 * Methods required for android spinner
	 */
	
	public String toString()
	{
	    return name + " - " + status ;
	}
	
	/*
	 * Method for debugging
	 */
	
	public void printWol()
	{
		Log.d(TAG, "Name: " + name);
		Log.d(TAG, "Status: " + status);
		Log.d(TAG, "Desc: " + desc);
	}

}