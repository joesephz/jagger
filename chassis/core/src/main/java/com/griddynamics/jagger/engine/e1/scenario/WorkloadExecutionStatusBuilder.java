/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.griddynamics.jagger.engine.e1.scenario;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.griddynamics.jagger.coordinator.NodeId;

import java.util.Map;
import java.util.Set;

public class WorkloadExecutionStatusBuilder {
    private final Map<NodeId, Integer> threads = Maps.newConcurrentMap();
    private final Map<NodeId, Integer> samples = Maps.newConcurrentMap();
    private final Map<NodeId, Integer> delays = Maps.newConcurrentMap();
    private final Map<NodeId, Long> pollTime = Maps.newConcurrentMap();
    private WorkloadTask task;

    public WorkloadExecutionStatusBuilder(WorkloadTask task) {
        this.task = task;
    }

    public WorkloadExecutionStatusBuilder addNodeInfo(NodeId id, int threads, int samples, Integer delay, long pollTime) {
        this.threads.put(id, threads);
        this.samples.put(id, samples);
        this.delays.put(id, delay);
        this.pollTime.put(id, pollTime);
        return this;
    }

    public WorkloadExecutionStatus build() {
        return new DefaultWorkloadExecutionStatus(threads, samples, delays, pollTime, task);
    }

    private class DefaultWorkloadExecutionStatus implements WorkloadExecutionStatus {
        private final Set<NodeId> nodes;
        private final Map<NodeId, Integer> threads;
        private final Map<NodeId, Integer> samples;
        private final Map<NodeId, Integer> delays;
        private final Map<NodeId, Long> pollTime;
        private WorkloadTask task;

        private DefaultWorkloadExecutionStatus(Map<NodeId, Integer> threads, Map<NodeId, Integer> samples,
                                               Map<NodeId, Integer> delays, Map<NodeId, Long> pollTime, WorkloadTask task) {
            boolean nodesAreEqual = threads.keySet().equals(samples.keySet()) && samples.keySet().equals(pollTime.keySet());

            Preconditions.checkArgument(nodesAreEqual);
            this.nodes = threads.keySet();

            this.task = task;
            this.threads = threads;
            this.samples = samples;
            this.delays = delays;
            this.pollTime = pollTime;
        }


        @Override
        public Set<NodeId> getNodes() {
            return nodes;
        }

        @Override
        public Integer getThreads(NodeId id) {
            return threads.get(id);
        }

        @Override
        public Integer getSamples(NodeId id) {
            return samples.get(id);
        }

        @Override
        public Integer getDelay(NodeId id) {
            return delays.get(id);
        }

        @Override
        public Long getPollTime(NodeId id) {
            return pollTime.get(id);
        }

        @Override
        public int getTotalSamples() {
            int result = 0;
            for (Integer sample : samples.values()) {
                result += sample;
            }
            return result;
        }

        @Override
        public int getTotalThreads() {
            int result = 0;
            for (Integer threads : this.threads.values()) {
                result += threads;
            }
            return result;
        }

        @Override
        public String toString() {
            String line = "---------------------------------------------------------------------------------------------------------\n";
            String format = "|%1$-40s|%2$-20s|%3$-20s|%4$-20s|\n";
            String report = String.format(this.task.getTaskName() + '\n' +
                    line + format + line, "IDENTIFIER", "THREADS", "SAMPLES", "DELAYS");
            Set<NodeId> nodes = Sets.newHashSet(this.threads.keySet());
            nodes.addAll(this.samples.keySet());
            nodes.addAll(this.delays.keySet());

            for (NodeId node : nodes) {
                report += String.format(format,
                        node.getIdentifier(), this.threads.get(node),
                        this.samples.get(node), this.delays.get(node));
            }
            return report + line;
        }
    }
}