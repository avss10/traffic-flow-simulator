package com.traffic.flow.simulation.model;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public abstract class LaneChangeBase extends IDMVehicle {
    public LaneChangeBase(
            String id,
            Coordinate source,
            Coordinate target,
            Long[] edgePath,
            Double[] costs,
            List<Coordinate> fullPath) {
        super(id, source, target, edgePath, costs, fullPath);
    }

    public abstract Map<Long, List<Link>> basicMovement(
            IDMVehicle head, double interval, Map<Long, List<Link>> edgeMap);

    public abstract Map<Long, List<Link>> born(Map<Long, List<Link>> edgeMap, String type);
}
