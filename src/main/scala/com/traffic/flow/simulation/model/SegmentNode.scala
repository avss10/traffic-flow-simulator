package com.traffic.flow.simulation.model

import java.io.Serializable

import com.vividsolutions.jts.geom.Coordinate

case class SegmentNode(id: Long, coordinate: Coordinate, signal: Boolean, intersect: Boolean) extends Serializable
