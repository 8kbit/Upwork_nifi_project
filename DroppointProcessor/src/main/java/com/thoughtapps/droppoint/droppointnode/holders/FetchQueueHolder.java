package com.thoughtapps.droppoint.droppointnode.holders;

import com.thoughtapps.droppoint.core.dto.Batch;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by zaskanov on 08.04.2017.
 */

/**
 * Hold queue of batches ready to process
 */
@Component
public class FetchQueueHolder {

    private Map<String, NodeFetchQueue> queueMap = new HashMap<>();

    // add new batch in processing queue
    private synchronized void add(Batch batch) {
        NodeFetchQueue queue = queueMap.get(batch.getNodeId());
        if (queue == null) {
            queueMap.put(batch.getNodeId(), new NodeFetchQueue(batch.getNodeId()));
            queue = queueMap.get(batch.getNodeId());
        }

        queue.add(batch);
    }

    public synchronized void addAll(Collection<Batch> batches) {
        for (Batch batch : batches)
            add(batch);
    }

    // get and remove batch from processing queue. Return NULL if no element exists
    public synchronized Batch poll(String nodeId) {
        NodeFetchQueue queue = queueMap.get(nodeId);
        if (queue == null) return null;

        return queue.poll();
    }

    public synchronized void clear() {
        queueMap.clear();
    }

    private class NodeFetchQueue {
        private Set<Batch> set = new HashSet<>();
        private Deque<Batch> list = new LinkedList<>();

        String nodeId;

        NodeFetchQueue(String nodeId) {
            this.nodeId = nodeId;
        }

        // add new batch in processing queue
        private void add(Batch batch) {
            if (set.add(batch))
                list.addLast(batch);
        }

        // get and remove batch from processing queue. Return NULL if no element exists
        Batch poll() {
            Batch batch = list.pollFirst();
            if (batch == null) return null;
            set.remove(batch);

            return batch;
        }
    }
}
