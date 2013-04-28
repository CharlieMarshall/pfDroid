package charlie.marshall.pfsense;

import java.io.Serializable;

@SuppressWarnings("serial") //with this annotation we are going to hide compiler warning
public class InterfaceStatus implements Serializable 
{
	static final String TAG = "pfsense_app";
	private String heading;
	private String value;
	
	/*
	 * Set methods
	 */
	
	public void setHeading(String heading)
	{
		this.heading = heading;
	}
	
	public void setValue(String value)
	{
		this.value = value ;
	}
		
	/*
	 * Get methods
	 */
	
	public String getHeading()
	{
		return heading;
	}
	
	public String getValue()
	{
		return value;
	}
	
}
