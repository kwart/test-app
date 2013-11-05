package org.jboss.test;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import org.picketlink.test.trust.ws.WSTest;
import org.picketlink.trust.jbossws.SAML2Constants;
import org.picketlink.identity.federation.api.wstrust.WSTrustClient;
import org.picketlink.identity.federation.api.wstrust.WSTrustClient.SecurityInfo;
import org.picketlink.identity.federation.core.wstrust.WSTrustException;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.trust.jbossws.handler.SAML2Handler;
import org.w3c.dom.Element;

/**
 * Hello world!
 * 
 * @author Josef Cacek
 */
public class App {

    // Constructors ----------------------------------------------------------

    // Public methods --------------------------------------------------------

    public static void main(String[] args) throws Exception {
        Element assertion = getAssertionFromSTS("UserA", "PassA");
        
        // Step 2: Stuff the Assertion on the SOAP message context and add the SAML2Handler to client side handlers
        URL wsdl = new URL("http://localhost:8080/ws-testbean/WSTestBean?wsdl");
        QName serviceName = new QName("http://ws.trust.test.picketlink.org/", "WSTestBeanService");
        Service service = Service.create(wsdl, serviceName);
        WSTest port = service.getPort(new QName("http://ws.trust.test.picketlink.org/", "WSTestBeanPort"), WSTest.class);
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(SAML2Constants.SAML2_ASSERTION_PROPERTY, assertion);
        List<Handler> handlers = bp.getBinding().getHandlerChain();
        handlers.add(new SAML2Handler());
        bp.getBinding().setHandlerChain(handlers);

        // Step 3: Access the WS. Exceptions will be thrown anyway.
        System.out.println(port.echo("Test"));
    }

    protected static Element getAssertionFromSTS(String username, String password) throws Exception {
        // Step 1: Get a SAML2 Assertion Token from the STS
        WSTrustClient client = new WSTrustClient("PicketLinkSTS", "PicketLinkSTSPort", "http://localhost:8080/picketlink-sts/PicketLinkSTS", new SecurityInfo(username,
                password));
        Element assertion = null;
        try {
            System.out.println("Invoking token service to get SAML assertion for " + username);
            assertion = client.issueToken(SAMLUtil.SAML2_TOKEN_TYPE);
            System.out.println("SAML assertion for " + username + " successfully obtained!");
        } catch (WSTrustException wse) {
            System.out.println("Unable to issue assertion: " + wse.getMessage());
            wse.printStackTrace();
            System.exit(1);
        }
        return assertion;
    }
    // Protected methods -----------------------------------------------------

    // Private methods -------------------------------------------------------

    // Embedded classes ------------------------------------------------------
}
