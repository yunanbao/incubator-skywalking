/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.agent.core.context.trace;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.skywalking.apm.agent.core.context.AbstractTracerContext;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.util.KeyValuePair;
import org.apache.skywalking.apm.agent.core.context.util.ThrowableTransformer;
import org.apache.skywalking.apm.agent.core.dictionary.DictionaryUtil;
import org.apache.skywalking.apm.network.proto.SpanObject;
import org.apache.skywalking.apm.network.proto.SpanType;
import org.apache.skywalking.apm.network.trace.component.Component;

/**
 * @author wusheng
 */
public class ManualSpan implements IManualSpan {
    protected volatile int spanId;
    protected volatile int parentSpanId;
    protected volatile List<KeyValuePair> tags;
    protected volatile String operationName;
    protected volatile int operationId;
    protected volatile SpanLayer layer;
    /**
     * The start time of this Span.
     */
    protected volatile long startTime;
    /**
     * The end time of this Span.
     */
    protected volatile long endTime;
    /**
     * Error has occurred in the scope of span.
     */
    protected volatile boolean errorOccurred = false;

    protected volatile int componentId = 0;

    protected volatile String componentName;

    /**
     * Log is a concept from OpenTracing spec. https://github.com/opentracing/specification/blob/master/specification.md#log-structured-data
     */
    protected volatile List<LogDataEntity> logs;

    /**
     * The refs of parent trace segments, except the primary one. For most RPC call, {@link #refs} contains only one
     * element, but if this segment is a start span of batch process, the segment faces multi parents, at this moment,
     * we use this {@link #refs} to link them.
     */
    protected volatile List<TraceSegmentRef> refs;

    protected volatile ReentrantLock spanSelfLock;

    protected volatile AbstractTracerContext context = null;

    private volatile boolean isFinished = false;

    protected ManualSpan(String operationName) {
        this.operationName = operationName;
        this.operationId = DictionaryUtil.nullValue();
        spanSelfLock = new ReentrantLock();
    }

    public void belongToCurrentContext() {
        ContextManager.recruit(this);
    }

    /**
     * Set a key:value tag on the Span.
     *
     * @return this Span instance, for chaining
     */
    @Override
    public AbstractSpan tag(String key, String value) {
        if (tags == null) {
            tags = new LinkedList<KeyValuePair>();
        }
        tags.add(new KeyValuePair(key, value));
        return this;
    }

    /**
     * Finish the active Span. When it is finished, it will be archived by the given {@link TraceSegment}, which owners
     * it.
     */
    public boolean finish() {
        spanSelfLock.lock();
        try {
            if (!isFinished) {
                this.endTime = System.currentTimeMillis();
                if (context == null) {
                    throw new IllegalStateException("Can't finish the span without context owner");
                }
                context.archiveFinishedSpan(this);
                return true;
            } else {
                return false;
            }
        } finally {
            spanSelfLock.unlock();
        }
    }

    @Override
    public AbstractSpan start() {
        this.startTime = System.currentTimeMillis();
        return this;
    }

    /**
     * Record an exception event of the current walltime timestamp.
     *
     * @param t any subclass of {@link Throwable}, which occurs in this span.
     * @return the Span, for chaining
     */
    @Override
    public AbstractSpan log(Throwable t) {
        if (logs == null) {
            logs = new LinkedList<LogDataEntity>();
        }
        logs.add(new LogDataEntity.Builder()
            .add(new KeyValuePair("event", "error"))
            .add(new KeyValuePair("error.kind", t.getClass().getName()))
            .add(new KeyValuePair("message", t.getMessage()))
            .add(new KeyValuePair("stack", ThrowableTransformer.INSTANCE.convert2String(t, 4000)))
            .build(System.currentTimeMillis()));
        return this;
    }

    /**
     * Record a common log with multi fields, for supporting opentracing-java
     *
     * @param fields
     * @return the Span, for chaining
     */
    @Override
    public AbstractSpan log(long timestampMicroseconds, Map<String, ?> fields) {
        if (logs == null) {
            logs = new LinkedList<LogDataEntity>();
        }
        LogDataEntity.Builder builder = new LogDataEntity.Builder();
        for (Map.Entry<String, ?> entry : fields.entrySet()) {
            builder.add(new KeyValuePair(entry.getKey(), entry.getValue().toString()));
        }
        logs.add(builder.build(timestampMicroseconds));
        return this;
    }

