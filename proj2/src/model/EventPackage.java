/**
 * 
 */
package model;

import javax.swing.event.DocumentEvent.EventType;
import java.io.Serializable;

/**
 * @author gyz
 *
 */
public class EventPackage implements Serializable{
	public final String eventType;
	public final int len;
	public final int offset;
	public final String inserted;
	public final int docLength;
	
	public EventPackage(String eventType, int len, int offset, String inserted, int docLength){
		this.eventType=eventType;
		this.len=len;
		this.offset=offset;
		this.inserted=inserted;
		this.docLength= docLength;
	}
	
	public EventPackage(){
	    this.eventType = "None";
	    this.len = -1;
	    this.offset = -1;
	    this.inserted = "$$$";
	    this.docLength = -1;
	}
	
    public boolean equals(EventPackage that) {
        return (((this.eventType.equals(that.eventType) && this.len == that.len) && this.offset == that.offset) && this.inserted
                .equals(that.inserted)) && (this.docLength == that.docLength);
    }
	

}
