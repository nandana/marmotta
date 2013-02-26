<%--

    Copyright (C) 2013 The Apache Software Foundation.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ page import="org.apache.marmotta.platform.core.api.config.ConfigurationService" %>
<%@ page import="org.apache.marmotta.platform.core.util.CDIContext" %>
<%
    ConfigurationService configurationService = CDIContext.getInstance(ConfigurationService.class);
    response.sendRedirect(configurationService.getBaseUri()+configurationService.getStringConfiguration("kiwi.pages.startup"));
%>
