package charlie.marshall.pfsense;

import android.util.Log;

public class Interfaces
{

	private String interfaceName ="";
	private String interfaceAlias ="";
	
	static final String TAG = "pfsense_app";
	
	/*
	 * Constructor - initialises with passed variables
	 */
	public Interfaces(String interfaceName,  String interfaceAlias)
	{
		this.interfaceName = interfaceName;
		this.interfaceAlias = interfaceAlias;
	}
	
	
	/*
	 * Get methods 
	 */
	
	public String getName()
	{
		return interfaceName;
	}
	
	public String getAlias()
	{
		return interfaceAlias;
	}


	public void printInterfaces() {
		Log.d(TAG, "Name: " + interfaceName);
		Log.d(TAG, "Alias: " + interfaceAlias);
	}

	public String toString()
	{
		return interfaceAlias;
	}

}