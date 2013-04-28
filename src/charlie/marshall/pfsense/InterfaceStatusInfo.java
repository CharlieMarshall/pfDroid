package charlie.marshall.pfsense;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

@SuppressWarnings("serial") //with this annotation we are going to hide compiler warning
public class InterfaceStatusInfo implements Serializable
{

	static final String TAG = "pfsense_app";
	private List<InterfaceStatus> nameValue;
	private String header = "";
	private String link = "";
	private String type = "";
	private String typeValue = "";
	private String statusStr = "";
	
	boolean status = false;
	boolean hasButton = false;
	boolean hasType = false;

	/*
	 * Constructor
	 */
	
	public InterfaceStatusInfo(String header)
	{
		this.header = header;
		nameValue = new ArrayList<InterfaceStatus>();
	}
	
	/*
	 * Set methods
	 */
	
	/*
	 * setStatus
	 * 
	 * up = true
	 * Anything else = false
	 */
	
	public void setStatus(boolean status)
	{
		this.status = status;
	}
	
	/*
	 * setStatusStr
	 * 
	 * Sets the value of the Status
	 * ie up, no carrier etc
	 */
	
	
	public void setStatusStr(String status)
	{
		this.statusStr = status;
	}
	
	/*
	 * setInterfaceType
	 * 
	 * hasType = true if there is a type field
	 * type = the NAME of the type field
	 */
	
	public void setInterfaceType(boolean hasType, String type)
	{
		this.hasType = hasType;
		this.type = type;
	}
	
	/*
	 * setInterfaceTypeValue
	 * 
	 * typeValue = the value of the type field
	 */
	
	public void setInterfaceTypeValue(String typeValue)
	{
		this.typeValue = typeValue;
	}
	
	/*
	 * addInterface
	 * 
	 * adds the name value pair to the arrayList
	 */
	
	public void addInterface(InterfaceStatus is)
	{
		nameValue.add(is);
	}
	
	/*
	 * setLink
	 * 
	 * link = the link attached to the button
	 * hasButton = true if there is a button
	 */
	
	public void setLink(String link)
	{
		this.link = link;
		hasButton = true;
	}
	
	/*
	 * Get methods
	 */
	
	public boolean getStatus()
	{
		return status;
	}
	
	public String getStatusStr()
	{
		return statusStr;
	}

	public String getHeader()
	{
		return header;
	}
	
	public boolean hasButton()
	{
		return hasButton;
	}
	
	public boolean hasType()
	{
		return hasType;
	}
	
	public String getLink()
	{
		return link;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getTypeValue()
	{
		return typeValue;
	}

	public String getHeader(int i)
	{
		return nameValue.get(i).getHeading();
	}

	public String getValue(int i)
	{
		return nameValue.get(i).getValue();
	}

	public int getSize()
	{
		return nameValue.size();
	}

	public void printTest()
	{
		Log.d(TAG, "interface TITLE: " + header);

		for (int i=0; i<nameValue.size(); i++)
		{
			Log.d(TAG, "Item header: " + nameValue.get(i).getHeading());
			Log.d(TAG, "Item value: " + nameValue.get(i).getValue());
		}
	}
	
	public String toString()
	{
	    return header ;
	}

}
