/**
 * Free source code from
 * From http://hc.apache.org/httpcomponents-client-ga/ntlm.html
 */
package com.jaeksoft.searchlib.util.cifs;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.params.HttpParams;

public class NTLMSchemeFactory implements AuthSchemeFactory {

	@Override
	public AuthScheme newInstance(final HttpParams params) {
		return new NTLMScheme(new JCIFSEngine());
	}

}