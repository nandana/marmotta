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
package org.apache.marmotta.commons.http;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Add file description here!
 * <p/>
 * Author: Sebastian Schaffert
 */
public class LMFHttpUtils {





    /**
     * A utility method for parsing HTTP Content-Type and Accept header, taking into account different parameters that
     * are typically passed. Recognized parameters:
     * - charset: gives the charset of the content
     * - q: gives the precedence of the content
     * The result is an ordered list of content types in order of the computed preference in the header value passed as
     * string argument.
     * <p/>
     * Author: Sebastian Schaffert
     *
     *
     */
    public static List<ContentType> parseAcceptHeader(String header) {
        String[] components = header.split(",");
        List<ContentType> contentTypes = new ArrayList<ContentType>(components.length);
        for(String c : components) {
            String mt[] = c.split(";");

            String[] tst = mt[0].split("/");

            if(tst.length == 2) {
                ContentType type = parseContentType(c);
                if(type != null) {
                    contentTypes.add(type);
                }
            }
        }

        Collections.sort(contentTypes);

        return contentTypes;
    }


    public static List<ContentType> parseStringList(Collection<String> types) {
        List<ContentType> contentTypes = new ArrayList<ContentType>(types.size());
        for(String c : types) {
            ContentType type = parseContentType(c);
            if(type != null) {
                contentTypes.add(type);
            }
        }
        return contentTypes;
    }


    public static ContentType parseContentType(String c) {
        String mt[] = c.split(";");

        String[] tst = mt[0].split("/");

        if(tst.length == 2) {
            ContentType type = new ContentType(tst[0],tst[1]);

            // add parameters
            for(int i=1; i<mt.length; i++) {
                String[] kv = mt[i].split("=");
                if(kv.length == 2) {
                    type.setParameter(kv[0].trim(),kv[1].trim());

                    if("charset".equalsIgnoreCase(kv[0].trim())) {
                        type.setCharset(Charset.forName(kv[1].trim()));
                    }
                }
            }

            return type;
        } else {
            return null;
        }
    }

    /**
     * Determine the best content type out of the selection of content types we offer and the ordered list of content
     * types requested by the peer.
     *
     * TODO: implement remove variant selection algorithm (RFC 2296, http://tools.ietf.org/html/rfc2296)
     *
     * @param offeredTypes list of offered types in order of precedence
     * @param requestedTypes list of requested types in order of precedence - may contain wildcards
     * @return
     */
    public static ContentType bestContentType(List<ContentType> offeredTypes, List<ContentType> requestedTypes) {
        // check for directly matching types
        for(ContentType requested : requestedTypes) {
            for(ContentType offered : offeredTypes) {
                if(requested.matches(offered)) {
                    return offered;
                }
            }
        }
        // check for qualified subtypes also
        for(ContentType requested : requestedTypes) {
            for(ContentType offered : offeredTypes) {
                if(requested.matchesSubtype(offered)) {
                    return offered;
                }
            }
        }

        // check for wildcard matching types
        for(ContentType requested : requestedTypes) {
            for(ContentType offered : offeredTypes) {
                if(requested.matchesWildcard(offered)) {
                    return offered;
                }
            }
        }
        return null;
    }

}
