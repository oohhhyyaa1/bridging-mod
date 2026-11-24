package dev.hybridbridging;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlacementLogicTest {
    private static final Vec3d RAY_START = Vec3d.ZERO;
    private static final Vec3d RAY_END = new Vec3d(4.5d, 0.0d, 0.0d);

    @Test
    void prefersForwardPlacementOverTopPlacement() {
        Vec3d movement = new Vec3d(1.0d, 0.0d, 0.0d);
        Vec3d point = new Vec3d(2.0d, 0.0d, 0.0d);
        double forward = PlacementLogic.scoreCandidate(Direction.EAST, movement, false, false,
                point, RAY_START, RAY_END, 0.25d);
        double top = PlacementLogic.scoreCandidate(Direction.UP, movement, false, false,
                point, RAY_START, RAY_END, 0.25d);
        assertTrue(forward > top);
    }

    @Test
    void diagonalMovementPrefersBetterAlignedFace() {
        Vec3d movement = new Vec3d(1.0d, 0.0d, 0.2d);
        Vec3d point = new Vec3d(2.0d, 0.0d, 0.0d);
        double east = PlacementLogic.scoreCandidate(Direction.EAST, movement, true, false,
                point, RAY_START, RAY_END, 0.25d);
        double south = PlacementLogic.scoreCandidate(Direction.SOUTH, movement, true, false,
                point, RAY_START, RAY_END, 0.25d);
        assertTrue(east > south);
    }

    @Test
    void edgeAssistanceOnlyBoostsForwardAlignedFaces() {
        Vec3d movement = new Vec3d(1.0d, 0.0d, 0.0d);
        Vec3d point = new Vec3d(2.0d, 0.0d, 0.0d);
        double forwardBase = PlacementLogic.scoreCandidate(Direction.EAST, movement, false, false,
                point, RAY_START, RAY_END, 0.25d);
        double forwardEdge = PlacementLogic.scoreCandidate(Direction.EAST, movement, false, true,
                point, RAY_START, RAY_END, 0.25d);
        double sideBase = PlacementLogic.scoreCandidate(Direction.NORTH, movement, false, false,
                point, RAY_START, RAY_END, 0.25d);
        double sideEdge = PlacementLogic.scoreCandidate(Direction.NORTH, movement, false, true,
                point, RAY_START, RAY_END, 0.25d);
        assertTrue(forwardEdge > forwardBase);
        assertEquals(sideBase, sideEdge, 1.0E-6d);
    }

    @Test
    void distanceUsesClosestPointOnRaySegment() {
        assertEquals(3.0d, PlacementLogic.distanceToSegment(
                new Vec3d(2.0d, 3.0d, 0.0d), RAY_START, RAY_END), 1.0E-6d);
    }

    @Test
    void pointsBeyondRayEndAreMeasuredFromEndpoint() {
        assertEquals(1.5d, PlacementLogic.distanceToSegment(
                new Vec3d(6.0d, 0.0d, 0.0d), RAY_START, RAY_END), 1.0E-6d);
    }

    @Test
    void degenerateSegmentUsesDistanceFromItsSinglePoint() {
        assertEquals(3.0d, PlacementLogic.distanceToSegment(
                new Vec3d(1.0d, 2.0d, 2.0d), Vec3d.ZERO, Vec3d.ZERO), 1.0E-6d);
    }
}
