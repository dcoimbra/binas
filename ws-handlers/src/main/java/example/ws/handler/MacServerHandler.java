package example.ws.handler;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.security.Key;
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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
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
		// FOR INBOUND MESSAGES:
		try {
			// get SOAP envelope
			SOAPMessage msg = smc.getMessage();
	        SOAPPart sp = msg.getSOAPPart();
	        SOAPEnvelope se = sp.getEnvelope();
	        SOAPBody sb = se.getBody();
	        SOAPHeader sh = se.getHeader();
			
	        // expected hMAC msg value		
			
	        //get header element value
	        Name name = se.createName("hMac", "binas", "http://ws.binas.org/");
	        Iterator<?> it = sh.getChildElements(name);
	       // assert header element value
	       SOAPElement element = (SOAPElement) it.next();
	       String hMacRec = element.getValue();
	       
	       Key hMacKey = getSessionKey(smc);
	       System.out.println("\t\t\tSESSION KEY:"+hMacKey);
	       String hMacExp = digestMessage(convertToString(sb)+hMacKey.toString());	
	        //compare expected and received hMAC values
			if(!hMacExp.equals(hMacRec)) {
				System.out.println("\n\nvalues are different!\n expected:"+hMacExp+"\nbut got:"+hMacRec+"\n\n");
				return false;
			}
			System.out.println("\n\nvalues match!\n\n");
			
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		} catch (SOAPException e) {
			throw new RuntimeException(e.getMessage());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		
		return true;
	}

	private String convertToString(SOAPBody element) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        
        DOMSource source = new DOMSource(element);
        StringWriter stringResult = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
        String message = stringResult.toString();
        return message;
    }
	
	private Key getSessionKey(SOAPMessageContext smc) {
		
		Key sessionKey = (Key) smc.get("SESSION_KEY");
		return sessionKey;
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
