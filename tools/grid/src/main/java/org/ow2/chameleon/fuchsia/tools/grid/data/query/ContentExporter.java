/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.fuchsia.tools.grid.data.query;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.ow2.chameleon.fuchsia.core.component.ExporterService;
import org.ow2.chameleon.fuchsia.tools.grid.ContentHelper;
import org.ow2.chameleon.fuchsia.tools.grid.model.LinkerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Instantiate
public class ContentExporter extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ContentExporter.class);

    private static final String URL = "/contentExporter";

    public static final List<String> EXPORTER_SERVICE_INTERFACE = new ArrayList<String>() {{
        add(ExporterService.class.getName());
    }};

    public static final List<String> EXPORTER_SERVICE_PROPERTIES = new ArrayList<String>();

    @Requires
    HttpService web;

    @Requires
    ContentHelper content;

    BundleContext context;

    public ContentExporter(BundleContext context) {
        this.context = context;
    }

    @Validate
    public void validate() {
        try {
            web.registerServlet(URL, this, null, null);
        } catch (ServletException e) {
            LOG.error("Error while registering the servlet", e);
        } catch (NamespaceException e) {
            LOG.error("Error while registering the servlet", e);
        }
    }

    @Invalidate
    public void invalidate() {
        web.unregister(URL);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        List<LinkerNode> rootList = new ArrayList<LinkerNode>();

        ObjectMapper mapper = new ObjectMapper();

        for (Factory factory : content.getFuchsiaFactories(EXPORTER_SERVICE_INTERFACE, EXPORTER_SERVICE_PROPERTIES)) {
            rootList.add(new LinkerNode(factory.getName()));
        }

        mapper.writeValue(resp.getWriter(), rootList);

    }

}
