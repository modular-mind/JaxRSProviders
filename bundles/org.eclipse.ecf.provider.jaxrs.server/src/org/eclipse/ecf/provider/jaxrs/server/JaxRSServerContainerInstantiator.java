/*******************************************************************************
* Copyright (c) 2015 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.provider.jaxrs.server;

import java.net.URI;
import java.util.Map;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.provider.jaxrs.JaxRSContainerInstantiator;

public abstract class JaxRSServerContainerInstantiator extends JaxRSContainerInstantiator {

	public static final String URL_PROTOCOL_PROP = "protocol";
	public static final String URL_PROTOCOL_DEFAULT = "http";
	public static final String URL_HOSTNAME_PROP = "hostname";
	public static final String URL_HOSTNAME_DEFAULT = "localhost";
	public static final String URL_PORT_PROP = "port";
	public static final String URL_HTTP_PORT_DEFAULT = "80";
	public static final String URL_HTTPS_PORT_DEFAULT = "443";
	public static final String URL_PATH_PREFIX_PROP = "pathPrefix";
	public static final String URL_PATH_PREFIX_DEFAULT = "/";
	public static final String URL_PREFIX_PROP = "urlPrefix";

	public JaxRSServerContainerInstantiator(String serverConfigTypeName) {
		super(serverConfigTypeName);
	}

	public JaxRSServerContainerInstantiator(String serverConfigTypeName, String clientConfigTypeName) {
		super(serverConfigTypeName, clientConfigTypeName);
	}

	protected String getDotProperty(String configName, String paramName) {
		return configName + "." + paramName;
	}

	protected String getSystemProperty(String configName, String key, String def) {
		return System.getProperty(getDotProperty(configName, key), def);
	}

	protected String getProtocol(Map<String, ?> params, String configName) {
		return super.getParameterValue(params, URL_PROTOCOL_PROP,
				getSystemProperty(configName, URL_PROTOCOL_PROP, URL_PROTOCOL_DEFAULT));
	}

	protected String getHostname(Map<String, ?> params, String configName) {
		return super.getParameterValue(params, URL_HOSTNAME_PROP,
				getSystemProperty(configName, URL_HOSTNAME_PROP, URL_HOSTNAME_DEFAULT));
	}

	protected String getPort(Map<String, ?> params, String configName, boolean https) {
		if (https) {
			String p = super.getParameterValue(params, URL_PORT_PROP,
					getSystemProperty(configName, URL_PORT_PROP, URL_HTTPS_PORT_DEFAULT));
			if (p.equalsIgnoreCase(URL_HTTPS_PORT_DEFAULT))
				return "";
			return p;
		} else {
			String p = super.getParameterValue(params, URL_PORT_PROP,
					getSystemProperty(configName, URL_PORT_PROP, URL_HTTP_PORT_DEFAULT));
			if (p.equalsIgnoreCase(URL_HTTP_PORT_DEFAULT))
				return "";
			return p;
		}
	}

	protected String getPath(Map<String, ?> params, String configName) {

		return super.getParameterValue(params, URL_PATH_PREFIX_PROP,
				getSystemProperty(configName, URL_PATH_PREFIX_PROP, URL_PATH_PREFIX_DEFAULT));
	}

	protected String getUrl(Map<String, ?> params, String configName) {
		String sysUp = getSystemProperty(configName, URL_PREFIX_PROP, null);
		if (sysUp != null)
			return sysUp;
		else {
			String protocol = getProtocol(params, configName);
			String hostname = getHostname(params, configName);
			String port = getPort(params, configName, protocol.equalsIgnoreCase("https"));
			String path = getPath(params, configName);
			return protocol + "://" + hostname + ((!"".equals(port)) ? ":" + port : "") + path;
		}

	}

	protected URI getUri(Map<String, ?> params, String configName) throws ContainerCreateException {
		try {
			return new URI(getUrl(params, configName));
		} catch (Exception e) {
			throw new ContainerCreateException("Cannot create Jersey Server Container uri", e);
		}
	}
}
