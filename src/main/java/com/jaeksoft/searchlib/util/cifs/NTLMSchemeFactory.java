/**
 * Free source code from
 * From http://hc.apache.org/httpcomponents-client-4.3.x/ntlm.html
 */
package com.jaeksoft.searchlib.util.cifs;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.protocol.HttpContext;

public class NTLMSchemeFactory implements AuthSchemeProvider {

	@Override
	public AuthScheme create(final HttpContext context) {
		return new NTLMScheme(new JCIFSEngine());
	}

}