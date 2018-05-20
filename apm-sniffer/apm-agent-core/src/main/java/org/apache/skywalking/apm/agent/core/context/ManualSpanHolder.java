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

package org.apache.skywalking.apm.agent.core.context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.skywalking.apm.agent.core.context.trace.IManualSpan;

/**
 * @author wusheng
 */
public class ManualSpanHolder {
    private volatile Map holder;
    private volatile ReentrantLock holderLock;
    private volatile List finishedSpans;

    private ManualSpanHolder() {
        holder = new HashMap(0);
        holderLock = new ReentrantLock();
        finishedSpans = new LinkedList();
    }

    void addSpan(IManualSpan span) {
        holderLock.lock();
        try {
            if (holder.containsKey(span)) {
                throw new IllegalStateException("Can't add span into span holder twice");
            }
            holder.put(span, new Object());
        } finally {
            holderLock.unlock();
        }
    }

    void finishSpan(IManualSpan span) {
        holderLock.lock();
        try {
            if (holder.containsKey(span)) {
                holder.remove(span);
                finishedSpans.add(span);
            } else {
                throw new IllegalStateException("Can't finish the span not in the context.");
            }
        } finally {
            holderLock.unlock();
        }
    }

    public boolean isAllFinished() {
        return holder.size() == 0;
    }

    static void create(TracingContext tracingContext) {
        synchronized (tracingContext) {
            if (tracingContext.hasManualSpanHolder()) {
                ManualSpanHolder holder = new ManualSpanHolder();
                tracingContext.setManualSpanHolder(holder);
            }
        }
    }
}
