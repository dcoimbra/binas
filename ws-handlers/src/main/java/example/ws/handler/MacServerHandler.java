package example.ws.handler;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class MacServerHandler implements SOAPHandler<SOAPMessageContext> {

	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean isOutbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if(isOutbound) {	// if message is outbound there is no need to handle/verify message integrity
			return true;
		}
		
		try {
			// get SOAP envelope
			SOAPMessage msg = smc.getMessage();
	        SOAPPart sp = msg.getSOAPPart();
	        SOAPEnvelope se = sp.getEnvelope();
	        SOAPBody sb = se.getBody();
	        SOAPHeader sh = se.getHeader();
			
	        // expected hMAC msg value
	        String hMacExp = digestMessage(convertToString(sb)+getSessionKey());			
			
	        //get header element value
	        Name name = se.createName("hMac", "binas", "http://ws.binas.org/");
	        Iterator<?> it = sh.getChildElements(name);
	       // assert header element value
	       SOAPElement element = (SOAPElement) it.next();
	       String hMacRec = element.getValue();
	       
	        //compare expected and received hMAC values
			if(!hMacExp.equals(hMacRec)) {
				System.out.println("\n\nvalues are different!\n expected:"+hMacExp+"\nbut got:"+hMacRec+"\n\n");
				return false;
			}
			System.out.println("\n\nvalues match!\n\n");
			
		} catch (NoSuchAlgorithmException e) {  //TODO treat exceptions
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	private String convertToString(SOAPBody element) throws Exception{
        
        DOMSource source = new DOMSource(element);
        StringWriter stringResult = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
        String message = stringResult.toString();
        return message;
    }
	
	private String getSessionKey() {	//TODO get session key
		return "123";
	}
	
	
	private String digestMessage(String s) throws NoSuchAlgorithmException {
		MessageDigest msgD = MessageDigest.getInstance("SHA-256");	//TODO user MD5 instead of SHA-256?
		msgD.update(s.getBytes());
		byte[] digest = msgD.digest();
		return printHexBinary(digest);
	}
	
	@Override
	public boolean handleFault(SOAPMessageContext context) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void close(MessageContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

}
