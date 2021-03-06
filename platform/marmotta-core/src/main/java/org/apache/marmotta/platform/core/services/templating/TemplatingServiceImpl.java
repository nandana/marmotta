/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.core.services.templating;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.common.base.Preconditions;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.api.templating.TemplatingService;
import org.apache.marmotta.platform.core.events.ConfigurationChangedEvent;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.marmotta.platform.core.startup.MarmottaStartupService;

/**
 * Template Service Implementation
 * 
 * @author Sergio Fernández
 */
@ApplicationScoped
public class TemplatingServiceImpl implements TemplatingService {
	
	private Map<String,String> common; 
	
    @Inject
    private ConfigurationService configurationService;

    @Inject
    private MarmottaStartupService startupService;

	public TemplatingServiceImpl() {
		super();
		common = new HashMap<String,String>();
	}

	@PostConstruct
	public void initDataModel() {
        Preconditions.checkState(startupService.isHostStarted());

        String project = configurationService.getStringConfiguration("kiwi.pages.project", DEFAULT_PROJECT);
        common.put("PROJECT", project);
        common.put("DEFAULT_STYLE", configurationService.getStringConfiguration("kiwi.pages.style_path", DEFAULT_STYLE));
        common.put("SERVER_URL", configurationService.getServerUri());
        common.put("BASIC_URL", configurationService.getBaseUri());
        common.put("LOGO", configurationService.getStringConfiguration("kiwi.pages.project."+project+".logo", project+".png"));
        common.put("FOOTER", configurationService.getStringConfiguration("kiwi.pages.project."+project+".footer", "(footer not properly configured for project "+project+")"));
	}
	
    /**
     * Update the data model in case an important value has changed
     * @param event
     */
    public void configurationChangedEvent(@Observes ConfigurationChangedEvent event) {
        if (event.getKeys().contains("kiwi.context")
                || event.getKeys().contains("kiwi.host")
                || event.getKeys().contains("templating.sort_by_weight")
                || event.getKeys().contains("kiwi.pages.project")) {
            initDataModel();
        }
    }
	
	@Override
    public  Configuration getConfiguration() {
        return getConfiguration(TemplatingServiceImpl.class);
    }

	@Override
    public  Configuration getConfiguration(Class<?> cls) {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(cls, TemplatingService.PATH);
        return cfg;
    }

    @Override
    public Template getTemplate(String name) throws IOException {
        return getConfiguration().getTemplate(name);
    }
	
    @Override
    public Template getTemplate(Class<?> cls, String name) throws IOException {
        return getConfiguration(cls).getTemplate(name);
    }
	
	@Override
	public String process(String name) throws IOException, TemplateException {
		return process(name, new HashMap<String, Object>());
	}

	@Override
	public String process(String name, Map<String, Object> data) throws IOException, TemplateException {
        OutputStream os = new ByteArrayOutputStream();
        Writer writer = new BufferedWriter(new OutputStreamWriter(os));
        process(name, data, writer);
        return os.toString();
	}

	@Override
	public void process(String name, Writer writer) throws IOException, TemplateException {
		process(name, new HashMap<String, Object>(), writer);
	}

	@Override
	public void process(String name, Map<String, Object> data, Writer writer) throws IOException, TemplateException {
		process(TemplatingServiceImpl.class, name, data, writer);
	}

	@Override
	public void process(Class<?> cls, String name, Map<String, Object> data, Writer writer) throws IOException, TemplateException {
        Template tpl = getTemplate(cls, name);
        data.putAll(common);
        tpl.process(data, writer);
        writer.flush();
	}
    
}
