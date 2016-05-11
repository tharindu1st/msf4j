/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.msf4j;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.msf4j.internal.entitywriter.EntityWriterRegistry;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Class that represents an HTTP response in MSF4J level.
 */
public class Response {

    private static final String COMMA_SEPARATOR = ", ";
    private static final int NULL_STATUS_CODE = -1;
    public static final int NO_CHUNK = 0;
    public static final int DEFAULT_CHUNK_SIZE = -1;

    private final CarbonMessage carbonMessage;
    private final CarbonCallback carbonCallback;
    private int statusCode = NULL_STATUS_CODE;
    private String mediaType = null;
    private Object entity;
    private int chunkSize = NO_CHUNK;

    public Response(CarbonCallback carbonCallback) {
        carbonMessage = new DefaultCarbonMessage();
        this.carbonCallback = carbonCallback;
    }

    /**
     * @return true if message is fully available in the response object
     */
    public boolean isEomAdded() {
        return carbonMessage.isEndOfMsgAdded();
    }

    /**
     * @return returns true if the message body is empty
     */
    public boolean isEmpty() {
        return carbonMessage.isEmpty();
    }

    /**
     * @return next available message body chunk
     */
    public ByteBuffer getMessageBody() {
        return carbonMessage.getMessageBody();
    }

    /**
     * @return complete content of the response object
     */
    public List<ByteBuffer> getFullMessageBody() {
        return carbonMessage.getFullMessageBody();
    }

    /**
     * @return map of headers in the response object
     */
    public Map<String, String> getHeaders() {
        return carbonMessage.getHeaders();
    }

    /**
     * Get a header of the response.
     *
     * @param key header neame
     * @return value of the header
     */
    public String getHeader(String key) {
        return carbonMessage.getHeader(key);
    }

    /**
     * Set a header in the response.
     *
     * @param key   header name
     * @param value value of the header
     */
    public void setHeader(String key, String value) {
        carbonMessage.setHeader(key, value);
    }

    /**
     * Add a set of headers to the response as a map.
     *
     * @param headerMap headers to be added to the response
     */
    public void setHeaders(Map<String, String> headerMap) {
        carbonMessage.setHeaders(headerMap);
    }

    /**
     * Get a property of the CarbonMessage.
     *
     * @param key Property key
     * @return property value
     */
    public Object getProperty(String key) {
        return carbonMessage.getProperty(key);
    }

    /**
     * @return map of properties in the CarbonMessage
     */
    public Map<String, Object> getProperties() {
        return carbonMessage.getProperties();
    }

    /**
     * Set a property in the underlining CarbonMessage object.
     *
     * @param key   property key
     * @param value property value
     */
    public void setProperty(String key, Object value) {
        carbonMessage.setProperty(key, value);
    }

    /**
     * @param key remove the header with this name
     */
    public void removeHeader(String key) {
        carbonMessage.removeHeader(key);
    }

    /**
     * Remove a property from the underlining CarbonMessage object.
     *
     * @param key property key
     */
    public void removeProperty(String key) {
        carbonMessage.removeProperty(key);
    }

    /**
     * @return the underlining CarbonMessage object
     */
    public CarbonMessage getCarbonMessage() {
        return carbonMessage;
    }

    /**
     * Set the status code of the HTTP response.
     *
     * @param statusCode HTTP status code
     */
    public void setStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the status code of the HTTP response.
     *
     * @return status code value
     */
    public int getStatusCode() {
        if (statusCode != NULL_STATUS_CODE) {
            return statusCode;
        } else if (entity != null) {
            return javax.ws.rs.core.Response.Status.OK.getStatusCode();
        } else {
            return javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode();
        }
    }

    /**
     * Set HTTP media type of the response.
     *
     * @param mediaType HTTP media type string
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Set http body for the HTTP response.
     *
     * @param entity object that should be set as the response body
     */
    public void setEntity(Object entity) {
        if (!carbonMessage.isEmpty()) {
            throw new IllegalStateException("CarbonMessage should not contain a message body");
        }
        if (entity instanceof javax.ws.rs.core.Response) {
            javax.ws.rs.core.Response response = (javax.ws.rs.core.Response) entity;
            this.entity = response.getEntity();
            MultivaluedMap<String, String> multivaluedMap = response.getStringHeaders();
            if (multivaluedMap != null) {
                multivaluedMap.forEach((key, strings) -> setHeader(key, String.join(COMMA_SEPARATOR, strings)));
            }
            setStatus(response.getStatus());
            if (response.getMediaType() != null) {
                setMediaType(response.getMediaType().toString());
            }
        } else {
            this.entity = entity;
        }
    }

    /**
     * Specify the chunk size to send the response.
     *
     * @param chunkSize if 0 response will be sent without chunking
     *                  if -1 a default chunk size will be applied
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    /**
     * Send the HTTP response using the content in this object.
     */
    public void send() {
        carbonMessage.setProperty(Constants.HTTP_STATUS_CODE, getStatusCode());
        processEntity();
        carbonCallback.done(carbonMessage);
    }

    @SuppressWarnings("unchecked")
    private void processEntity() {
        if (entity != null) {
            EntityWriterRegistry.getInstance()
                    .getEntityWriter(entity.getClass())
                    .writeData(carbonMessage, entity, mediaType, chunkSize);
        } else {
            carbonMessage.addMessageBody(ByteBuffer.allocate(0));
            carbonMessage.setEndOfMsgAdded(true);
        }
    }
}
