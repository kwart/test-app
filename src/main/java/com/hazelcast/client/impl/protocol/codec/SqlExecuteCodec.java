/*
 * Copyright (c) 2008-2023, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.client.impl.protocol.codec;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.Generated;
import com.hazelcast.client.impl.protocol.codec.builtin.*;
import com.hazelcast.client.impl.protocol.codec.custom.*;

import javax.annotation.Nullable;

import static com.hazelcast.client.impl.protocol.ClientMessage.*;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.*;

/*
 * This file is auto-generated by the Hazelcast Client Protocol Code Generator.
 * To change this file, edit the templates or the protocol
 * definitions on the https://github.com/hazelcast/hazelcast-client-protocol
 * and regenerate it.
 */

/**
 * Starts execution of an SQL query (as of 4.2).
 */
@Generated("c6f0c66e6b60374a499c416c3e3d3762")
public final class SqlExecuteCodec {
    //hex: 0x210400
    public static final int REQUEST_MESSAGE_TYPE = 2163712;
    //hex: 0x210401
    public static final int RESPONSE_MESSAGE_TYPE = 2163713;
    private static final int REQUEST_TIMEOUT_MILLIS_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_CURSOR_BUFFER_SIZE_FIELD_OFFSET = REQUEST_TIMEOUT_MILLIS_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    private static final int REQUEST_EXPECTED_RESULT_TYPE_FIELD_OFFSET = REQUEST_CURSOR_BUFFER_SIZE_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_SKIP_UPDATE_STATISTICS_FIELD_OFFSET = REQUEST_EXPECTED_RESULT_TYPE_FIELD_OFFSET + BYTE_SIZE_IN_BYTES;
    private static final int REQUEST_INITIAL_FRAME_SIZE = REQUEST_SKIP_UPDATE_STATISTICS_FIELD_OFFSET + BOOLEAN_SIZE_IN_BYTES;
    private static final int RESPONSE_UPDATE_COUNT_FIELD_OFFSET = RESPONSE_BACKUP_ACKS_FIELD_OFFSET + BYTE_SIZE_IN_BYTES;
    private static final int RESPONSE_IS_INFINITE_ROWS_FIELD_OFFSET = RESPONSE_UPDATE_COUNT_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    private static final int RESPONSE_INITIAL_FRAME_SIZE = RESPONSE_IS_INFINITE_ROWS_FIELD_OFFSET + BOOLEAN_SIZE_IN_BYTES;

    private SqlExecuteCodec() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class RequestParameters {

        /**
         * Query string.
         */
        public java.lang.String sql;

        /**
         * Query parameters.
         */
        public java.util.List<com.hazelcast.internal.serialization.Data> parameters;

        /**
         * Timeout in milliseconds.
         */
        public long timeoutMillis;

        /**
         * Cursor buffer size.
         */
        public int cursorBufferSize;

        /**
         * Schema name.
         */
        public @Nullable java.lang.String schema;

        /**
         * The expected result type. Possible values are:
         *   ANY(0)
         *   ROWS(1)
         *   UPDATE_COUNT(2)
         */
        public byte expectedResultType;

        /**
         * Query ID.
         */
        public com.hazelcast.sql.impl.QueryId queryId;

        /**
         * Flag to skip updating phone home statistics.
         */
        public boolean skipUpdateStatistics;

        /**
         * True if the skipUpdateStatistics is received from the client, false otherwise.
         * If this is false, skipUpdateStatistics has the default value for its type.
         */
        public boolean isSkipUpdateStatisticsExists;
    }

