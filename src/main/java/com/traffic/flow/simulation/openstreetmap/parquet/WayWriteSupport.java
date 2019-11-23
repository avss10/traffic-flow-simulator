package com.traffic.flow.simulation.openstreetmap.parquet;

import static java.util.stream.IntStream.range;
import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.INT32;
import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.INT64;
import static org.apache.parquet.schema.Type.Repetition.REPEATED;
import static org.apache.parquet.schema.Type.Repetition.REQUIRED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

@ToString
@EqualsAndHashCode
public class WayWriteSupport extends OpenStreetMapEntityWriteSupport<Way> {

    private final PrimitiveType nodeIndexType;
    private final PrimitiveType nodeIdType;
    private final GroupType nodes;

    public WayWriteSupport() {
        nodeIndexType = new PrimitiveType(REQUIRED, INT32, "index");
        nodeIdType = new PrimitiveType(REQUIRED, INT64, "nodeId");
        nodes = new GroupType(REPEATED, "nodes", nodeIndexType, nodeIdType);
    }

    @Override
    protected MessageType getSchema() {
        final List<Type> attributes = new ArrayList<>(getCommonAttributes());
        attributes.add(nodes);
        return new MessageType("way", attributes);
    }

    @Override
    protected void writeSpecificFields(Way record, int nextAvailableIndex) {
        final List<WayNode> wayNodes = record.getWayNodes();
        final Map<Integer, Long> indexedNodes = new HashMap<>();
        range(0, wayNodes.size())
                .forEach(index -> indexedNodes.put(index, wayNodes.get(index).getNodeId()));

        if (!indexedNodes.isEmpty()) {
            recordConsumer.startField(nodes.getName(), nextAvailableIndex);
            indexedNodes.forEach(
                    (index, nodeId) -> {
                        recordConsumer.startGroup();

                        recordConsumer.startField(nodeIndexType.getName(), 0);
                        recordConsumer.addInteger(index);
                        recordConsumer.endField(nodeIndexType.getName(), 0);

                        recordConsumer.startField(nodeIdType.getName(), 1);
                        recordConsumer.addLong(nodeId);
                        recordConsumer.endField(nodeIdType.getName(), 1);

                        recordConsumer.endGroup();
                    });
            recordConsumer.endField(nodes.getName(), nextAvailableIndex);
        }
    }
}