    /**
     * In the scope of this span tracing context, error occurred, in auto-instrumentation mechanism, almost means throw
     * an exception.
     *
     * @return span instance, for chaining.
     */
    @Override
    public AbstractSpan errorOccurred() {
        this.errorOccurred = true;
        return this;
    }

    /**
     * Set the operation name, just because these is not compress dictionary value for this name. Use the entire string
     * temporarily, the agent will compress this name in async mode.
     *
     * @param operationName
     * @return span instance, for chaining.
     */
    @Override
    public AbstractSpan setOperationName(String operationName) {
        this.operationName = operationName;
        this.operationId = DictionaryUtil.nullValue();
        return this;
    }

    /**
     * Set the operation id, which compress by the name.
     *
     * @param operationId
     * @return span instance, for chaining.
     */
    @Override
    public AbstractSpan setOperationId(int operationId) {
        this.operationId = operationId;
        this.operationName = null;
        return this;
    }

    @Override
    public int getSpanId() {
        return spanId;
    }

    @Override
    public int getOperationId() {
        return operationId;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    @Override
    public AbstractSpan setLayer(SpanLayer layer) {
        this.layer = layer;
        return this;
    }

    /**
     * Set the component of this span, with internal supported. Highly recommend to use this way.
     *
     * @param component
     * @return span instance, for chaining.
     */
    @Override
    public AbstractSpan setComponent(Component component) {
        this.componentId = component.getId();
        return this;
    }

    /**
     * Set the component name. By using this, cost more memory and network.
     *
     * @param componentName
     * @return span instance, for chaining.
     */
    @Override
    public AbstractSpan setComponent(String componentName) {
        this.componentName = componentName;
        return this;
    }

    @Override
    public AbstractSpan start(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public SpanObject.Builder transform() {
        SpanObject.Builder spanBuilder = SpanObject.newBuilder();

        spanBuilder.setSpanId(this.spanId);
        spanBuilder.setParentSpanId(parentSpanId);
        spanBuilder.setStartTime(startTime);
        spanBuilder.setEndTime(endTime);
        if (operationId != DictionaryUtil.nullValue()) {
            spanBuilder.setOperationNameId(operationId);
        } else {
            spanBuilder.setOperationName(operationName);
        }
        if (isEntry()) {
            spanBuilder.setSpanType(SpanType.Entry);
        } else if (isExit()) {
            spanBuilder.setSpanType(SpanType.Exit);
        } else {
            spanBuilder.setSpanType(SpanType.Local);
        }
        if (this.layer != null) {
            spanBuilder.setSpanLayerValue(this.layer.getCode());
        }
        if (componentId != DictionaryUtil.nullValue()) {
            spanBuilder.setComponentId(componentId);
        } else {
            if (componentName != null) {
                spanBuilder.setComponent(componentName);
            }
        }
        spanBuilder.setIsError(errorOccurred);
        if (this.tags != null) {
            for (KeyValuePair tag : this.tags) {
                spanBuilder.addTags(tag.transform());
            }
        }
        if (this.logs != null) {
            for (LogDataEntity log : this.logs) {
                spanBuilder.addLogs(log.transform());
            }
        }
        if (this.refs != null) {
            for (TraceSegmentRef ref : this.refs) {
                spanBuilder.addRefs(ref.transform());
            }
        }

        return spanBuilder;
    }

    @Override public void ref(TraceSegmentRef ref) {
        if (refs == null) {
            refs = new LinkedList<TraceSegmentRef>();
        }
        if (!refs.contains(ref)) {
            refs.add(ref);
        }
    }

    @Override
    public boolean isEntry() {
        return false;
    }

    @Override
    public boolean isExit() {
        return false;
    }

    @Override public boolean isSampled() {
        return true;
    }

    @Override public void joinCurrentContext() {
        ContextManager.recruit(this);
    }

    @Override public void setOwner(AbstractTracerContext context) {
        spanSelfLock.lock();
        try {
            if (this.context == null) {
                this.context = context;
            } else {
                throw new IllegalStateException("Owner can't be set twice");
            }
        } finally {
            spanSelfLock.unlock();
        }
    }
}
