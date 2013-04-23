package charlie.marshall.pfsense;

import android.util.Log;

public class Alias
{

	private String name = "";
	private String value = "";
	private String desc = "";
	
	static final String TAG = "pfsense_app";
	
	/*
	 * Constructor 
	 * Initialised with interface - this doesn't really make any sense but its the first variable we come across
	 */
	
	public Alias(String name)
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
	
	public void setValue(String value)
	{
		this.value = value;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}
	
	/*
	 * Get methods 
	 */
	
	public String getName()
	{
		return name;
	}
	
	public String getValue()
	{
		return value;
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
	    return name ;
	}
	
	/*
	 * Method for debugging
	 */
	
	public void printWol()
	{
		Log.d(TAG, "Name: " + name);
		Log.d(TAG, "Value: " + value);
		Log.d(TAG, "Desc: " + desc);
	}

}