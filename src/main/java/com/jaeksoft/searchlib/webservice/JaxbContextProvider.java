/*
 * Copyright 2014-2017 OpenSearchServer Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaeksoft.searchlib.webservice;

import org.reflections.Reflections;

import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.HashSet;
import java.util.Set;

public class JaxbContextProvider implements ContextResolver<JAXBContext> {

	private JAXBContext context;

	public JaxbContextProvider() throws JAXBException {
		final Reflections reflections = new Reflections("com.jaeksoft.searchlib");
		final Set<Class<?>> annotated = new HashSet<>();
		annotated.addAll(reflections.getTypesAnnotatedWith(XmlRootElement.class, true));
		annotated.addAll(reflections.getTypesAnnotatedWith(XmlType.class, true));
		context = JAXBContext.newInstance(annotated.toArray(new Class<?>[annotated.size()]));
	}

	@Override
	public JAXBContext getContext(Class<?> type) {
		return context;
	}
}