    public static ClientMessage encodeRequest(java.lang.String sql, java.util.Collection<com.hazelcast.internal.serialization.Data> parameters, long timeoutMillis, int cursorBufferSize, @Nullable java.lang.String schema, byte expectedResultType, com.hazelcast.sql.impl.QueryId queryId, boolean skipUpdateStatistics) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        clientMessage.setContainsSerializedDataInRequest(true);
        clientMessage.setRetryable(false);
        clientMessage.setOperationName("Sql.Execute");
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[REQUEST_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, REQUEST_MESSAGE_TYPE);
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, -1);
        encodeLong(initialFrame.content, REQUEST_TIMEOUT_MILLIS_FIELD_OFFSET, timeoutMillis);
        encodeInt(initialFrame.content, REQUEST_CURSOR_BUFFER_SIZE_FIELD_OFFSET, cursorBufferSize);
        encodeByte(initialFrame.content, REQUEST_EXPECTED_RESULT_TYPE_FIELD_OFFSET, expectedResultType);
        encodeBoolean(initialFrame.content, REQUEST_SKIP_UPDATE_STATISTICS_FIELD_OFFSET, skipUpdateStatistics);
        clientMessage.add(initialFrame);
        StringCodec.encode(clientMessage, sql);
        ListMultiFrameCodec.encodeContainsNullable(clientMessage, parameters, DataCodec::encode);
        CodecUtil.encodeNullable(clientMessage, schema, StringCodec::encode);
        SqlQueryIdCodec.encode(clientMessage, queryId);
        return clientMessage;
    }

    public static SqlExecuteCodec.RequestParameters decodeRequest(ClientMessage clientMessage) {
        ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
        RequestParameters request = new RequestParameters();
        ClientMessage.Frame initialFrame = iterator.next();
        request.timeoutMillis = decodeLong(initialFrame.content, REQUEST_TIMEOUT_MILLIS_FIELD_OFFSET);
        request.cursorBufferSize = decodeInt(initialFrame.content, REQUEST_CURSOR_BUFFER_SIZE_FIELD_OFFSET);
        request.expectedResultType = decodeByte(initialFrame.content, REQUEST_EXPECTED_RESULT_TYPE_FIELD_OFFSET);
        if (initialFrame.content.length >= REQUEST_SKIP_UPDATE_STATISTICS_FIELD_OFFSET + BOOLEAN_SIZE_IN_BYTES) {
            request.skipUpdateStatistics = decodeBoolean(initialFrame.content, REQUEST_SKIP_UPDATE_STATISTICS_FIELD_OFFSET);
            request.isSkipUpdateStatisticsExists = true;
        } else {
            request.isSkipUpdateStatisticsExists = false;
        }
        request.sql = StringCodec.decode(iterator);
        request.parameters = ListMultiFrameCodec.decodeContainsNullable(iterator, DataCodec::decode);
        request.schema = CodecUtil.decodeNullable(iterator, StringCodec::decode);
        request.queryId = SqlQueryIdCodec.decode(iterator);
        return request;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class ResponseParameters {

        /**
         * Row metadata.
         */
        public @Nullable java.util.List<com.hazelcast.sql.SqlColumnMetadata> rowMetadata;

        /**
         * Row page.
         */
        public @Nullable com.hazelcast.sql.impl.client.SqlPage rowPage;

        /**
         * The number of updated rows.
         */
        public long updateCount;

        /**
         * Error object.
         */
        public @Nullable com.hazelcast.sql.impl.client.SqlError error;

        /**
         * Is the result set unbounded.
         */
        public boolean isInfiniteRows;

        /**
         * True if the isInfiniteRows is received from the member, false otherwise.
         * If this is false, isInfiniteRows has the default value for its type.
         */
        public boolean isIsInfiniteRowsExists;
    }

    public static ClientMessage encodeResponse(@Nullable java.util.List<com.hazelcast.sql.SqlColumnMetadata> rowMetadata, @Nullable com.hazelcast.sql.impl.client.SqlPage rowPage, long updateCount, @Nullable com.hazelcast.sql.impl.client.SqlError error, boolean isInfiniteRows) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[RESPONSE_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, RESPONSE_MESSAGE_TYPE);
        encodeLong(initialFrame.content, RESPONSE_UPDATE_COUNT_FIELD_OFFSET, updateCount);
        encodeBoolean(initialFrame.content, RESPONSE_IS_INFINITE_ROWS_FIELD_OFFSET, isInfiniteRows);
        clientMessage.add(initialFrame);

        ListMultiFrameCodec.encodeNullable(clientMessage, rowMetadata, SqlColumnMetadataCodec::encode);
        CodecUtil.encodeNullable(clientMessage, rowPage, SqlPageCodec::encode);
        CodecUtil.encodeNullable(clientMessage, error, SqlErrorCodec::encode);
        return clientMessage;
    }

    public static SqlExecuteCodec.ResponseParameters decodeResponse(ClientMessage clientMessage) {
        ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
        ResponseParameters response = new ResponseParameters();
        ClientMessage.Frame initialFrame = iterator.next();
        response.updateCount = decodeLong(initialFrame.content, RESPONSE_UPDATE_COUNT_FIELD_OFFSET);
        if (initialFrame.content.length >= RESPONSE_IS_INFINITE_ROWS_FIELD_OFFSET + BOOLEAN_SIZE_IN_BYTES) {
            response.isInfiniteRows = decodeBoolean(initialFrame.content, RESPONSE_IS_INFINITE_ROWS_FIELD_OFFSET);
            response.isIsInfiniteRowsExists = true;
        } else {
            response.isIsInfiniteRowsExists = false;
        }
        response.rowMetadata = ListMultiFrameCodec.decodeNullable(iterator, SqlColumnMetadataCodec::decode);
        response.rowPage = CodecUtil.decodeNullable(iterator, SqlPageCodec::decode);
        response.error = CodecUtil.decodeNullable(iterator, SqlErrorCodec::decode);
        return response;
    }
}
