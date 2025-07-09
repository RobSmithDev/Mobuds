/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package mobuds;

import com.sun.lwuit.util.Resources;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.DeviceClass;
import java.io.InputStream;
import java.io.OutputStream;
import javax.bluetooth.UUID;
import javax.obex.*;
import javax.bluetooth.DataElement;
import java.util.*;


// midp/cldc API
import javax.microedition.io.Connector;

/**
 *
 * @author rob smith
 */
public class clsTellFriend implements DiscoveryListener {

    MobudsSystem moSystem;
    DiscoveryAgent agent;
    RemoteDevice remdev;
    String serviceURL = null;
    int[] attrSet = null;
    int transactionID=-1;

 // Manually works out the connection URL from a service record
    private String manualConnectionUrl(ServiceRecord record, String bluetoothAddress) {
        DataElement protocolDescriptorList = record.getAttributeValue(0x0004);
        Enumeration e = (Enumeration) protocolDescriptorList.getValue(); // DATSEQ | DATALT
        e.nextElement(); // L2CAP (ignored)
        DataElement protocolDescriptorRFCOMM = (DataElement) e.nextElement();
        e = (Enumeration) protocolDescriptorRFCOMM.getValue(); // DATSEQ
        e.nextElement(); // UUID (ignored)
        DataElement channelRFCOMM = (DataElement) e.nextElement();
        long channel = channelRFCOMM.getLong(); // U_INT_1

        StringBuffer nameBuffer = new StringBuffer(6+3+12+1+2+19+14+13);
        nameBuffer.append("btgoep://");
        nameBuffer.append(bluetoothAddress);
        nameBuffer.append(":");
        nameBuffer.append(channel);
        nameBuffer.append(";authenticate=false;encrypt=false;master=false");
        return nameBuffer.toString();
    }

	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            if (transID==transactionID) {
                for(int i =0; i < servRecord.length; i++)
                    serviceURL = manualConnectionUrl(servRecord[i],remdev.getBluetoothAddress());
            }
	}

    // Bluetooth search for services completed on a device
    public void serviceSearchCompleted(int transID, int respCode) {
            if (transactionID==transID) {
                synchronized (this) {
                    this.notify();
                }
            }
    }

    // we dont need this
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
    }
    // We dont need this
    public void inquiryCompleted(int discType) {
    }

    public clsTellFriend(MobudsSystem moSys, DiscoveryAgent da, RemoteDevice dev) {
        moSystem = moSys;
        agent = da;
        remdev = dev;
    }

    // Try 1106 and then 1105
    public int run(String serviceI) {
        UUID[] uuids = new UUID[1];
		uuids[0] = new UUID(serviceI,true);

        if (serviceURL==null) {
            try {
                transactionID = agent.searchServices(attrSet, uuids, remdev, this);
            } catch (Exception jsjs) {
                return 1;
            }

            if (transactionID<0) return 2;

            synchronized (this) {
                try {
                    this.wait();
                    agent.cancelServiceSearch(transactionID);
                } catch (Exception ohno) {return 3;};
            }

            // Not found.
            if (serviceURL == null) return 4;
        }

        // Ok, we have a URL. but we need to connect to it and try and send a VCARD!
        int err=4;
        ClientSession con = null;
        HeaderSet hdr;
        OutputStream out = null;
        Operation op = null;
        try {
            Resources resources = Resources.open("/Gfx/quick.res");
            InputStream ip = resources.getData("vcard");
            resources = null;
            byte[] vCard = new byte[ip.available()];
            ip.read(vCard);
            ip.close();
            ip=null;
			int vlen = vCard.length;
            if (vlen<5) return -22;
            err=5;
            con = (ClientSession)Connector.open(serviceURL);
            HeaderSet hs = con.createHeaderSet();
            err=6;
            con.connect(hs);
            hs = con.createHeaderSet();
            hs.setHeader(HeaderSet.TYPE, "text/vCard");
            hs.setHeader(HeaderSet.NAME, "Mobuds.vcf");
            op = con.put(hs);
            out = op.openOutputStream();
            err=8;
			out.write(vCard);
            err=9;
            out.flush();
            try {if (out!=null) out.close();}catch(Exception ex2) {};
            try {if (op!=null) op.close();}catch(Exception ex3) {};
            try {if (con!=null) {con.disconnect(null);con.close();}} catch(Exception ex4) {};
            err=10;
        } catch (Exception onknow2) {
            try {if (op!=null) op.abort();}catch(Exception ex1) {};
            try {if (out!=null) out.close();}catch(Exception ex2) {};
            try {if (op!=null) op.close();}catch(Exception ex3) {};
            try {if (con!=null) {con.disconnect(null);con.close();}} catch(Exception ex4) {};

            return err;
        }

        return 0;
    }

}
